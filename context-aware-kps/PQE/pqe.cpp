#include "pqe.h"

#include <chrono>
#include <functional>
#include <algorithm>

#include <QDebug>

#include "ontology/ontology.h"
#include <smartslog/generic.h>

#include "captgenerator.h"
#include "cfunction.hpp"
#include "captgeneratordesc.h"

#include "unistd.h"

typedef CFunction<subscription_t*> SubWrapper;

Pqe::Pqe(QObject *parent) : QObject(parent),
      m_captGeneratorSubscription(nullptr), m_userRequestSubscription(nullptr),
      m_processedRequestSubscription(nullptr), m_pageRequestSubscription(nullptr)
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

void Pqe::executePreferenceQuery(individual_t* userRequest) {
    qDebug() << "Executing preference query: " << userRequest->uuid;

    individual_t* dynamicContext = Common::getIndividualProperty(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT);

    sslog_ss_populate_individual(dynamicContext);
    double lat = Common::getProperty(dynamicContext, PROPERTY_LAT, false).toDouble();
    double lon = Common::getProperty(dynamicContext, PROPERTY_LON, false).toDouble();

    QList<Placemark> resultList = generatRandomPlacemarks(lat, lon, 100);

    m_results.insert(userRequest->uuid, resultList);

    qDebug() << "Context coordinates: " << lat << lon;
    sslog_ss_add_property(userRequest, PROPERTY_PROCESSED, const_cast<char*>("true"));
}

void Pqe::run() {
    Common::initializeSmartspace("Preference query executor");
    subscribe();
}

void Pqe::subscribe() {
    using namespace std::placeholders;

    // Subscribe to CAPTGenerators
    qDebug("Subscribing to CAPTGenerators");
    m_captGeneratorSubscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_captGeneratorSubscription, CLASS_CONTEXTAWAREGENERATOR);
    SubWrapper::wrap(m_captGeneratorSubscription, std::bind(&Pqe::processAsyncCaptGeneratorSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_captGeneratorSubscription, &SubWrapper::handler);

    // Subscribe to User requests
    qDebug("Subscribing to UserRequests");
    m_userRequestSubscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_userRequestSubscription, CLASS_USERREQUEST);
    SubWrapper::wrap(m_userRequestSubscription, std::bind(&Pqe::processAsyncUserRequestSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_userRequestSubscription, &SubWrapper::handler);

    // Subscribe to Page requests
    qDebug("Subscribing to PageRequests");
    m_pageRequestSubscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_pageRequestSubscription, CLASS_PAGEREQUEST);
    SubWrapper::wrap(m_pageRequestSubscription, std::bind(&Pqe::processAsyncPageRequestSubscription, this, _1));
    sslog_sbcr_set_changed_handler(m_pageRequestSubscription, &SubWrapper::handler);


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
    connect(this, &Pqe::pageRequestAdded, this, &Pqe::processPageRequest);

    sslog_sbcr_subscribe(m_captGeneratorSubscription);
    sslog_sbcr_subscribe(m_userRequestSubscription);
    sslog_sbcr_subscribe(m_pageRequestSubscription);
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
        qDebug() << "No generators for this object type, executing immediately";
        executePreferenceQuery(userRequest);
        return;
    }

    for (QString str : pendingCaptGeneratorIds) {
        qDebug() << "Waiting generator: " << str;
    }

    PendingRequest pendingRequest(userRequest, objectType, pendingCaptGeneratorIds);

    m_pendingRequests.insert(userRequuestUuid, pendingRequest);
}

void Pqe::processPageRequest(QString uuid) {
    individual_t* pageRequest = sslog_new_individual(CLASS_PAGEREQUEST);
    sslog_set_individual_uuid(pageRequest, uuid.toStdString().c_str());
    sslog_ss_populate_individual(pageRequest);

    // TODO: all incorrect requests must be handled without crash
    individual_t* userRequest = Common::getIndividualProperty(pageRequest, PROPERTY_RELATESTO);
    QString userRequestUuid = userRequest->uuid;

    bool convertOk;
    int pageNumber = Common::getProperty(pageRequest, PROPERTY_PAGE, false).toInt(&convertOk);

    QList<Placemark> placemarks = m_results.value(userRequestUuid);

    if (!placemarks.isEmpty() && convertOk) {
        int fromIdx = PAGE_SIZE * pageNumber;
        int toIdx = qMin(PAGE_SIZE * (pageNumber + 1), placemarks.size());

        individual_t* page = sslog_new_individual(CLASS_PAGE);
        Common::setGeneratedId(page, "page");

        qDebug() << "Sending page " << pageNumber << "[" << fromIdx << ":" << toIdx << "]";
        for (int i = fromIdx; i < toIdx ; i++) {
            Placemark placemark = placemarks.at(i);

            individual_t* placemarkIndividual = sslog_new_individual(CLASS_PLACEMARK);
            Common::setGeneratedId(placemarkIndividual);
            Common::setProperty(placemarkIndividual, PROPERTY_LAT, placemark.getLat());
            Common::setProperty(placemarkIndividual, PROPERTY_LON, placemark.getLon());

            qDebug() << "Sending placemark "
                     << placemark.getLat() << placemark.getLon() << placemarkIndividual->uuid;


            int code = sslog_ss_insert_individual(placemarkIndividual);
            if (code != 0) {
                qDebug() << "sslog_ss_insert_individual" << code << get_error_text();
            }


            code = sslog_add_property(page, PROPERTY_CONSISTSIN, placemarkIndividual);
            if (code != 0) {
                qDebug() << "sslog_ss_insert_individual" << code << get_error_text();
            }

            // TODO: placemark individual must be freed
        }

        sslog_ss_insert_individual(page);
        sslog_ss_add_property(pageRequest, PROPERTY_RESULTSIN, page);
    }

    sslog_ss_add_property(pageRequest, PROPERTY_PROCESSED, const_cast<char*>("true"));
    sslog_free_individual(pageRequest);
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
            executePreferenceQuery(pendingRequest.getUserRequest());
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
    qDebug() << "processAsyncCaptGeneratorSubscription";

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
    qDebug() << "processAsyncUserRequestSubscription";

    auto changes = sslog_sbcr_get_changes_last(subscription);
    auto individuals = sslog_sbcr_ch_get_individual_by_action(changes, ACTION_INSERT);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &individuals->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);

        if (sslog_ss_exists_uuid(const_cast<char*>(uuid))) {
            qDebug() << "User request added " << uuid;
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

void Pqe::processAsyncPageRequestSubscription(subscription_t* subscription) {
    qDebug() << "processAsyncPageRequestSubscription";

    auto changes = sslog_sbcr_get_changes_last(subscription);
    auto individuals = sslog_sbcr_ch_get_individual_by_action(changes, ACTION_INSERT);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &individuals->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);

        if (sslog_ss_exists_uuid(const_cast<char*>(uuid))) {
            qDebug() << "Page request added " << uuid;
            emit pageRequestAdded(uuid);
        }
    }
}

QList<Placemark> Pqe::generatRandomPlacemarks(double lat, double lon, int n) {
    QList<Placemark> result;

    std::normal_distribution<> latDist(lat, 0.1);
    std::normal_distribution<> lonDist(lon, 0.1);

    for (int i = 0; i < n; i++) {
        result << Placemark(latDist(Common::randomEngine), lonDist(Common::randomEngine));
    }

    return result;
}
