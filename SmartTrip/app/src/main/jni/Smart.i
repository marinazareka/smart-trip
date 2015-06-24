%module Smart

%include "std_string.i"
%include "std_vector.i"

%{
    #include "smart.h"
%}

struct Point {
    double lat;
    double lon;
    std::string name;
};

namespace std {
    %template(PointList) vector<Point>;
}

bool connect(const char* smartspace, const char* ip_address, int port);
bool disconnect();
std::vector<Point> loadPoints(double lat, double lon, double radius);