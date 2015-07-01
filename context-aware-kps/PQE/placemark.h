#ifndef PLACEMARK_H
#define PLACEMARK_H

#include <QSharedDataPointer>

class PlacemarkData;

class Placemark {
public:
    Placemark(double lat, double lon);
    Placemark(QString coordinatesString, QString name, QString description);

    Placemark(const Placemark &);
    Placemark &operator=(const Placemark &);
    ~Placemark();

    double getLat() const;
    double getLon() const;

    QString getName() const;
    QString getDescription() const;

private:
    QExplicitlySharedDataPointer<PlacemarkData> data;
};

#endif // PLACEMARK_H
