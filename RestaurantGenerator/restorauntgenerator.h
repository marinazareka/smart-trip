#ifndef RESTORAUNTGENERATOR_H
#define RESTORAUNTGENERATOR_H

#include <QObject>

#include <userrequest.h>

class CAPTGenerator;


class subscription_s;
typedef subscription_s subscription_t;

class individual_s;
typedef individual_s individual_t;

class property_s;
typedef property_s property_t;


class RestaurantGenerator : public QObject
{
    Q_OBJECT

    CAPTGenerator* m_captGenerator;

public:
    explicit RestaurantGenerator(QObject *parent = 0);

signals:
    void finished();

public slots:
    void run();
    void processNewRequest(UserRequest userRequest);
    void shutdown();

private:


    QString getStringProperty(individual_t* individual, property_t* property);
    float getFloatProperty(individual_t* individual, property_t* property);
};

#endif // RESTORAUNTGENERATOR_H
