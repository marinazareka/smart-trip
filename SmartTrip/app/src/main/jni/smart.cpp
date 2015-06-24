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
