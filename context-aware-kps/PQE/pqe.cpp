#include "pqe.h"

#include <chrono>
#include <functional>

#include <QDebug>

#include "ontology/ontology.h"
#include <smartslog/generic.h>

#include "captgenerator.h"
#include "cfunction.hpp"
#include "captgeneratordesc.h"

typedef CFunction<subscription_t*> SubWrapper;

Pqe::Pqe(QObject *parent) : QObject(parent),
      m_captGeneratorSubscription(nullptr), m_userRequestSubscription(nullptr),
      m_processedRequestSubscription(nullptr)
{
}

void Pqe::refreshProcessedRequestSubscription() {
    using namespace std::placeholders;

    if (m_processedRequestSubscription != nullptr) {
        SubWrapper::unwrap(m_processedRequestSubscription);
        sslog_sbcr_unsubscribe(m_processedRequestSubscription);
        sslog_free_subscription(m_processedRequestSubscription);
    }

    m_processedRequestSubscription = sslog_new_subscription(true);
    list_t* properties = list_get_new_list();
    list_add_data(PROPERTY_GENERATES, properties);

    // TODO: not sure if is properties list requires to be unique on each call to sslog_sbcr_add_individual
    for (CaptGeneratorDesc captGeneratorDesc : m_captGenerators.values()) {
        sslog_sbcr_add_individual(m_processedRequestSubscription, captGeneratorDesc.getIndividual(), properties);
    }

    // list_free_with_nodes(properties, NULL);

    SubWrapper::wrap(m_processedRequestSubscription, std::bind(&Pqe::processAsyncProcessedRequestSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_processedRequestSubscription, &SubWrapper::handler);

    if (sslog_sbcr_subscribe(m_processedRequestSubscription) != 0) {
        throw std::runtime_error(get_error_text());
    }
}

void Pqe::executePreferenceQuery(PendingRequest pendingRequest) {
    qDebug() << "Executing preference query: " << pendingRequest.getUserRequest()->uuid;
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

    // Subscribe to processed request
    /*
     * m_processedRequestSubscription = sslog_new_subscription(true);
    individual_t* emptyIndividual = sslog_new_individual(CLASS_CONTEXTAWAREGENERATOR);

    list_t* properties = list_get_new_list();
    list_add_data(PROPERTY_GENERATES, properties);
    sslog_sbcr_add_individual(m_processedRequestSubscription, emptyIndividual, properties);

    */

    connect(this, &Pqe::captGeneratorAdded, this, &Pqe::addCaptGenerator);
    connect(this, &Pqe::captGeneratorRemoved, this, &Pqe::removeCaptGenerator);
    connect(this, &Pqe::userRequestAdded, this, &Pqe::processUserRequest);
    connect(this, &Pqe::processedRequestAdded, this, &Pqe::processProcessedRequest);

    sslog_sbcr_subscribe(m_captGeneratorSubscription);
    sslog_sbcr_subscribe(m_userRequestSubscription);
    //sslog_sbcr_subscribe(m_processedRequestSubscription);
}

void Pqe::addCaptGenerator(QString uuid) {
    individual_t* captGenerator = sslog_new_individual(CLASS_CONTEXTAWAREGENERATOR);
    sslog_set_individual_uuid(captGenerator, uuid.toStdString().c_str());
    sslog_ss_populate_individual(captGenerator);

    QString objectType = Common::getProperty(captGenerator, PROPERTY_OBJECTTYPE).toString();

    if (m_captGenerators.contains(objectType)) {
        qDebug() << "CAPTGenerator with uuid " << uuid << " already exists";
        sslog_free_individual(captGenerator);
        return;
    }

    m_objectTypes.insert(objectType, uuid);
    m_captGenerators.insert(uuid, CaptGeneratorDesc(captGenerator, objectType));

    refreshProcessedRequestSubscription();

    qDebug() << "CAPTGenerator added: " << uuid << " objectType: " << objectType;
}

void Pqe::removeCaptGenerator(QString uuid) {
    qDebug() << "CAPTGenerator removed: " << uuid;
    CaptGeneratorDesc captGenerator = m_captGenerators.take(uuid);
    sslog_free_individual(captGenerator.getIndividual());

    m_objectTypes.remove(captGenerator.getObjectType(), uuid);
}

void Pqe::processUserRequest(QString userRequuestUuid) {
    individual_t* userRequest = sslog_new_individual(CLASS_USERREQUEST);
    sslog_set_individual_uuid(userRequest, userRequuestUuid.toStdString().c_str());
    sslog_ss_populate_individual(userRequest);

    QString objectType = Common::getProperty(userRequest, PROPERTY_OBJECTTYPE).toString();

    qDebug() << "UserRequest added: " << userRequuestUuid << " objectType: " << objectType;


    QSet<QString> pendingCaptGeneratorIds = m_objectTypes.values(objectType).toSet();

    if (pendingCaptGeneratorIds.isEmpty()) {
        qDebug() << "No generators for this object type";
    }

    for (QString str : pendingCaptGeneratorIds) {
        qDebug() << "Waiting generator: " << str;
    }

    PendingRequest pendingRequest(userRequest, objectType, pendingCaptGeneratorIds);

    m_pendingRequests.insert(userRequuestUuid, pendingRequest);
}

void Pqe::processProcessedRequest(QString captGeneratorUuid, QString processedRequestUuid) {
    qDebug() << "ProcessedRequest added: " << captGeneratorUuid << processedRequestUuid;

    individual_t* processedRequestIndividual = sslog_new_individual(CLASS_PROCESSEDREQUEST);
    sslog_set_individual_uuid(processedRequestIndividual, processedRequestUuid.toStdString().c_str());
    sslog_ss_populate_individual(processedRequestIndividual);

    individual_t* userRequestIndividual = Common::getIndividualProperty(processedRequestIndividual,
                                                                        PROPERTY_ISASSOCIATEDWITH);

    QString userRequestUuid = userRequestIndividual->uuid;

    if (m_pendingRequests.contains(userRequestUuid)) {
        // User request already received
        PendingRequest pendingRequest = m_pendingRequests.value(userRequestUuid);
        if (!pendingRequest.removePendingCaptGenerator(captGeneratorUuid)) {
            qDebug() << "Received processed request from not registered CAPTGenerator: " << captGeneratorUuid;
            throw std::runtime_error("Received processed request from not registered CAPTGenerator");
        }

        if (!pendingRequest.hasPendingCaptGenerators()) {
            m_pendingRequests.remove(userRequestUuid);
            executePreferenceQuery(pendingRequest);
        }
    } else {
        // User request has not yet received
        throw std::runtime_error("NIY: User request has not yet received");
    }

    sslog_free_individual(userRequestIndividual);
    sslog_free_individual(processedRequestIndividual);
   // if (m_pendingRequests.contains())

}

void Pqe::processAsyncCaptGeneratorSubscription(subscription_t* subscription) {
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

void Pqe::processAsyncUserRequestSubscription(subscription_t* subscription) {
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

void Pqe::processAsyncProcessedRequestSubscription(subscription_t* subscription) {
    qDebug() << "processAsyncProcessedRequestSubscription";

    auto changes = sslog_sbcr_get_changes_last(subscription);
    auto properties = sslog_sbcr_ch_get_property_by_action(changes, ACTION_INSERT);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &properties->links) {
        property_changes_data_s* propertyChange = (property_changes_data_s*) (list_entry(listHead, list_t, links)->data);

        const char* captGeneratorUuid = propertyChange->owner_uuid;
        const char* processedRequestUuid = propertyChange->current_value;
        emit processedRequestAdded(captGeneratorUuid, processedRequestUuid);
    }
}