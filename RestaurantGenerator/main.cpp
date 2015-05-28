#include <QCoreApplication>

#include <QTimer>

#include "restorauntgenerator.h"

int main(int argc, char *argv[]) {
    QCoreApplication a(argc, argv);

    RestaurantGenerator* generator = new RestaurantGenerator(&a);

    QObject::connect(generator, SIGNAL(finished()), &a, SLOT(quit()));

    QTimer::singleShot(0, generator, SLOT(run()));

    return a.exec();
}
