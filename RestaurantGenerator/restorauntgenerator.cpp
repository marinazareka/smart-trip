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

    m_captGenerator->initializeSmartspace();
    m_captGenerator->publish();
    m_captGenerator->subscribe();
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
    sslog_free_data_property_value_struct(propValue);

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

