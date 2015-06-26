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

class Pqe : public QObject {
    Q_OBJECT

    static constexpr int PAGE_SIZE = 10;

    subscription_t* m_captGeneratorSubscription;
    subscription_t* m_userRequestSubscription;
    subscription_t* m_processedRequestSubscription;
    subscription_t* m_pageRequestSubscription;

    Gets* m_gets;

    QMultiMap<QString /*objectType*/, QString /*uuid*/> m_objectTypes;
    QMap<QString /*uuid*/, CaptGeneratorDesc> m_captGenerators;

    // Processed requests for UserRequests than has not been received by PQE
    QList<QString> m_receivedProcessedRequests;

    // Results for userRequest awaiting page requests
    // TODO: results should be cleared sometime
    QMap<QString /* userRequest */, QList<Placemark>> m_results;

    QMap<QString /* userRequest */, PendingRequest> m_pendingRequests;

public:
    explicit Pqe(QObject* parent = 0);

    void refreshProcessedRequestSubscription();
    void executePreferenceQuery(individual_t* userRequest);

signals:
    void finished();

    void captGeneratorAdded(QString uuid);
    void captGeneratorRemoved(QString uuid);

    void userRequestAdded(QString uuid);
    void pageRequestAdded(QString uuid);

    void processedRequestAdded(QString captGenerator, QString processedRequestUuid);

public slots:
    void run();

    void subscribe();

    void addCaptGenerator(QString uuid);
    void removeCaptGenerator(QString uuid);
    void processUserRequest(QString uuid);
    void processPageRequest(QString uuid);

    void processProcessedRequest(QString captGenerator, QString processedRequestUuid);

    void onPointsLoaded(QList<Placemark> points);

private:
    void processAsyncCaptGeneratorSubscription(subscription_t* subscription);
    void processAsyncUserRequestSubscription(subscription_t* subscription);
    void processAsyncProcessedRequestSubscription(subscription_t* subscription);
    void processAsyncPageRequestSubscription(subscription_t* subscription);

    QList<Placemark> generatRandomPlacemarks(double lat, double lon, int n);
    QList<Placemark> loadPlacemarks(double lat, double lon, double radius);
};

#endif // PQE_H
