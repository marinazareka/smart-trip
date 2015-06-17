#include "preferenceterm.h"

#include <stdexcept>

PreferenceTerm::PreferenceTerm() {

}

PreferenceTerm::~PreferenceTerm() {

}

QList<individual_t*> PreferenceTerm::convertToSslogIndividuals() {
    // TODO: make this method abstract
    throw std::runtime_error("TODO: make this method abstract");
}

