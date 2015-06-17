#ifndef PQE_H
#define PQE_H

#include <QObject>
#include <QString>
#include <QMap>

#include <random>

#include "common.h"
#include "captgeneratordesc.h"
#include "pendingrequest.h"

class Pqe : public QObject {
    Q_OBJECT

    subscription_t* m_captGeneratorSubscription;
    subscription_t* m_userRequestSubscription;
    subscription_t* m_processedRequestSubscription;

    QMultiMap<QString /*objectType*/, QString /*uuid*/> m_objectTypes;
    QMap<QString /*uuid*/, CaptGeneratorDesc> m_captGenerators;

    // Processed requests for UserRequests than has not been received by PQE
    QList<QString> m_receivedProcessedRequests;

    QMap<QString /* userRequest */, PendingRequest> m_pendingRequests;


public:
    explicit Pqe(QObject* parent = 0);

    void refreshProcessedRequestSubscription();
    void executePreferenceQuery(PendingRequest pendingRequest);

signals:
    void finished();

    void captGeneratorAdded(QString uuid);
    void captGeneratorRemoved(QString uuid);

    void userRequestAdded(QString uuid);

    void processedRequestAdded(QString captGenerator, QString processedRequestUuid);

public slots:
    void run();

    void subscribe();

    void addCaptGenerator(QString uuid);
    void removeCaptGenerator(QString uuid);
    void processUserRequest(QString uuid);

    void processProcessedRequest(QString captGenerator, QString processedRequestUuid);

private:
    void processAsyncCaptGeneratorSubscription(subscription_t* subscription);
    void processAsyncUserRequestSubscription(subscription_t* subscription);
    void processAsyncProcessedRequestSubscription(subscription_t* subscription);
};

#endif // PQE_H
