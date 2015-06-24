#ifndef UTIL_SMART_H_
#define UTIL_SMART_H_

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <inttypes.h>

void randomize_time();
void randomize(uint32_t seed_);
void generateId(char* outId, int size);

#endif
