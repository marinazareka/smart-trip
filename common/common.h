#ifndef COMMON_H_
#define COMMON_H_

#include <smartslog.h>

void cleanup_individual(sslog_individual_t** individual);
#define CLEANUP_INDIVIDUAL __attribute__((cleanup(cleanup_individual)))

char* rand_uuid(const char* prefix);
char* double_to_string(double value);
char* long_to_string(long value); 
void init_rand();

double parse_double(const char* string_double);


#endif
