#ifndef CAPTGENERATORDESC_H
#define CAPTGENERATORDESC_H

#include "common.h"

class CaptGeneratorDesc
{
    individual_t* m_individual;
    QString m_objectType;

public:
    CaptGeneratorDesc() = default;
    CaptGeneratorDesc(individual_t* individual, QString objectType);

    individual_t* getIndividual() const;
    QString getObjectType() const;
};

#endif // CAPTGENERATORDESC_H
