#include <QCoreApplication>

#include <QTimer>

#include <signal.h>

#include "captgenerator.h"
#include "restorauntgenerator.h"
#include "common.h"

int main(int argc, char *argv[]) {
    Common::randomize();

    QCoreApplication a(argc, argv);

    RestaurantGenerator* generator = new RestaurantGenerator(&a);

    QObject::connect(generator, SIGNAL(finished()), &a, SLOT(quit()));

    QTimer::singleShot(0, generator, SLOT(run()));

    return a.exec();
}
