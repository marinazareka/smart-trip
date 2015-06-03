#ifndef COMMON_H
#define COMMON_H

#include <random>

#include <QString>
#include <QVariant>


class individual_s;
typedef individual_s individual_t;

class subscription_s {};
typedef subscription_s subscription_t;

namespace Common {
    extern std::default_random_engine m_randomEngine;
    extern std::uniform_int_distribution<int> m_idDistribution;

    QString generateId(const char* prefix = nullptr);
    void setGeneratedId(individual_t* individual, const char* prefix = nullptr);
    void randomize(unsigned seed = 0);

    void initializeSmartspace(const char* kpName);
    void shutdownSmartspace();

    QVariant getProperty(individual_t* individual, const char* key);
    individual_t* getIndividualProperty(individual_t* individual, const char* key);
}

#endif // COMMON_H
