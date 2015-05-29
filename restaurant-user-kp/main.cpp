#include <chrono>
#include <sstream>
#include <stdexcept>

#include <unistd.h>

#include <QCoreApplication>
#include <QSettings>
#include <QDebug>

#include <smartslog/generic.h>
#include "ontology/ontology.h"

static std::default_random_engine random_engine;
static std::uniform_int_distribution<int> id_distribution(1, 1000000000);

static individual_t* userProfile;
static individual_t* userStaticContext;
static individual_t* user;

class Exception : public std::runtime_error {
public:
    Exception(const std::string& message);
};

Exception::Exception(const std::string& message) : std::runtime_error(message) {

}

static void randomize() {
    unsigned seed = std::chrono::system_clock::now().time_since_epoch() / std::chrono::milliseconds(1);
    random_engine.seed(seed);
}

static std::string generateId() {
    std::ostringstream out;
    out << "id" << id_distribution(random_engine);
    return out.str();
}

static void processResponse(individual_t* userReqeust) {

}

static void initializeUserProfile() {
    // User static context has age = 25

    userProfile = sslog_new_individual(CLASS_USERPROFILE);
    sslog_set_individual_uuid(userProfile, generateId().c_str());

    userStaticContext = sslog_new_individual(CLASS_USERCONTEXT);
    sslog_set_individual_uuid(userStaticContext, generateId().c_str());
    sslog_add_property(userStaticContext, PROPERTY_AGE, "25");

    user = sslog_new_individual(CLASS_USER);
    sslog_set_individual_uuid(user, generateId().c_str());

    sslog_add_property(user, PROPERTY_HASPROFILE, userProfile);
    sslog_add_property(user, PROPERTY_HASSTATICUSERCONTEXT, userStaticContext);

    qDebug("Inserting user static information");
    sslog_ss_insert_individual(userProfile);
    sslog_ss_insert_individual(userStaticContext);
    sslog_ss_insert_individual(user);
}

static individual_t* doRequest() {
    individual_t* userDynamicContext = sslog_new_individual(CLASS_USERCONTEXT);
    sslog_set_individual_uuid(userDynamicContext, generateId().c_str());

    sslog_add_property(userDynamicContext, PROPERTY_LAT, "61.78");
    sslog_add_property(userDynamicContext, PROPERTY_LON, "34.35");

    individual_t* userRequest = sslog_new_individual(CLASS_USERREQUEST);
    sslog_set_individual_uuid(userRequest, generateId().c_str());

    individual_t* restaurantPreferenceItem = sslog_new_individual(CLASS_BETWEENPREFERENCETERM);
    sslog_set_individual_uuid(restaurantPreferenceItem, generateId().c_str());
    sslog_add_property(restaurantPreferenceItem, PROPERTY_LOWER, "2");
    sslog_add_property(restaurantPreferenceItem, PROPERTY_UPPER, "4");
    sslog_add_property(restaurantPreferenceItem, PROPERTY_PROPERTY, "rating");

    sslog_add_property(userRequest, PROPERTY_CONSISTSIN, restaurantPreferenceItem);
    sslog_add_property(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT, userDynamicContext);

    sslog_add_property(userRequest, PROPERTY_RELATESTO, user);

    qDebug() << "Inserting request information. Request uuid " << userRequest->uuid;
    sslog_ss_insert_individual(restaurantPreferenceItem);
    sslog_ss_insert_individual(userDynamicContext);
    sslog_ss_insert_individual(userRequest);

    return userRequest;
}

static void subscribeResponse(individual_t* userRequest) {
    subscription_t* responseSubscription = sslog_new_subscription(false);

    list_t* properties = list_get_new_list();
    list_add_data(PROPERTY_PROCESSED, properties);

    sslog_sbcr_add_individual(responseSubscription, userRequest, properties);

    if (sslog_sbcr_subscribe(responseSubscription) != 0) {
        throw Exception("Can't subscribe exception");
    }


    for (;;) {
        const prop_val_t* propValue = sslog_get_property(userRequest, PROPERTY_PROCESSED->name);
        if (propValue != NULL && propValue->prop_value != NULL) {
            QString processedValue = reinterpret_cast<const char*>(propValue->prop_value);
            if (processedValue == "true") {
                processResponse(userRequest);
                break;
            }
        }

        qDebug("Waiting response");
        if (sslog_sbcr_wait(responseSubscription) != 0) {
            throw Exception("Error while waiting subscription");
        }
    }

    sslog_sbcr_unsubscribe(responseSubscription);
}



int main(int argc, char *argv[]) {
    Q_UNUSED(argc);
    Q_UNUSED(argv);

    QCoreApplication::setOrganizationName("PetrSU");
    QCoreApplication::setApplicationName("Restaurant User KP");
    QSettings settings;

    randomize();

    sslog_ss_init_session_with_parameters("X", "127.0.0.1", 10622);
    register_ontology();

    qDebug("Joining KP");
    if (ss_join(sslog_get_ss_info(), const_cast<char*>("Restaurant User KP")) == -1) {
        qDebug("Can't join KP");
        return 1;
    }

    try {
        initializeUserProfile();
        individual_t* userRequest = doRequest();
        subscribeResponse(userRequest);
    } catch (Exception ex) {
        qDebug() << "Error while working with smartspace: " << ex.what();
    }

    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());

    return 0;
}
