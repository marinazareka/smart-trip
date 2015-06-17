#include "common.h"

#include <chrono>

#include <QDebug>

#include "smartslog/generic.h"
#include "ontology/ontology.h"

namespace Common {

std::default_random_engine m_randomEngine;
std::uniform_int_distribution<int> m_idDistribution(1, 1000000000);

void randomize(unsigned seed) {
    if (seed == 0) {
        seed = std::chrono::system_clock::now().time_since_epoch() / std::chrono::milliseconds(1);
    }

    m_randomEngine.seed(seed);
}

QString generateId(const char* prefix) {
    if (prefix != nullptr) {
        return QString("%1%2").arg(prefix).arg(m_idDistribution(m_randomEngine));
    } else {
        return QString("id%1").arg(m_idDistribution(m_randomEngine));
    }
}

void setGeneratedId(individual_t *individual, const char* prefix) {
    QString generatedId = generateId(prefix);
    sslog_set_individual_uuid(individual, generatedId.toStdString().c_str());
}

void initializeSmartspace(const char* kpName) {
    sslog_ss_init_session_with_parameters("X", "192.168.112.6", 10622);
    register_ontology();

    qDebug("Joining KP");
    if (ss_join(sslog_get_ss_info(), const_cast<char*>(kpName)) == -1) {
        qDebug("Can't join SS");
        throw std::runtime_error("Can't join SS");
    }
}

void shutdownSmartspace() {
    qDebug("Shutting down KP");
    sslog_repo_clean_all();
    sslog_ss_leave_session(sslog_get_ss_info());
}

QVariant getProperty(individual_t* individual, property_t* property, bool load) {
    const prop_val_t* value = load
            ? sslog_ss_get_property(individual, property)
            : sslog_get_property(individual, property->name);
    if (value == nullptr) {
        return QVariant();
    }

    const char* strValue = reinterpret_cast<const char*>(value->prop_value);
    return strValue;
}

individual_t* getIndividualProperty(individual_t* individual, property_t* property) {
    const prop_val_t* value = sslog_get_property(individual, property->name);
    if (value == nullptr) {
        return nullptr;
    }

    return reinterpret_cast<individual_t*>(value->prop_value);
}

}
