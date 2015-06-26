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

void Gets::requestPoints(double lat, double lon, double radius) {
    QString postRequest = QString("<request><params>"
            "<latitude>%1</latitude>"
            "<longitude>%2</longitude>"
            "<radius>%3</radius>"
            "</params></request>").arg(lat).arg(lon).arg(radius);

    QNetworkRequest request(QUrl(GETS_SERVER + "/loadPoints.php"));
    qDebug() << "Sending request";
    m_networkManager->post(request, postRequest.toUtf8());
}

void Gets::onRequestFinished(QNetworkReply* reply) {
    QByteArray resultByteArray = reply->readAll();
    QString result = resultByteArray;

    qDebug() << "Received " << result;

    QXmlStreamReader xml(result);

    QList<Placemark> placemarks;

    while (!xml.hasError() && !xml.atEnd()) {
        if (xml.readNextStartElement()) {
            if (xml.name() == "Placemark") {
                placemarks.append(readPlacemark(xml));
            } else {
                xml.skipCurrentElement();
            }
        }
    }

    emit pointsLoaded(placemarks);
}

Placemark Gets::readPlacemark(QXmlStreamReader& xml) {
    Placemark placemark(0, 0);
    while (xml.readNextStartElement()) {
        if (xml.name() == "Point") {
            xml.readNextStartElement();
            if (xml.name() != "coordinates")
                throw std::runtime_error("Incorrect XML");

            QString coordinates = xml.readElementText();
            placemark = Placemark(coordinates);

            xml.skipCurrentElement();
        }
    }
    return placemark;
}

