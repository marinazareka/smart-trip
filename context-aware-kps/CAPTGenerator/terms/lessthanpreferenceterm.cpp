#include "lessthanpreferenceterm.h"

#include "smartslog/generic.h"
#include "ontology/ontology.h"

#include "captgenerator.h"
#include "common.h"

LessThanPreferenceTerm::LessThanPreferenceTerm(QString property, float value)
    : m_property(property), m_value(value)
{

}

QList<individual_t*> LessThanPreferenceTerm::convertToSslogIndividuals() {
    individual_t* individual = sslog_new_individual(CLASS_LESSTHANPREFERENCETERM);
    Common::setGeneratedId(individual);

    sslog_add_property(individual, PROPERTY_PROPERTY, m_property.toStdString().c_str());
    sslog_add_property(individual, PROPERTY_VALUE, QString::number(m_value).toStdString().c_str());

    return QList<individual_t*>() << individual;
}

