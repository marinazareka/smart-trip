%module Smart

%{
    #include "smart.h"
%}

bool connect(const char* smartspace, const char* ip_address, int port);
bool disconnect();