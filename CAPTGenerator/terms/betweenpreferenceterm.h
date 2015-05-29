#ifndef BETWEENPREFERENCETERM_H
#define BETWEENPREFERENCETERM_H

#include <QString>

#include "attributepreferenceterm.h"

class BetweenPreferenceTerm : public AttributePreferenceTerm
{
    float m_lower;
    float m_upper;
    QString m_property;

public:
    BetweenPreferenceTerm(QString property, float lower, float upper);
};

#endif // BETWEENPREFERENCETERM_H
