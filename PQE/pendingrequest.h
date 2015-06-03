#ifndef PENDINGREQUEST_H
#define PENDINGREQUEST_H

#include <QSet>
#include <QString>

#include "common.h"

class PendingRequest
{


public:
    PendingRequest(QString objectType, QSet<QString> pendingCaptGenerators);
};

#endif // PENDINGREQUEST_H
