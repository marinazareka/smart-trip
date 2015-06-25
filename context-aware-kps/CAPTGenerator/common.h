#ifndef COMMON_H
#define COMMON_H

#include <random>

#include <QString>
#include <QVariant>


class individual_s;
typedef individual_s individual_t;

class property_s;
typedef property_s property_t;

class subscription_s {};
typedef subscription_s subscription_t;

namespace Common {
    extern std::default_random_engine randomEngine;
    extern std::uniform_int_distribution<int> idDistribution;

    QString generateId(const char* prefix = nullptr);
    void setGeneratedId(individual_t* individual, const char* prefix = nullptr);
    void randomize(unsigned seed = 0);

    void initializeSmartspace(const char* kpName);
    void shutdownSmartspace();

    void setProperty(individual_t* individual, property_t* property, QVariant variant);

    QVariant getProperty(individual_t* individual, property_t*, bool load = false);
    individual_t* getIndividualProperty(individual_t* individual, property_t* property);
}

#endif // COMMON_H
