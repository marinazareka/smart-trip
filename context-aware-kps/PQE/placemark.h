#ifndef PLACEMARK_H
#define PLACEMARK_H

#include <QSharedDataPointer>

class PlacemarkData;

class Placemark {
public:
    Placemark(double lat, double lon);
    Placemark(QString coordinatesString);

    Placemark(const Placemark &);
    Placemark &operator=(const Placemark &);
    ~Placemark();

    double getLat();
    double getLon();

private:
    QExplicitlySharedDataPointer<PlacemarkData> data;
};

#endif // PLACEMARK_H
