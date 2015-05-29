#include "captgenerator.h"

#include <QTimer>
#include <QDebug>

#include <chrono>
#include <random>

#include "ontology/ontology.h"

#include <smartslog/generic.h>

QMap<subscription_t*, CAPTGenerator*> CAPTGenerator::s_generators;
QMutex CAPTGenerator::s_generatorsLock;

Q_DECLARE_METATYPE(subscription_t*)
Q_DECLARE_METATYPE(individual_t*)

CAPTGenerator::CAPTGenerator(QString name, QString objectType)
    : m_name(name), m_objectType(objectType), m_idDistribution(1, 1000000000),
      m_isSubscribed(false), m_isPublished(false) {
    qRegisterMetaType<subscription_t*>();
    qRegisterMetaType<individual_t*>();

}

void CAPTGenerator::publish() {
    if (m_isPublished) {
        throw std::runtime_error("CAPTGenerator already published");
    }

    m_selfIndividual = sslog_new_individual(CLASS_CONTEXTAWAREGENERATOR);
    setGeneratedId(m_selfIndividual);

    sslog_add_property(m_selfIndividual, PROPERTY_OBJECTTYPE, m_objectType.toStdString().c_str());
    sslog_ss_insert_individual(m_selfIndividual);

    m_isPublished = true;
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

    m_subscription = sslog_new_subscription(true);
    sslog_sbcr_add_class(m_subscription, CLASS_USERREQUEST);

    sslog_sbcr_set_changed_handler(m_subscription, &staticSubscriptionChangedHandler);

    registerStaticSubscription(m_subscription);

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

    unregisterStaticSubsciption(m_subscription);
    sslog_sbcr_stop(m_subscription);

    sslog_sbcr_unsubscribe(m_subscription);
    sslog_free_subscription(m_subscription);
}

void CAPTGenerator::processSubscriptionChange(subscription_t *subscription) {
    list_t* changes = sslog_sbcr_ch_get_individual_all(sslog_sbcr_get_changes_last(subscription));

    list_head_t* listHead = NULL;
    list_for_each(listHead, &changes->links) {
        const char* uuid = (const char*) (list_entry(listHead, list_t, links)->data);

        individual_t* userRequest = sslog_new_individual(CLASS_USERREQUEST);
        sslog_set_individual_uuid(userRequest, uuid);

        prop_val_t* dynamicContextValue = sslog_ss_get_property(userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT);
        individual_t* dynamicContext = reinterpret_cast<individual_t*>(dynamicContextValue->prop_value);

        prop_val_t* relatesToValue = sslog_ss_get_property(userRequest, PROPERTY_RELATESTO);
        individual_t* user = reinterpret_cast<individual_t*>(relatesToValue->prop_value);

        prop_val_t* staticUserContextValue = sslog_ss_get_property(user, PROPERTY_HASSTATICUSERCONTEXT);
        individual_t* staticUserContext = reinterpret_cast<individual_t*>(staticUserContextValue->prop_value);

        QString userUuid = user->uuid;
        QString dynamicContextUuid = dynamicContext->uuid;
        QString userRequestUuid = uuid;
        QString staticContextUuid = staticUserContext->uuid;

        sslog_free_data_property_value_struct(dynamicContextValue);
        sslog_free_data_property_value_struct(staticUserContextValue);
        sslog_free_data_property_value_struct(relatesToValue);

        sslog_free_individual(dynamicContext);
        sslog_free_individual(user);
        sslog_free_individual(staticUserContext);
        sslog_free_individual(userRequest);

        if (userRequest != nullptr) {
            qDebug() << "Found inserted UserRequest with uuid " << uuid;
            emit subscriptionChanged(userUuid, dynamicContextUuid, staticContextUuid, userRequestUuid);
        }
    }

    list_free_with_nodes(changes, NULL);
}

void CAPTGenerator::initializeSmartspace() {
    sslog_ss_init_session_with_parameters("X", "127.0.0.1", 10622);
    register_ontology();

    qDebug("Joining KP");
    if (ss_join(sslog_get_ss_info(), const_cast<char*>(m_name.toStdString().c_str())) == -1) {
        qDebug("Can't join SS");
        throw std::runtime_error("Can't join SS");
    }
}

void CAPTGenerator::shutdownSmartspace() {
    qDebug("Shutting down KP");
    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

void CAPTGenerator::randomize() {
    unsigned seed = std::chrono::system_clock::now().time_since_epoch() / std::chrono::milliseconds(1);
    m_randomEngine.seed(seed);
}

QString CAPTGenerator::generateId() {
    QString ret = QStringLiteral("id") + m_idDistribution(m_randomEngine);
    return ret;
}

void CAPTGenerator::setGeneratedId(individual_t *individual) {
    QString generatedId = generateId();
    sslog_set_individual_uuid(individual, generatedId.toStdString().c_str());
}

void CAPTGenerator::registerStaticSubscription(subscription_t *subscription) {
    QMutexLocker lock(&s_generatorsLock);

    s_generators.insert(subscription, this);
}

// static
void CAPTGenerator::unregisterStaticSubsciption(subscription_t *subscription) {
    QMutexLocker lock(&s_generatorsLock);
    s_generators.remove(subscription);
}

// static
CAPTGenerator* CAPTGenerator::getStaticGenerator(subscription_t *subscription) {
    QMutexLocker lock(&s_generatorsLock);
    return s_generators.value(subscription, nullptr);
}

// static
void CAPTGenerator::staticSubscriptionChangedHandler(subscription_t *subscription){
    CAPTGenerator* generator = getStaticGenerator(subscription);
    if (generator != nullptr) {
        QMetaObject::invokeMethod(generator,
                                  "processSubscriptionChange",
                                  Qt::QueuedConnection,
                                  Q_ARG(subscription_t*, subscription));
    }
}
