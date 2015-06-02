#ifndef CAPTGENERATOR_H
#define CAPTGENERATOR_H

#include "captgenerator_global.h"

#include <QObject>
#include <QMap>
#include <QMutex>
#include <QSet>

#include "userrequest.h"

class individual_s;
typedef individual_s individual_t;

class subscription_s {};
typedef subscription_s subscription_t;

class PreferenceTerm;

class CAPTGENERATORSHARED_EXPORT CAPTGenerator : public QObject
{
    Q_OBJECT

    QSet<QString> m_processedRequests;

    QString m_name;
    QString m_objectType;


    individual_t* m_selfIndividual;
    subscription_t* m_subscription;

    bool m_isSubscribed;
    bool m_isPublished;

    static std::default_random_engine m_randomEngine;
    static std::uniform_int_distribution<int> m_idDistribution;

    /*static QMutex s_generatorsLock;
    static QMap<subscription_t*, CAPTGenerator*> s_generators;*/

public:
    CAPTGenerator(QString name, QString objectType);

    static QString generateId();
    static void setGeneratedId(individual_t* individual);
    static void randomize(unsigned seed = 0);

public slots:
    void publish();
    void unpublish();
    void subscribe();
    void unsubscribe();

    void waitSubscription();

    void processSubscriptionChange(subscription_t* subscription);
    void publishProcessedRequest(UserRequest userRequest, PreferenceTerm* preferenceTerm);

    void initializeSmartspace();
    void shutdownSmartspace();

signals:
    void userRequestReceived(UserRequest userRequest);
    void userRequestProcessed();

private:

    bool checkRequestProcessed();

    /*void registerStaticSubscription(subscription_t* subscription);
    static void unregisterStaticSubsciption(subscription_t* subscription);
    static CAPTGenerator *getStaticGenerator(subscription_t* subscription);

    static void staticSubscriptionChangedHandler(subscription_t* subscription);*/
};

#endif // CAPTGENERATOR_H
