#ifndef LESSTHANPREFERENCETERM_H
#define LESSTHANPREFERENCETERM_H

#include <QString>

#include "attributepreferenceterm.h"

class LessThanPreferenceTerm : public AttributePreferenceTerm
{
    QString m_property;
    float m_value;

public:
    LessThanPreferenceTerm(QString property, float value);

    QList<individual_t*> convertToSslogIndividuals() override;
};

#endif // LESSTHANPREFERENCETERM_H
