#include "placemark.h"

#include <QString>
#include <QStringList>

class PlacemarkData : public QSharedData {
public:
    double lat;
    double lon;
};

Placemark::Placemark(double lat, double lon) : data(new PlacemarkData) {
    data->lat = lat;
    data->lon = lon;
}

Placemark::Placemark(QString coordinatesString) {
    QStringList parts = coordinatesString.split(';', QString::SplitBehavior::SkipEmptyParts);
    data->lat = parts.at(0).toDouble();
    data->lon = parts.at(1).toDouble();
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

