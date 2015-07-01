#ifndef SMART_JNI_H
#define SMART_JNI_H

#include <string>
#include <vector>

struct Point {
    double lat;
    double lon;
    std::string name;
    std::string description;
};

class Exception {
public:
    Exception(std::string message_) {
        message = message_;
    }

    std::string getMessage() {
        return message;
    }

private:
    std::string message;
};

extern void connect(const char* smartspace, const char* ip_address, int port);
extern void disconnect();
extern std::vector<Point> loadPoints(double lat, double lon, double radius, const char* pattern);


#endif

