#include "placemark.h"

#include <QString>
#include <QStringList>

class PlacemarkData : public QSharedData {
public:
    double lat;
    double lon;

    QString name;
    QString description;
};

Placemark::Placemark(double lat, double lon) : data(new PlacemarkData) {
    data->lat = lat;
    data->lon = lon;
}

Placemark::Placemark(QString coordinatesString, QString name, QString description)
    : data(new PlacemarkData) {
    QStringList parts = coordinatesString.split(',', QString::SplitBehavior::SkipEmptyParts);
    data->lat = parts.at(1).toDouble();
    data->lon = parts.at(0).toDouble();
    data->name = name;
    data->description = description;
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

QString Placemark::getName() const {
    return data->name;
}

QString Placemark::getDescription() const {
    return data->description;
}

double Placemark::getLat() const {
    return data->lat;
}

double Placemark::getLon() const {
    return data->lon;
}

