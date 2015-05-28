#ifndef RESTORAUNTGENERATOR_H
#define RESTORAUNTGENERATOR_H

#include <QObject>

class CAPTGenerator;

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
    void test();

private:
    void initializeSmartspace();
    void shutdownSmartspace();
};

#endif // RESTORAUNTGENERATOR_H
