#include "restorauntgenerator.h"

#include <QTimer>
#include <QString>
#include <QDebug>

#include "ontology/ontology.h"

#include <smartslog/generic.h>

#include <captgenerator.h>

#include <terms/lessthanpreferenceterm.h>

RestaurantGenerator::RestaurantGenerator(QObject *parent) : QObject(parent) {

}

void RestaurantGenerator::run() {
    m_captGenerator = new CAPTGenerator("RestorauntGenerator", "restaurant");
    m_captGenerator->setParent(this);

    connect(m_captGenerator, &CAPTGenerator::userRequestReceived,
            this, &RestaurantGenerator::processNewRequest);

    connect(m_captGenerator, &CAPTGenerator::userRequestProcessed,
            this, &RestaurantGenerator::continueWaiting);

    Common::initializeSmartspace("RestorauntGenerator");
    m_captGenerator->publish();
    m_captGenerator->subscribe();

    m_captGenerator->waitSubscription();
}

void RestaurantGenerator::continueWaiting() {
    m_captGenerator->waitSubscription();
}

void RestaurantGenerator::processNewRequest(UserRequest userRequest) {
    float latitude = userRequest.getDynamicContextProperty(PROPERTY_LAT->name).toFloat();
    float longitude = userRequest.getDynamicContextProperty(PROPERTY_LON->name).toFloat();
    float age = userRequest.getStaticContextProperty(PROPERTY_AGE->name).toFloat();

    qDebug() << "Dynamic context lat = " << latitude << " lon = " << longitude << " age = " << age;

    if (age < 18) {
        LessThanPreferenceTerm* term = new LessThanPreferenceTerm("age-limit", 18);
        m_captGenerator->publishProcessedRequest(userRequest, term);
    } else {
        m_captGenerator->publishProcessedRequest(userRequest, nullptr);
    }
}

void RestaurantGenerator::shutdown() {
    qDebug() << "Shutdown";

    m_captGenerator->unsubscribe();
    m_captGenerator->unpublish();
    Common::shutdownSmartspace();

    emit finished();
}
