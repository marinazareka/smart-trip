#include "common.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>

#if !defined(NO_GLIB)
#include <glib.h>
#endif

#include <string.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#include "ontology.h"

#define BUFSIZE 500

unsigned short rand_state[3];

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

char* rand_uuid_buf(const char* prefix, char* buf, size_t buf_size) {
    snprintf(buf, buf_size, "%s%ld", prefix, nrand48(rand_state));
    return buf;
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

void scoped_pthread_mutex_unlock_ptr(pthread_mutex_t** mutex) {
    if (*mutex != NULL) {
        pthread_mutex_unlock(*mutex);
    }

    *mutex = NULL;
}

sslog_individual_t* create_poi_individual(sslog_node_t* node, double lat, double lon,
                                          const char* title, const char* category) {
    char buf[BUFSIZE];

    sslog_individual_t* point = sslog_new_individual(CLASS_POINT, rand_uuid_buf("point", buf, BUFSIZE));
    sslog_individual_t* location = sslog_new_individual(CLASS_LOCATION, rand_uuid_buf("location", buf, BUFSIZE));

    sslog_insert_property(location, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(location, PROPERTY_LONG, double_to_string(lon));

    sslog_insert_property(point, PROPERTY_HASLOCATION, location);
    sslog_insert_property(point, PROPERTY_POITITLE, (void*) title);
    sslog_insert_property(point, PROPERTY_POICATEGORY, (void*) category);

    sslog_node_insert_individual(node, location);
    sslog_node_insert_individual(node, point);

    return point;
}

sslog_individual_t* create_point_individual(sslog_node_t* node, double lat, double lon) {
    char buf[BUFSIZE];

    sslog_individual_t* point = sslog_new_individual(CLASS_POINT, rand_uuid_buf("point", buf, BUFSIZE));
    sslog_individual_t* location = sslog_new_individual(CLASS_LOCATION, rand_uuid_buf("location", buf, BUFSIZE));

    sslog_insert_property(location, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(location, PROPERTY_LONG, double_to_string(lon));

    sslog_insert_property(point, PROPERTY_HASLOCATION, location);

    sslog_node_insert_individual(node, location);
    sslog_node_insert_individual(node, point);

    return point;
}

// Get point coordinates
// Point must be populated
bool get_point_coordinates(sslog_node_t* node, sslog_individual_t* point, double* out_lat, double* out_lon) {
    sslog_individual_t* location = (sslog_individual_t*) sslog_get_property(point, PROPERTY_HASLOCATION);
    if (location == NULL) {
        return false;
    }

    if (sslog_node_populate(node, location) != SSLOG_ERROR_NO) {
        return false;
    }

    const char* lat_str = sslog_get_property(location, PROPERTY_LAT);
    const char* lon_str = sslog_get_property(location, PROPERTY_LONG);

    if (lat_str == NULL || lon_str == NULL) {
        return false;
    }

    *out_lat = parse_double(lat_str);
    *out_lon = parse_double(lon_str);

    return true;
}

#if !defined(NO_GLIB)
sslog_node_t* create_node(const char* kp_name, const char* config) {
    GKeyFile* keyfile = g_key_file_new();

    if (!g_key_file_load_from_dirs(keyfile, config, search_dirs, NULL, G_KEY_FILE_NONE, NULL)) {
       fprintf(stderr, "Can't load settings file %s\n", config);
       return NULL;
    }

    char* address = g_key_file_get_string(keyfile, "SIB", "Address", NULL);
    char* name = g_key_file_get_string(keyfile, "SIB", "Name", NULL);
    int port = (int) g_key_file_get_integer(keyfile, "SIB", "Port", NULL);

    sslog_node_t* ret = create_node_resolve(kp_name, name, address, port);

    g_free(address);
    g_free(name);
    g_key_file_free(keyfile);

    return ret;
}

char* get_config_value(const char* config, const char* group, const char* key) {
    GKeyFile* keyfile = g_key_file_new();
    char* ret = NULL;

    if (!g_key_file_load_from_dirs(keyfile, config, search_dirs, NULL, G_KEY_FILE_NONE, NULL)) {
       fprintf(stderr, "%s:%i: Can't load settings file %s\n", __FILE__, __LINE__, config);
       return NULL;
    }

    gchar* value = g_key_file_get_string(keyfile, group, key, NULL);
    if (value != NULL)  {
        ret = strdup(value);
        g_free(value);
    }

    g_key_file_free(keyfile);

    return ret;
}
#endif

sslog_node_t* create_node_resolve(const char* name, const char* smartspace, const char* address, int port) {
    // Check if address is already IP4 address
    struct in_addr addr;
    if (inet_aton(address, &addr) == 1) {
#ifdef DEBUG
        fprintf(stdout, "Address %s doesn't require resolving\n", address);
#endif
        return sslog_new_node(name, smartspace, address, port);
    }

    struct addrinfo* addrinfo = NULL;

    if (getaddrinfo(address, NULL, NULL, &addrinfo) != 0) {
        perror("getaddrinfo");
        return NULL;
    }

    if (addrinfo == NULL) {
        fprintf(stderr, "Can't resolve address %s\n", address);
        return NULL;
    }

    struct sockaddr_in* found_address = NULL;
    struct addrinfo* addr_ptr;
    for (addr_ptr = addrinfo; addr_ptr != NULL; addr_ptr = addr_ptr->ai_next) {
        if (addr_ptr->ai_family == AF_INET) {
            found_address = (struct sockaddr_in*) addr_ptr->ai_addr;
            break;
        }
    }

    sslog_node_t* node = NULL;

    if (found_address != NULL) {
        char* ip_str = inet_ntoa(found_address->sin_addr);
        node = sslog_new_node(name, smartspace, ip_str, port);
        fprintf(stdout, "Address %s resolved to %s\n", address, ip_str);
    }

    freeaddrinfo(addrinfo);

    return node;
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



void flat_array_init(FlatArray* array, size_t elem_size) {
    array->size = 0;
    array->capacity = 1;
    array->elem_size = elem_size;
    array->array = malloc(elem_size);
}

void* flat_array_insert(FlatArray* array) {
    if (array->size == array->capacity) {
        array->capacity *= 2;
        array->array = realloc(array->array, array->capacity * array->elem_size);
        if (array->array == NULL) {
            abort();
        }
    }

    return ((char*) array->array + (array->size++) * array->elem_size);
}

void flat_array_free(FlatArray* array) {
    free(array->array);
}


sslog_individual_t* st_get_subject_by_object(sslog_node_t* node, const char* object_id, sslog_property_t* property) {
    sslog_triple_t* triple_template = sslog_new_triple_detached(SSLOG_TRIPLE_ANY,
                                                                sslog_entity_get_uri(property), object_id,
                                                                SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_URI);

    list_t* triples = sslog_node_query_triple(node, triple_template);

    sslog_triple_t* triple = NULL;
    list_head_t* iter;
    list_for_each(iter, &triples->links) {
        triple = list_entry(iter, list_t, links)->data;
        break;
    }

    if (triple == NULL) {
        return NULL;
    } else {
        return sslog_node_get_individual_by_uri(node, triple->subject);
    }

    // TODO: Cleanup received triples
}

bool remove_and_insert_property(sslog_node_t* node, sslog_individual_t* ind,
                                       sslog_property_t* prop, void* value) {
    if (value == NULL) {
        return true;
    }

    if (sslog_node_remove_property(node, ind, prop, NULL) != SSLOG_ERROR_NO) {
        return false;
    }

    if (sslog_node_update_property(node, ind, prop, NULL, value) != SSLOG_ERROR_NO) {
        return false;
    }

    return true;
}

