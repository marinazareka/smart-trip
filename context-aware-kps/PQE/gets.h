#ifndef GETS_H
#define GETS_H

#include <QObject>
#include <QList>
#include <QString>

#include "placemark.h"

class QNetworkAccessManager;
class QNetworkReply;
class QXmlStreamReader;

class Gets : public QObject
{
    Q_OBJECT

    static const QString GETS_SERVER;

    QNetworkAccessManager* m_networkManager;

public:
    explicit Gets(QObject *parent = 0);

signals:
    void pointsLoaded(QList<Placemark> points);

public slots:
    void requestPoints(double lat, double lon, double radius);

private slots:
    void onRequestFinished(QNetworkReply* reply);

private:
    Placemark readPlacemark(QXmlStreamReader& xml);
};

#endif // GETS_H
