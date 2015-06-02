#ifndef GENERATORSUBSCRIPTIONHANDLER_H
#define GENERATORSUBSCRIPTIONHANDLER_H

#include <QObject>

class individual_s;
typedef individual_s individual_t;

class GeneratorSubscriptionHandler : public QObject
{
    Q_OBJECT

public:
    explicit GeneratorSubscriptionHandler(individual_t* captGenerator, QObject *parent = 0);

signals:

public slots:
};

#endif // GENERATORSUBSCRIPTIONHANDLER_H
