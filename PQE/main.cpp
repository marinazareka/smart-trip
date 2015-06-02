#include <QCoreApplication>

#include <QTimer>
#include <QDebug>

#include <functional>

#include "pqe.h"
#include "cfunction.hpp"

static void test(int a) {
     qDebug() << "test: " << a;
}

int main(int argc, char *argv[]) {
   /* using namespace std::placeholders;

    std::function<void(int)> test2 = [](int a) {
        qDebug() << "test2: " << a;
    };


    CFunction<int> functionWrapper;
    functionWrapper.wrap(4, &test);
    functionWrapper.wrap(5, std::bind([](int a, int b) {
         qDebug() << "test3: " << (a + b);
    }, 10, _1));

    functionWrapper.wrap(6, test2);

    CFunction<int>::handler(4);
    CFunction<int>::handler(5);
    CFunction<int>::handler(6);

    return 0;
*/

    Pqe::randomize();

    QCoreApplication a(argc, argv);

    Pqe* pqe = new Pqe(&a);
    QObject::connect(pqe, SIGNAL(finished()), &a, SLOT(quit()));
    QTimer::singleShot(0, pqe, SLOT(run()));

    return a.exec();
}
