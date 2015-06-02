#ifndef PQE_H
#define PQE_H

#include <QObject>
#include <QString>
#include <QMap>

#include <random>

class individual_s;
typedef individual_s individual_t;

class subscription_s {};
typedef subscription_s subscription_t;

class Pqe : public QObject {
    Q_OBJECT

    static std::default_random_engine m_randomEngine;
    static std::uniform_int_distribution<int> m_idDistribution;

    subscription_t* m_captGeneratorSubscription;
    subscription_t* m_userRequestSubscription;
    subscription_t* m_processedRequestSubscription;

    QMap<QString, individual_t*> captGenerator;

public:
    explicit Pqe(QObject* parent = 0);

    static QString generateId();
    static void setGeneratedId(individual_t* individual);
    static void randomize(unsigned seed = 0);

    void refreshProcessedRequest();

signals:
    void finished();

    void captGeneratorAdded(QString uuid);
    void captGeneratorRemoved(QString uuid);

    void userRequestAdded(QString uuid);

    void processedRequestGenerated(QString uuid, individual_t* captGenerator);

public slots:
    void run();

    void initializeSmartspace();
    void shutdownSmartspace();

    void subscribe();

    void addCaptGenerator(QString uuid);
    void removeCaptGenerator(QString uuid);
    void processUserRequest(QString uuid);

private:
    void processAsyncCaptGeneratorSubscription(subscription_t* subscription);
    void processAsyncUserRequestSubscription(subscription_t* subscription);
};

#endif // PQE_H
