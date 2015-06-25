extern "C" {
#include "smartslog/generic.h"
#include "ontology/ontology.h"
#include "util.h"
}

#include "smart.h"

#define BUFFER_LEN (1024)

static individual_t* user = NULL;
static individual_t* userProfile = NULL;
static individual_t* userStaticContext = NULL;
static individual_t* userRequest = NULL;

static char buffer[BUFFER_LEN];

static individual_t* createIndividual(class_t* cls) {
    individual_t* individual = sslog_new_individual(cls);
    generateId(buffer, BUFFER_LEN);
    sslog_set_individual_uuid(individual, buffer);
}

static void setDoubleProperty(individual_t* individual, property_t* property, double value) {
    snprintf(buffer, BUFFER_LEN, "%lf", value);
    sslog_add_property(individual, property, buffer);
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

bool connect(const char* smartspace, const char* ip_address, int port) {
    randomize_time();

    sslog_ss_init_session_with_parameters(smartspace, ip_address, port);
    register_ontology();

    if (ss_join(sslog_get_ss_info(), const_cast<char*>("Smart Trip Android KP")) == -1) {
        return false;
    }

    publishUser();

    return true;
}

bool disconnect() {
    unpublishUser();

    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

/**
 * Create dynamic context and add it to userRequest without publishing request's property.
 */
bool publishUserContext(individual_t* userRequest, double lat, double lon) {
    individual_t* newUserDynamicContext = createIndividual(CLASS_USERCONTEXT);
    setDoubleProperty(newUserDynamicContext, PROPERTY_LAT, lat);
    setDoubleProperty(newUserDynamicContext, PROPERTY_LON, lon);
    sslog_ss_insert_individual(newUserDynamicContext);
    sslog_add_property(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT, newUserDynamicContext);
}

std::vector<Point> loadPoints(double lat, double lon, double radius) {
    userRequest = createIndividual(CLASS_USERREQUEST);
    publishUserContext(userRequest, lat, lon);

    sslog_ss_insert_individual(userRequest);
    sslog_ss_add_property(user, PROPERTY_RELATESTO, userRequest);

    std::vector<Point> ret;

    ret.push_back({61.78, 34.35, "Hello world"});
    ret.push_back({61.781, 34.351, "Hello world 1"});
    ret.push_back({61.782, 34.352, "Hello world 2"});
    ret.push_back({61.783, 34.353, "Hello world 3"});

    return ret;
}
