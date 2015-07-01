#ifndef REQUESTHANDLER_H
#define REQUESTHANDLER_H

#include <QObject>
#include <QVariant>

#include "placemark.h"
#include "common.h"

class RequestHandlerData;
class Placemark;

class RequestHandler : public QObject {
    Q_OBJECT

    static constexpr int PAGE_SIZE = 10;


    RequestHandlerData* d;

public:
    RequestHandler(QString userRequestUuid,
                   QMultiMap<QString, QString> captGenerators,
                   QObject *parent = 0);
    ~RequestHandler();

    bool isReadyToExecute() const;

signals:

public slots:
    void execute();
    void onPointsLoaded(QVariant tag, QList<Placemark> points);
    void processPageRequest(individual_t* pageRequest);
};

#endif // REQUESTHANDLER_H
