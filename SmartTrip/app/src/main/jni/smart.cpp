extern "C" {
#include "smartslog/generic.h"
#include "ontology/ontology.h"
#include "util.h"
}

#include "smart.h"

#define BUFFER_LEN (1024)

static individual_t* user;
static individual_t* userProfile;
static individual_t* userStaticContext;

static char buffer[BUFFER_LEN];

static individual_t* createIndividual(class_t* cls) {
    individual_t* individual = sslog_new_individual(cls);
    generateId(buffer, BUFFER_LEN);
    sslog_set_individual_uuid(individual, buffer);
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

bool publishUserContext(double lat, double lon) {

}

std::vector<Point> loadPoints(double lat, double lon, double radius) {
    std::vector<Point> ret;

    ret.push_back({61.78, 34.35, "Hello world"});
    ret.push_back({61.781, 34.351, "Hello world 1"});
    ret.push_back({61.782, 34.352, "Hello world 2"});
    ret.push_back({61.783, 34.353, "Hello world 3"});

    return ret;
}
