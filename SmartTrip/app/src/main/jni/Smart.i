%module Smart

%include "std_string.i"
%include "std_vector.i"
%include exception.i

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

%include exception.i

%exception {
	try {
		$function
	} catch(...) {
		SWIG_exception(SWIG_RuntimeError, "Unknown exception");
	}
}

void connect(const char* smartspace, const char* ip_address, int port);
void disconnect();

std::vector<Point> loadPoints(double lat, double lon, double radius);