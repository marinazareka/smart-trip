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
    initializeSmartspace();

    m_captGenerator = new CAPTGenerator("RestorauntGenerator", "restaurant");
    m_captGenerator->setParent(this);

    connect(m_captGenerator, SIGNAL(subscriptionChanged(subscription_t*, QString)),
            this, SLOT(test(subscription_t*,QString)));

    m_captGenerator->publish();
    m_captGenerator->subscribe();
}

void RestaurantGenerator::test(subscription_t*, QString userRequestUuid) {
    qDebug() << "UserRequest received with uuid " << userRequestUuid;

    individual_t* userRequest = sslog_new_individual(CLASS_USERREQUEST);
    sslog_set_individual_uuid(userRequest, userRequestUuid.toStdString().c_str());

    float latitude = 0.0;
    float longitude = 0.0;

    prop_val_t* propValue = sslog_ss_get_property(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT);
    if (propValue != nullptr) {
        individual_t* dynamicContext = reinterpret_cast<individual_t*>(propValue->prop_value);

        latitude = getFloatProperty(dynamicContext, PROPERTY_LAT);
        longitude = getFloatProperty(dynamicContext, PROPERTY_LON);

        sslog_free_value_struct(propValue);
    } else {
        qDebug() << "User request without dynamic context received";
    }

    qDebug() << "Dynamic context lat = " << latitude << " lon = " << longitude;

    m_captGenerator->unsubscribe();
    m_captGenerator->unpublish();

    emit finished();
}

void RestaurantGenerator::initializeSmartspace() {
    sslog_ss_init_session_with_parameters("X", "127.0.0.1", 10622);
    register_ontology();

    qDebug("Joining KP");
    if (ss_join(sslog_get_ss_info(), const_cast<char*>("Restaurant Generator KP")) == -1) {
        qDebug("Can't join SS");
        emit finished();
    }
}

void RestaurantGenerator::shutdownSmartspace() {
    qDebug("Shutting down KP");
    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

QString RestaurantGenerator::getStringProperty(individual_t *individual, property_t *property) {
    auto propValue = sslog_ss_get_property(individual, property);
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

