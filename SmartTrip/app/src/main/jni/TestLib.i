%module TestLib

%{
    #include "test.h"
%}

bool test(const char* smartspace, const char* ip_address, int port);
