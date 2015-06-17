#ifndef PENDINGREQUEST_H
#define PENDINGREQUEST_H

#include <QSet>
#include <QString>
#include <QSharedData>

#include "common.h"

class Data;

class PendingRequest {
public:
    PendingRequest();
    PendingRequest(const PendingRequest &);
    PendingRequest &operator=(const PendingRequest &);
    ~PendingRequest();

    PendingRequest(individual_t* userRequestIndividual, QString objectType, QSet<QString> pendingCaptGenerators);

    bool removePendingCaptGenerator(QString captGeneratorUuid);
    bool hasPendingCaptGenerators() const;
    individual_t* getUserRequest() const;

private:
    QExplicitlySharedDataPointer<Data> d;
};

#endif // PENDINGREQUEST_H
