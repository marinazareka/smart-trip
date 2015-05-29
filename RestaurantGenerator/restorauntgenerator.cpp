#include "restorauntgenerator.h"

#include <QTimer>
#include <QString>
#include <QDebug>

#include "ontology/ontology.h"

#include <smartslog/generic.h>

#include <captgenerator.h>

RestaurantGenerator::RestaurantGenerator(QObject *parent) : QObject(parent) {

}

void RestaurantGenerator::run() {

    m_captGenerator = new CAPTGenerator("RestorauntGenerator", "restaurant");
    m_captGenerator->setParent(this);

    connect(m_captGenerator, &CAPTGenerator::subscriptionChanged,
            this, &RestaurantGenerator::processNewRequest);

    m_captGenerator->initializeSmartspace();
    m_captGenerator->publish();
    m_captGenerator->subscribe();
}

void RestaurantGenerator::processNewRequest(QString userUuid, QString dynamicContextUuid,
                                            QString staticContextUuid, QString userRequestUuid) {
    qDebug() << "UserRequest received with uuid " << userRequestUuid;

    individual_t* userRequest = sslog_new_individual(CLASS_USERREQUEST);
    sslog_set_individual_uuid(userRequest, userRequestUuid.toStdString().c_str());

    individual_t* dynamicContext = sslog_new_individual(CLASS_USERCONTEXT);
    sslog_set_individual_uuid(dynamicContext, dynamicContextUuid.toStdString().c_str());

    individual_t* staticContext = sslog_new_individual(CLASS_USERCONTEXT);
    sslog_set_individual_uuid(staticContext, staticContextUuid.toStdString().c_str());

    float latitude = 0.0;
    float longitude = 0.0;
    float age = 0.0;

    latitude = getFloatProperty(dynamicContext, PROPERTY_LAT);
    longitude = getFloatProperty(dynamicContext, PROPERTY_LON);
    age = getFloatProperty(staticContext, PROPERTY_AGE);

    sslog_free_individual(userRequest);
    sslog_free_individual(dynamicContext);

    qDebug() << "Dynamic context lat = " << latitude << " lon = " << longitude << " age = " << age;
}

void RestaurantGenerator::shutdown() {
    m_captGenerator->unsubscribe();
    m_captGenerator->unpublish();
    m_captGenerator->shutdownSmartspace();

    emit finished();
}

QString RestaurantGenerator::getStringProperty(individual_t *individual, property_t *property) {
    prop_val_t* propValue = sslog_ss_get_property(individual, property);
    if (propValue != nullptr) {
        const char* propertyValueStr = reinterpret_cast<const char*>(propValue->prop_value);
        if (propertyValueStr != nullptr) {
            return propertyValueStr;
        }
    }

    return QString();
}

float RestaurantGenerator::getFloatProperty(individual_t *individual, property_t *property) {
    QString stringProperty = getStringProperty(individual, property);

    if (stringProperty.isEmpty() || stringProperty.isNull()) {
        return 0.0;
    } else {
        bool ok;
        float res = stringProperty.toFloat(&ok);

        if (!ok) {
            return 0.0;
        } else {
            return res;
        }
    }
}

