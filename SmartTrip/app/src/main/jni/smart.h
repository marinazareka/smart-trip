#ifndef SMART_JNI_H
#define SMART_JNI_H

#include <string>
#include <vector>

struct Point {
    double lat;
    double lon;
    std::string name;
};

extern bool connect(const char* smartspace, const char* ip_address, int port);
extern bool disconnect();
extern bool publishUserContext(double lat, double lon);

extern std::vector<Point> loadPoints(double lat, double lon, double radius);


#endif

