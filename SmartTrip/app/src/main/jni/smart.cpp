extern "C" {
#include "smartslog/generic.h"
}

#include "smart.h"

bool connect(const char* smartspace, const char* ip_address, int port) {
    sslog_ss_init_session_with_parameters(smartspace, ip_address, port);

    if (ss_join(sslog_get_ss_info(), const_cast<char*>("Smart Trip Android KP")) == -1) {
        return false;
    }

    return true;
}

bool disconnect() {
    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

std::vector<Point> loadPoints(double lat, double lon, double radius) {
    std::vector<Point> ret;

    ret.push_back({61.78, 34.35, "Hello world"});
    ret.push_back({61.781, 34.351, "Hello world 1"});
    ret.push_back({61.782, 34.352, "Hello world 2"});
    ret.push_back({61.783, 34.353, "Hello world 3"});

    return ret;
}
