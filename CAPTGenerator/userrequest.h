#ifndef USERREQUEST_H
#define USERREQUEST_H

#include <QSharedDataPointer>
#include <QVariant>


class UserRequestData;

class individual_s;
typedef individual_s individual_t;

class UserRequest
{
    friend class CAPTGenerator;

public:
    UserRequest();
    UserRequest(QString userRequestUuid);
    UserRequest(const UserRequest &);
    UserRequest &operator=(const UserRequest &);
    ~UserRequest();

    QVariant getStaticContextProperty(const char *key);
    QVariant getDynamicContextProperty(const char *key);

private:
    individual_t* getUserRequestIndividual() const;

private:
    QExplicitlySharedDataPointer<UserRequestData> data;
};

Q_DECLARE_METATYPE(UserRequest)

#endif // USERREQUEST_H
