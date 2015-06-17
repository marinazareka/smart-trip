#ifndef PRIORITIZEDCOMPOSITION_H
#define PRIORITIZEDCOMPOSITION_H

#include "preferenceterm.h"

class PrioritizedComposition : public PreferenceTerm
{
    PreferenceTerm* m_term1;
    PreferenceTerm* m_term2;

public:
    PrioritizedComposition(PreferenceTerm* term1, PreferenceTerm* term2);
    ~PrioritizedComposition() override;
};

#endif // PRIORITIZEDCOMPOSITION_H
