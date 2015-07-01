#include "gets.h"

#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QXmlStreamReader>

#include "placemark.h"

const QString Gets::GETS_SERVER = "http://gets.cs.petrsu.ru/gets/service";

Gets::Gets(QObject *parent) : QObject(parent) {
    m_networkManager = new QNetworkAccessManager(this);
    connect(m_networkManager, SIGNAL(finished(QNetworkReply*)), this, SLOT(onRequestFinished(QNetworkReply*)));
}

void Gets::requestPoints(QVariant tag, double lat, double lon, QString pattern, double radius) {
    QString postRequest = QString("<request><params>"
            "<latitude>%1</latitude>"
            "<longitude>%2</longitude>"
            "<radius>%3</radius>"
            "<pattern>%4</pattern>"
            "</params></request>").arg(lat).arg(lon).arg(radius).arg(pattern);
    // It is safe to pass possibly empty pattern because it will be ignored by Gets

    QNetworkRequest request(QUrl(GETS_SERVER + "/loadPoints.php"));
    request.setHeader(QNetworkRequest::ContentTypeHeader, "text/xml");
    qDebug() << "Sending request";
    QNetworkReply* reply = m_networkManager->post(request, postRequest.toUtf8());
    m_pendingRequests.insert(reply, tag);
}

void Gets::onRequestFinished(QNetworkReply* reply) {
    QByteArray resultByteArray = reply->readAll();
    QString result = resultByteArray;

    //qDebug() << "Received " << result;

    QXmlStreamReader xml(result);

    QList<Placemark> placemarks;

    while (!xml.hasError() && !xml.atEnd()) {
        if (xml.readNextStartElement()) {
            if (xml.name() == "Placemark") {
                placemarks.append(readPlacemark(xml));
            }
        }
    }

    for (Placemark placemark : placemarks) {
        qDebug() << "Loaded placemark" << placemark.getLat() << placemark.getLon();
    }

    QVariant pendingRequestTag = m_pendingRequests.value(reply);
    m_pendingRequests.remove(reply);

    emit pointsLoaded(pendingRequestTag, placemarks);
}

Placemark Gets::readPlacemark(QXmlStreamReader& xml) {    
    QString coordinates;
    QString name;
    QString description;

    while (xml.readNextStartElement()) {
        if (xml.name() == "Point") {
            xml.readNextStartElement();
            if (xml.name() != "coordinates")
                throw std::runtime_error("Incorrect XML");

            coordinates = xml.readElementText();
            xml.skipCurrentElement();
        } else if (xml.name() == "name") {
            name = xml.readElementText();
        } else if (xml.name() == "description") {
            description = xml.readElementText();
        } else {
            xml.skipCurrentElement();
        }
    }

    return Placemark(coordinates, name, description);
}

