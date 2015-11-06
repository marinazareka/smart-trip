#include "common.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define BUFSIZE 500

static unsigned short rand_state[3];

void init_rand() {
    rand_state[0] = rand_state[1] = rand_state[2] = time(NULL);
}

char* rand_uuid(const char* prefix) {
	static char rand_uuid_buffer[BUFSIZE];
	sprintf(rand_uuid_buffer, "%s%ld", prefix, nrand48(rand_state));
	return rand_uuid_buffer;
}

// TODO: sNprintf
char* double_to_string(double value) {
	static char buffer[BUFSIZE];
	sprintf(buffer, "%lf", value);
	return buffer;
}   

char* long_to_string(long value) {
    static char buffer[BUFSIZE];
    sprintf(buffer, "%ld", value);
    return buffer;
}

double parse_double(const char* string_double) {
    double ret;
    if (sscanf(string_double, "%lf", &ret) < 1) {
        return 0.0;
    } else {
        return ret;
    }
}

void cleanup_individual(sslog_individual_t** individual) {
    if (*individual != NULL) {
        sslog_remove_individual(*individual);
    }

    *individual = NULL;
}
