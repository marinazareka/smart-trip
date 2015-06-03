#ifndef PROCESSEDREQUEST_H
#define PROCESSEDREQUEST_H

#include <QString>

#include <QSharedDataPointer>
#include <QVariant>

class ProcessedRequest
{
    QString m_userRequest;

public:
    ProcessedRequest(QString userRequestUuid);
    QString getUserRequestUuid() const;


};

#endif // PROCESSEDREQUEST_H
