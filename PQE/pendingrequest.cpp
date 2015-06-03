#include "pendingrequest.h"

#include <QSharedData>

class Data : public QSharedData {
public:
    Data();
    ~Data();

    QString objectType;
    QSet<QString> pendingCaptGenerators;
    individual_t* userRequestIndividual;
};

PendingRequest::PendingRequest() {

}

PendingRequest::PendingRequest(const PendingRequest& other)
    : d(other.d) {

}

PendingRequest&PendingRequest::operator=(const PendingRequest& other) {
    if (this != &other)
        d.operator=(other.d);
    return *this;
}

PendingRequest::~PendingRequest() {
}

PendingRequest::PendingRequest(individual_t* userRequestIndividual, QString objectType, QSet<QString> pendingCaptGenerators)
    : d(new Data)
{
    d->objectType = objectType;
    d->pendingCaptGenerators = pendingCaptGenerators;
    d->userRequestIndividual = userRequestIndividual;
}

bool PendingRequest::removePendingCaptGenerator(QString captGeneratorUuid) {
    return d->pendingCaptGenerators.remove(captGeneratorUuid);
}

bool PendingRequest::hasPendingCaptGenerators() const {
    return !d->pendingCaptGenerators.isEmpty();
}

individual_t* PendingRequest::getUserRequest() const {
    return d->userRequestIndividual;
}



Data::Data() {

}

Data::~Data() {

}
