#ifndef USERREQUEST_H
#define USERREQUEST_H

#include <QSharedDataPointer>
#include <QVariant>


class UserRequestData;

class UserRequest
{
public:
    UserRequest();
    UserRequest(QString userRequestUuid);
    UserRequest(const UserRequest &);
    UserRequest &operator=(const UserRequest &);
    ~UserRequest();

    QVariant getStaticContextProperty(const char *key);
    QVariant getDynamicContextProperty(const char *key);

private:
    QSharedDataPointer<UserRequestData> data;
};

Q_DECLARE_METATYPE(UserRequest)

#endif // USERREQUEST_H
