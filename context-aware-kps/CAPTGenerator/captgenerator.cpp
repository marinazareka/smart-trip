#include "captgenerator.h"

#include <QTimer>
#include <QDebug>

#include <chrono>
#include <random>

#include "ontology/ontology.h"

#include <smartslog/generic.h>

#include "userrequest.h"
#include "terms/preferenceterm.h"

Q_DECLARE_METATYPE(subscription_t*)
Q_DECLARE_METATYPE(individual_t*)

CAPTGenerator::CAPTGenerator(QString name, QString objectType)
    : m_name(name), m_objectType(objectType),
      m_isSubscribed(false), m_isPublished(false) {
    qRegisterMetaType<subscription_t*>();
    qRegisterMetaType<individual_t*>();

}

void CAPTGenerator::publish() {
    if (m_isPublished) {
        throw std::runtime_error("CAPTGenerator already published");
    }

    m_selfIndividual = sslog_new_individual(CLASS_CONTEXTAWAREGENERATOR);
    Common::setGeneratedId(m_selfIndividual);

    sslog_add_property(m_selfIndividual, PROPERTY_OBJECTTYPE, m_objectType.toStdString().c_str());
    int res = sslog_ss_insert_individual(m_selfIndividual);

    m_isPublished = true;

    qDebug() << "Published CAPTGenerator uuid = " << m_selfIndividual->uuid << " res = " << res;
}

void CAPTGenerator::unpublish() {
    if (!m_isPublished) {
        throw std::runtime_error("Generator not published");
    }

    if (m_isSubscribed) {
        throw std::runtime_error("Can't unpublish subscribed generator");
    }

    sslog_ss_remove_individual(m_selfIndividual);
    m_isPublished = false;
}

void CAPTGenerator::subscribe() {
    if (!m_isPublished) {
        throw std::runtime_error("Generator not published");
    }

    if (m_isSubscribed) {
        throw std::runtime_error("Generator already subscribed");
    }

    m_subscription = sslog_new_subscription(false);
    sslog_sbcr_add_class(m_subscription, CLASS_USERREQUEST);

    //sslog_sbcr_set_changed_handler(m_subscription, &staticSubscriptionChangedHandler);

    //registerStaticSubscription(m_subscription);

    /*connect(this, SIGNAL(subscriptionChanged(subscription_t*)),
            this, SLOT(processSubscriptionChange(subscription_t*)),
                       Qt::QueuedConnection);*/

    m_isSubscribed = true;
    sslog_sbcr_subscribe(m_subscription);
}

void CAPTGenerator::unsubscribe() {
    if (!m_isSubscribed) {
        throw std::runtime_error("Generator not subscribed");
    }

    m_isSubscribed = false;

    //unregisterStaticSubsciption(m_subscription);
    sslog_sbcr_stop(m_subscription);

    sslog_sbcr_unsubscribe(m_subscription);
    sslog_free_subscription(m_subscription);
}

void CAPTGenerator::waitSubscription() {

    if (sslog_sbcr_wait(m_subscription) == 0) {
        processSubscriptionChange(m_subscription);
        emit userRequestProcessed();
    }
}

void CAPTGenerator::processSubscriptionChange(subscription_t *subscription) {
    list_t* changes = sslog_sbcr_ch_get_individual_all(sslog_sbcr_get_changes_last(subscription));
    //list_t* userRequests = sslog_ss_get_individual_by_class_all(CLASS_USERREQUEST);

    list_head_t* listHead = NULL;
    list_for_each(listHead, &changes->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);


        if (m_processedRequests.contains(uuid)) {
            continue;
            throw std::runtime_error("Request already processed");
        }

        m_processedRequests.insert(uuid);

        UserRequest userRequest(uuid);

        if (userRequest.getObjectType() == m_objectType) {
            qDebug() << "Found inserted UserRequest with uuid " << uuid;
            emit userRequestReceived(userRequest);
        } else {
            qDebug() << "Request objectType not equials generator objectType: " << userRequest.getObjectType() << m_objectType;
        }
    }

    list_free_with_nodes(changes, NULL);
}

void CAPTGenerator::publishProcessedRequest(UserRequest userRequest, PreferenceTerm* preferenceTerm) {
    if (!m_isPublished) {
        throw std::runtime_error("Generator not published");
    }

    individual_t* processedRequest = sslog_new_individual(CLASS_PROCESSEDREQUEST);
    Common::setGeneratedId(processedRequest, "processedrequest");

    QList<individual_t*> termIndividuals;

    if (preferenceTerm != nullptr) {
        // TODO: may be the sslog is able to automatically insert non-root individuals?
        termIndividuals = preferenceTerm->convertToSslogIndividuals();
        for (individual_t* term : termIndividuals) {
           sslog_ss_insert_individual(term);
        }

        Q_ASSERT(!termIndividuals.isEmpty());

        individual_t* rootTerm = termIndividuals.first();

        sslog_add_property(processedRequest, PROPERTY_RESULTSIN, rootTerm);
    }

    sslog_add_property(processedRequest, PROPERTY_ISASSOCIATEDWITH, userRequest.getUserRequestIndividual());

    qDebug() << "Publishing processed request: " << processedRequest->uuid;
    sslog_ss_insert_individual(processedRequest);

    sslog_ss_add_property(m_selfIndividual, PROPERTY_GENERATES, processedRequest);

    for (individual_t* term : termIndividuals) {
        sslog_free_individual(term);
    }

    sslog_free_individual(processedRequest);
}
