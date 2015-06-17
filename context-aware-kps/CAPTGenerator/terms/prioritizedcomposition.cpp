#include "prioritizedcomposition.h"

PrioritizedComposition::PrioritizedComposition(PreferenceTerm *term1, PreferenceTerm *term2)
    : m_term1(term1), m_term2(term2) {

}

PrioritizedComposition::~PrioritizedComposition() {
    delete m_term1;
    delete m_term2;
}

