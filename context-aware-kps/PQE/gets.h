#ifndef GETS_H
#define GETS_H

#include <QVariant>
#include <QObject>
#include <QList>
#include <QString>
#include <QMap>

#include "placemark.h"

class QNetworkAccessManager;
class QNetworkReply;
class QXmlStreamReader;

class Gets : public QObject
{
    Q_OBJECT

    static const QString GETS_SERVER;

    QNetworkAccessManager* m_networkManager;
    QMap<QNetworkReply*, QVariant> m_pendingRequests;

public:
    explicit Gets(QObject *parent = 0);

signals:
    void pointsLoaded(QVariant userRequest, QList<Placemark> points);

public slots:
    void requestPoints(QVariant userRequest, double lat, double lon, QString pattern, double radius);

private slots:
    void onRequestFinished(QNetworkReply* reply);

private:
    Placemark readPlacemark(QXmlStreamReader& xml);
};

#endif // GETS_H
