#include "restorauntgenerator.h"

#include <QTimer>

#include "ontology/ontology.h"

#include <smartslog/generic.h>

#include <captgenerator.h>

RestaurantGenerator::RestaurantGenerator(QObject *parent) : QObject(parent) {

}

void RestaurantGenerator::run() {
    initializeSmartspace();

    m_captGenerator = new CAPTGenerator("RestorauntGenerator", "restaurant");
    m_captGenerator->setParent(this);

    connect(m_captGenerator, SIGNAL(subscriptionChanged(subscription_t*)), this, SLOT(test()));

    m_captGenerator->publish();
    m_captGenerator->subscribe();
}

void RestaurantGenerator::test() {
    qDebug("UserRequest received");
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

