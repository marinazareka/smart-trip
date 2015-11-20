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

/**
 * Create and insert new point
 */
sslog_individual_t* create_point_individual(sslog_node_t* node,  double lat, double lon);

/**
 * Load point lat and lon
 */
bool get_point_coordinates(sslog_node_t* node, sslog_individual_t* point, double* out_lat, double* out_lon);

sslog_node_t* create_node(const char* kp_name, const char* config);
sslog_node_t* create_node_resolve(const char* name, const char* smartspace, const char* address, int port);

typedef struct  {
    size_t capacity;
    size_t size;
    void** array;
} PtrArray;

void ptr_array_init(PtrArray* array);
void ptr_array_insert(PtrArray* array, void* ptr);
void* ptr_array_remove_last(PtrArray* array);
void ptr_array_free(PtrArray* array);

#endif
