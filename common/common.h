#ifndef COMMON_H_
#define COMMON_H_

#include <smartslog.h>
#include <pthread.h>

void cleanup_individual(sslog_individual_t** individual);
#define CLEANUP_INDIVIDUAL __attribute__((cleanup(cleanup_individual)))

void scoped_pthread_mutex_unlock_ptr(pthread_mutex_t** mutex);

#define SCOPED_MUTEX_LOCK(mutex) \
    __attribute__((cleanup(scoped_pthread_mutex_unlock_ptr))) \
    volatile pthread_mutex_t* mutex##_tmp = &mutex; \
    pthread_mutex_lock(mutex##_tmp);

extern unsigned short rand_state[3];

char* rand_uuid(const char* prefix);
char* rand_uuid_buf(const char* prefix, char* buf, size_t buf_size);

char* double_to_string(double value);
char* long_to_string(long value); 
void init_rand();

double parse_double(const char* string_double);

/**
 * Create and insert new point
 */
sslog_individual_t* create_point_individual(sslog_node_t* node,  double lat, double lon);

/**
 * Create and insert new point with title and category
 */
sslog_individual_t* create_poi_individual(sslog_node_t* node, double lat, double lon, const char* title, const char* category);


/**
 * Load point lat and lon
 */
bool get_point_coordinates(sslog_node_t* node, sslog_individual_t* point, double* out_lat, double* out_lon);

sslog_node_t* create_node(const char* kp_name, const char* config);
sslog_node_t* create_node_resolve(const char* name, const char* smartspace, const char* address, int port);

char* get_config_value(const char* config, const char* group, const char* key);

typedef struct  {
    size_t capacity;
    size_t size;
    void** array;
} PtrArray;

void ptr_array_init(PtrArray* array);
void ptr_array_insert(PtrArray* array, void* ptr);
void* ptr_array_remove_last(PtrArray* array);
void ptr_array_free(PtrArray* array);


typedef struct {
    size_t capacity;
    size_t size;
    size_t elem_size;
    void* array;
} FlatArray;

void flat_array_init(FlatArray* array, size_t elem_size);
void* flat_array_insert(FlatArray* array);
void flat_array_free(FlatArray* array);

sslog_individual_t* st_get_subject_by_object(sslog_node_t* node, const char* object_id, sslog_property_t* property);

bool remove_and_insert_property(sslog_node_t* node, sslog_individual_t* ind,
                                       sslog_property_t* prop, void* value);

#endif
