#ifndef PQE_H
#define PQE_H

#include <QObject>
#include <QString>
#include <QMap>

#include <random>

#include "common.h"

class Pqe : public QObject {
    Q_OBJECT

    subscription_t* m_captGeneratorSubscription;
    subscription_t* m_userRequestSubscription;
    subscription_t* m_processedRequestSubscription;

    QMap<QString, individual_t*> captGenerator;

public:
    explicit Pqe(QObject* parent = 0);

    void refreshProcessedRequest();

signals:
    void finished();

    void captGeneratorAdded(QString uuid);
    void captGeneratorRemoved(QString uuid);

    void userRequestAdded(QString uuid);

    void processedRequestGenerated(QString uuid, individual_t* captGenerator);

public slots:
    void run();

    void subscribe();

    void addCaptGenerator(QString uuid);
    void removeCaptGenerator(QString uuid);
    void processUserRequest(QString uuid);

private:
    void processAsyncCaptGeneratorSubscription(subscription_t* subscription);
    void processAsyncUserRequestSubscription(subscription_t* subscription);
};

#endif // PQE_H
