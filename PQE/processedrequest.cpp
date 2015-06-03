#include "processedrequest.h"

ProcessedRequest::ProcessedRequest(QString userRequestUuid)
    : m_userRequest(userRequestUuid)
{
}

QString ProcessedRequest::getUserRequestUuid() const {
    return m_userRequest;
}

