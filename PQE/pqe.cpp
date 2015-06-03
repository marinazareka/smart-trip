#include "pqe.h"

#include <chrono>
#include <functional>

#include <QDebug>

#include "ontology/ontology.h"
#include <smartslog/generic.h>

#include "captgenerator.h"
#include "cfunction.hpp"

typedef CFunction<subscription_t*> SubWrapper;

Pqe::Pqe(QObject *parent) : QObject(parent) {

}

void Pqe::run() {
    Common::initializeSmartspace("Preference query executor");
    subscribe();
}

void Pqe::subscribe() {
    using namespace std::placeholders;

    // Subscribe to CAPTGenerators
    m_captGeneratorSubscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_captGeneratorSubscription, CLASS_CONTEXTAWAREGENERATOR);
    SubWrapper::wrap(m_captGeneratorSubscription, std::bind(&Pqe::processAsyncCaptGeneratorSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_captGeneratorSubscription, &SubWrapper::handler);

    // Subscribe to User requests
    m_userRequestSubscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_userRequestSubscription, CLASS_USERREQUEST);
    SubWrapper::wrap(m_userRequestSubscription, std::bind(&Pqe::processAsyncUserRequestSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_userRequestSubscription, &SubWrapper::handler);

    connect(this, &Pqe::captGeneratorAdded, this, &Pqe::addCaptGenerator);
    connect(this, &Pqe::captGeneratorRemoved, this, &Pqe::removeCaptGenerator);
    connect(this, &Pqe::userRequestAdded, this, &Pqe::processUserRequest);

    sslog_sbcr_subscribe(m_captGeneratorSubscription);
    sslog_sbcr_subscribe(m_userRequestSubscription);
}

void Pqe::addCaptGenerator(QString uuid) {
    qDebug() << "CAPTGenerator added: " << uuid;
}

void Pqe::removeCaptGenerator(QString uuid) {
    qDebug() << "CAPTGenerator removed: " << uuid;
}

void Pqe::processUserRequest(QString uuid) {
    qDebug() << "UserRequest added: " << uuid;
}

void Pqe::processAsyncCaptGeneratorSubscription(subscription_t* subscription) {
    // Extract generator
    auto changes = sslog_sbcr_get_changes_last(subscription);
    auto individuals = sslog_sbcr_ch_get_individual_all(changes);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &individuals->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);

        if (sslog_ss_exists_uuid(const_cast<char*>(uuid))) {
            emit captGeneratorAdded(uuid);
        } else {
            emit captGeneratorRemoved(uuid);
        }
    }
}

void Pqe::processAsyncUserRequestSubscription(subscription_t* subscription){
    auto changes = sslog_sbcr_get_changes_last(subscription);
    auto individuals = sslog_sbcr_ch_get_individual_all(changes);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &individuals->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);

        if (sslog_ss_exists_uuid(const_cast<char*>(uuid))) {
            emit userRequestAdded(uuid);
        }
    }
}
