#ifndef CAPTGENERATOR_H
#define CAPTGENERATOR_H

#include "captgenerator_global.h"

#include <QObject>
#include <QMap>
#include <QMutex>


class individual_s;
typedef individual_s individual_t;

class subscription_s {};
typedef subscription_s subscription_t;

class CAPTGENERATORSHARED_EXPORT CAPTGenerator : public QObject
{
    Q_OBJECT

    QString m_name;
    QString m_objectType;

    std::default_random_engine m_randomEngine;
    std::uniform_int_distribution<int> m_idDistribution;

    individual_t* m_selfIndividual;
    subscription_t* m_subscription;

    bool m_isSubscribed;
    bool m_isPublished;

    static QMutex s_generatorsLock;
    static QMap<subscription_t*, CAPTGenerator*> s_generators;

public:
    CAPTGenerator(QString name, QString objectType);

public slots:
    void publish();
    void unpublish();
    void subscribe();
    void unsubscribe();

    void processSubscriptionChange(subscription_t* subscription);

signals:
    void subscriptionChanged(subscription_t* subscription, QString userRequestUuid);

private:
    void randomize();
    QString generateId();
    void setGeneratedId(individual_t* individual);

    void registerStaticSubscription(subscription_t* subscription);
    static void unregisterStaticSubsciption(subscription_t* subscription);
    static CAPTGenerator *getStaticGenerator(subscription_t* subscription);

    static void staticSubscriptionChangedHandler(subscription_t* subscription);
};

#endif // CAPTGENERATOR_H
