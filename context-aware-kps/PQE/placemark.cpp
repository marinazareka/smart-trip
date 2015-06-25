#include "placemark.h"

class PlacemarkData : public QSharedData {
public:
    double lat;
    double lon;
};

Placemark::Placemark(double lat, double lon) : data(new PlacemarkData) {
    data->lat = lat;
    data->lon = lon;
}

Placemark::Placemark(const Placemark &rhs) : data(rhs.data) {

}

Placemark &Placemark::operator=(const Placemark &rhs) {
    if (this != &rhs)
        data.operator=(rhs.data);
    return *this;
}

Placemark::~Placemark() {

}

double Placemark::getLat() {
    return data->lat;
}

double Placemark::getLon() {
    return data->lon;
}

