extern "C" {
#include "smartslog/generic.h"
#include "ontology/ontology.h"
#include "util.h"
#include "unistd.h"
}

#include <cstring>

#include <android/log.h>


#include "smart.h"

#define BUFFER_LEN (1024)
#define TAG "SmartLib"

static individual_t* user = NULL;
static individual_t* userProfile = NULL;
static individual_t* userStaticContext = NULL;
static individual_t* userRequest = NULL;

static char buffer[BUFFER_LEN];

static individual_t* createIndividual(class_t* cls) {
    individual_t* individual = sslog_new_individual(cls);
    generateId(buffer, BUFFER_LEN);

    sslog_set_individual_uuid(individual, buffer);
    return individual;
}

static void setDoubleProperty(individual_t* individual, property_t* property, double value) {
    snprintf(buffer, BUFFER_LEN, "%lf", value);
    sslog_add_property(individual, property, buffer);
}

static void setIntProperty(individual_t* individual, property_t* property, int value) {
    snprintf(buffer, BUFFER_LEN, "%d", value);
    sslog_add_property(individual, property, buffer);
}

static void setStringProperty(individual_t* individual, property_t* property, const char* value) {
    sslog_add_property(individual, property, value);
}

static double getDoubleProperty(individual_t* individual, property_t* property, bool* ok) {
    const prop_val_t* value = sslog_get_property(individual, property->name);
    if (value == NULL || value->prop_value == NULL) {
        if (ok != NULL) {
            *ok = false;
        }
        return 0;
    }

    const char* strValue = reinterpret_cast<const char*>(value->prop_value);

    if (ok != NULL) {
        *ok = true;
    }
    return atof(strValue);
}

static void publishUser() {
    userProfile = createIndividual(CLASS_USERPROFILE);
    userStaticContext = createIndividual(CLASS_USERCONTEXT);
    user = createIndividual(CLASS_USER);

    sslog_add_property(user, PROPERTY_HASPROFILE, userProfile);
    sslog_add_property(user, PROPERTY_HASSTATICUSERCONTEXT, userStaticContext);

    sslog_ss_insert_individual(userProfile);
    sslog_ss_insert_individual(userStaticContext);
    sslog_ss_insert_individual(user);
}

static void unpublishUser() {
    sslog_ss_remove_individual(userProfile);
    sslog_ss_remove_individual(userStaticContext);
    sslog_ss_remove_individual(user);
}

void connect(const char* smartspace, const char* ip_address, int port) {
    randomize_time();

    sslog_ss_init_session_with_parameters(smartspace, ip_address, port);
    register_ontology();

    if (ss_join(sslog_get_ss_info(), const_cast<char*>("Smart Trip Android KP")) == -1) {
        throw Exception("Can't connect smartspace");
    }

    publishUser();
}

void disconnect() {
    unpublishUser();

    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

/**
 * Create dynamic context and add it to userRequest without publishing request's property.
 */
static individual_t* publishUserContext(individual_t* userRequest, double lat, double lon) {
    individual_t* userDynamicContext = createIndividual(CLASS_USERCONTEXT);
    setDoubleProperty(userDynamicContext, PROPERTY_LAT, lat);
    setDoubleProperty(userDynamicContext, PROPERTY_LON, lon);
    sslog_ss_insert_individual(userDynamicContext);
    sslog_add_property(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT, userDynamicContext);
    return userDynamicContext;
}

static individual_t* publishRequestParameters(individual_t* userRequest, double radius, const char* pattern) {
    individual_t* requestParameters = createIndividual(CLASS_SIMPLEREQUESTPARAMETERS);
    setDoubleProperty(requestParameters, PROPERTY_RADIUS, radius);

    if (pattern) {
        setStringProperty(requestParameters, PROPERTY_PATTERN, pattern);
    }

    sslog_ss_insert_individual(requestParameters);
    sslog_add_property(userRequest, PROPERTY_HASSIMPLEREQUESTPARAMETERS, requestParameters);
}

static individual_t* requestPage(individual_t* userRequest, int pageNumber) {
    individual_t* pageRequest = createIndividual(CLASS_PAGEREQUEST);
    setIntProperty(pageRequest, PROPERTY_PAGE, pageNumber);
    sslog_add_property(pageRequest, PROPERTY_RELATESTO, userRequest);
    sslog_ss_insert_individual(pageRequest);
    return pageRequest;
}

static void waitProcessedRequest(individual_t* request) {
    subscription_t* responseSubscription = sslog_new_subscription(false);

    list_t* properties = list_get_new_list();
    list_add_data(PROPERTY_PROCESSED, properties);
    sslog_sbcr_add_individual(responseSubscription, request, properties);

    if (sslog_sbcr_subscribe(responseSubscription) != 0) {
        throw Exception("Error subscribing exception");
    }

    for (;;) {
        const prop_val_t* propValue = sslog_get_property(request, PROPERTY_PROCESSED->name);

        if (propValue != NULL && propValue->prop_value != NULL) {
            const char* stringValue = reinterpret_cast<const char*>(propValue->prop_value);
            __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Processed property = %s", stringValue);

            if (strcmp(stringValue, "true") == 0) {
                break;
            }
        }

        if (sslog_sbcr_wait(responseSubscription) != 0) {
            throw Exception("Error while waiting subscription");
        }
    }

    sslog_sbcr_unsubscribe(responseSubscription);
    sslog_free_subscription(responseSubscription);
}

static bool readPageResponseAndRemove(individual_t* pageRequest, std::vector<Point>* outVector) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "readPageResponse");

    prop_val_t* propVal = sslog_ss_get_property(pageRequest, PROPERTY_RESULTSIN);

    if (propVal == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "No resultsin property in pagerequest");
        sslog_free_value_struct(propVal);
        //sslog_ss_remove_individual(pageRequest);
        return false;
    }

    individual_t* page = (individual_t*) propVal->prop_value;
    sslog_ss_populate_individual(page);

    list_t* placemarks = sslog_get_property_all(page, PROPERTY_CONSISTSIN->name);
    if (placemarks == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Empty page received");
        sslog_free_value_struct(propVal);
        //sslog_ss_remove_individual(page);
        //sslog_ss_remove_individual(pageRequest);
        return false;
    }

    bool hasPlacemarks = false;
    list_head_t* listHead;
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Starting page: ");
    list_for_each(listHead, &placemarks->links) {
        prop_val_t* propVal = (prop_val_t*) (list_entry(listHead, list_t, links)->data);
        individual_t* placemark = (individual_t*) propVal->prop_value;

        sslog_ss_populate_individual(placemark);

        double lat = getDoubleProperty(placemark, PROPERTY_LAT, NULL);
        double lon = getDoubleProperty(placemark, PROPERTY_LON, NULL);

        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Placemark received %f %f", lat, lon);

        outVector->push_back({lat, lon, "Test"});
        hasPlacemarks = true;

        sslog_ss_remove_individual(placemark);
    }

    sslog_free_value_struct(propVal);
    sslog_ss_remove_individual(page);

    // FIXME: trying to delete pageRequest causes strange notifications to subscribers about _user request_
    // FIXME: May be smartslog deletes all individuals, references by pageRequest?
    // sslog_ss_remove_individual(pageRequest);

    return hasPlacemarks;
}

std::vector<Point> loadPoints(double lat, double lon, double radius, const char* pattern) {
    userRequest = createIndividual(CLASS_USERREQUEST);

    individual_t* userDynamicContext = publishUserContext(userRequest, lat, lon);
    individual_t* requestParameters = publishRequestParameters(userRequest, radius, pattern);

    sslog_add_property(userRequest, PROPERTY_OBJECTTYPE, "any");
    sslog_add_property(userRequest, PROPERTY_RELATESTO, user);

    sslog_ss_insert_individual(userRequest);
    waitProcessedRequest(userRequest);

    std::vector<Point> ret;

    std::vector<individual_t*> pageRequests;
    bool hasPlacemarks = true;
    int page = 0;
    do {
        individual_t* pageRequest = requestPage(userRequest, page++);
        sslog_ss_populate_individual(pageRequest);
        waitProcessedRequest(pageRequest);
        sslog_ss_populate_individual(pageRequest);
        hasPlacemarks = readPageResponseAndRemove(pageRequest, &ret);
        pageRequests.push_back(pageRequest);
    } while (hasPlacemarks);

    // FIXME: Workaround for error described above
    for (individual_t* pageRequest : pageRequests) {
        sslog_ss_remove_individual(pageRequest);
    }

    sslog_ss_remove_individual(userRequest);
    sslog_ss_remove_individual(userDynamicContext);
    sslog_ss_remove_individual(requestParameters);

    return ret;
}
