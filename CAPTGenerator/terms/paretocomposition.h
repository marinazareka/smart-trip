#ifndef PARETOCOMPOSITION_H
#define PARETOCOMPOSITION_H

#include "preferenceterm.h"

class ParetoComposition : public PreferenceTerm
{
    PreferenceTerm* m_term1;
    PreferenceTerm* m_term2;

public:
    ParetoComposition(PreferenceTerm* term1, PreferenceTerm* term2);
    ~ParetoComposition() override;
};

#endif // PARETOCOMPOSITION_H
