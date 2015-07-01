#ifndef PQE_H
#define PQE_H

#include <QObject>
#include <QString>
#include <QMap>

#include <random>

#include "common.h"
#include "captgeneratordesc.h"
#include "pendingrequest.h"
#include "placemark.h"
#include "gets.h"

class RequestHandler;

class Pqe : public QObject {
    Q_OBJECT

    subscription_t* m_captGeneratorSubscription;
    subscription_t* m_userRequestSubscription;
    subscription_t* m_processedRequestSubscription;
    subscription_t* m_pageRequestSubscription;

    QMultiMap<QString /*objectType*/, QString /*capt generator uuid*/> m_objectTypes;
    QMap<QString /*uuid*/, CaptGeneratorDesc> m_captGenerators;

    // Processed requests for UserRequests than has not been received by PQE
    QList<QString> m_receivedProcessedRequests;

    QMap<QString, RequestHandler*> m_requestHandlers;

public:
    explicit Pqe(QObject* parent = 0);

    void refreshProcessedRequestSubscription();

signals:
    void finished();

    void captGeneratorAdded(QString uuid);
    void captGeneratorRemoved(QString uuid);

    void userRequestAdded(QString uuid);
    void userRequestRemoved(QString uuid);

    void pageRequestAdded(QString uuid);

    void processedRequestAdded(QString captGenerator, QString processedRequestUuid);

public slots:
    void run();

    void subscribe();

    void addCaptGenerator(QString uuid);
    void removeCaptGenerator(QString uuid);

    void processUserRequest(QString uuid);
    void completeUserRequest(QString uuid);

    void processPageRequest(QString uuid);

    void processProcessedRequest(QString captGenerator, QString processedRequestUuid);


private:
    void processAsyncCaptGeneratorSubscription(subscription_t* subscription);
    void processAsyncUserRequestSubscription(subscription_t* subscription);
    void processAsyncProcessedRequestSubscription(subscription_t* subscription);
    void processAsyncPageRequestSubscription(subscription_t* subscription);

    QList<Placemark> generatRandomPlacemarks(double lat, double lon, int n);
};

#endif // PQE_H
