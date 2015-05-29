#ifndef LOWESTPREFERENCETERM_H
#define LOWESTPREFERENCETERM_H

#include <QString>

#include "attributepreferenceterm.h"

class LowestPreferenceTerm : public AttributePreferenceTerm
{
    QString m_property;

public:
    LowestPreferenceTerm(QString property);
};

#endif // LOWESTPREFERENCETERM_H
