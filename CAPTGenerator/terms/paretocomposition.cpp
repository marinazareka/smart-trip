#include "paretocomposition.h"

ParetoComposition::ParetoComposition(PreferenceTerm* term1, PreferenceTerm* term2)
    : m_term1(term1), m_term2(term2) {
}

ParetoComposition::~ParetoComposition()
{
    delete m_term1;
    delete m_term2;
}

