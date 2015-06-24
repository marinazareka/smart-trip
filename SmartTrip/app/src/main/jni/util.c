#include "util.h"

#include <time.h>

static uint32_t y = 1;

static uint32_t nextRandom() {
    y ^= (y << 13);
    y ^= (y >> 17);
    return (y ^= (y << 5));
}

void randomize_time() {
    randomize(time(NULL));
}

void randomize(uint32_t seed_) {
    y = seed_;
}

void generateId(char* outId, int size) {
    uint32_t r = nextRandom();
    snprintf(outId, size, "id%" PRIu32, r);
}