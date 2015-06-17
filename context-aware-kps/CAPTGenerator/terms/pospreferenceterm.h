#ifndef POSPREFERENCETERM_H
#define POSPREFERENCETERM_H

#include <QString>

#include "attributepreferenceterm.h"

class POSPreferenceTerm : public AttributePreferenceTerm
{
    QString m_property;
    QString m_value;

public:
    POSPreferenceTerm(QString property, QString value);
    ~POSPreferenceTerm() override;
};

#endif // POSPREFERENCETERM_H
