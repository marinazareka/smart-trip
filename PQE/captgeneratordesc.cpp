#include "captgeneratordesc.h"

CaptGeneratorDesc::CaptGeneratorDesc(individual_t* individual, QString objectType)
    : m_individual(individual), m_objectType(objectType) {
}

individual_t*CaptGeneratorDesc::getIndividual() const {
    return m_individual;
}

QString CaptGeneratorDesc::getObjectType() const {
    return m_objectType;
}

