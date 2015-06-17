#ifndef PREFERENCETERM_H
#define PREFERENCETERM_H

#include <QList>

class individual_s;
typedef individual_s individual_t;

class PreferenceTerm
{
public:
    PreferenceTerm();
    virtual ~PreferenceTerm();

    /**
     * @brief convertToSslogIndividuals
     * @return list of individuals, first of that is preference tree root
     */
    virtual QList<individual_t*> convertToSslogIndividuals();
};

#endif // PREFERENCETERM_H
