#include "common.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>

#include <glib.h>

#include "ontology.h"

#define BUFSIZE 500

static unsigned short rand_state[3];

void init_rand() {
    FILE* urandom = fopen("/dev/urandom", "r");
    if (urandom == NULL) {
        fprintf(stderr, "Can't read random seed from /dev/urandom\n");
        rand_state[0] = rand_state[1] = rand_state[2] = time(NULL);
        return;
    }

    fread(rand_state, sizeof(unsigned short), 3, urandom);

    fclose(urandom);
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


sslog_individual_t* create_point_individual(sslog_node_t* node, double lat, double lon) {
    sslog_individual_t* point = sslog_new_individual(CLASS_POINT, rand_uuid("point"));
    sslog_individual_t* location = sslog_new_individual(CLASS_LOCATION, rand_uuid("location"));
    
    sslog_insert_property(location, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(location, PROPERTY_LONG, double_to_string(lon));

    sslog_insert_property(point, PROPERTY_HASLOCATION, location);

    sslog_node_insert_individual(node, location);
    sslog_node_insert_individual(node, point);

    return point;
}

bool get_point_coordinates(sslog_node_t* node, sslog_individual_t* point, double* out_lat, double* out_lon) {
   sslog_individual_t* location = (sslog_individual_t*) sslog_node_get_property(node, point, PROPERTY_HASLOCATION); 
   if (location == NULL) {
       return false;
   }

   sslog_node_populate(node, location);
   *out_lat = parse_double((const char*) sslog_get_property(location, PROPERTY_LAT));
   *out_lon = parse_double((const char*) sslog_get_property(location, PROPERTY_LONG));

   return true;
}

sslog_node_t* create_node(const char* kp_name, const char* config) {
    GKeyFile* keyfile = g_key_file_new();

    if (!g_key_file_load_from_file(keyfile, config, G_KEY_FILE_NONE, NULL)) {
       fprintf(stderr, "Can't load settings file %s\n", config);
       return NULL;
    }

    char* address = g_key_file_get_string(keyfile, "SIB", "Address", NULL);
    char* name = g_key_file_get_string(keyfile, "SIB", "Name", NULL);
    int port = (int) g_key_file_get_integer(keyfile, "SIB", "Port", NULL);

    sslog_node_t* ret = sslog_new_node(kp_name, name, address, port);

    g_free(address);
    g_free(name);
    g_key_file_free(keyfile);

    return ret;
}
 
void ptr_array_init(PtrArray* array) {
    array->size = 0;
    array->capacity = 1;
    array->array = malloc(sizeof(void*));
}

void ptr_array_insert(PtrArray* array, void* ptr) {
    if (array->size == array->capacity) {
        array->capacity *= 2;
        array->array = realloc(array->array, array->capacity * sizeof(void*));
        if (array->array == NULL) {
            abort();
        }
    }

    array->array[array->size++] = ptr;
}

void* ptr_array_remove_last(PtrArray* array) {
    assert(array->size > 0);
    return array->array[--array->size];
}

void ptr_array_free(PtrArray* array) {
    free(array->array);
}
