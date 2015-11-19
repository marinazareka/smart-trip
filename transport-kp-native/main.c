#include <stdlib.h>
#include <stdio.h>
#include <locale.h>
#include <pthread.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

// Exported to JNA
bool init(const char* name, const char* smartspace, const char* address, int port);
void shutdown();

bool subscribe();
void unsubscribe();

bool wait_subscription(int* out_points_count, double** out_points_pairs, void** data);
void publish(int points_count, double* points_pairs, const char* roadType, void* data); 


static sslog_node_t* node;
static sslog_subscription_t* sub;

static PtrArray requests_array;
static pthread_mutex_t requests_mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t requests_cond = PTHREAD_COND_INITIALIZER;

typedef struct {
    int count;
    double* points;
    void* extra;
} RequestData;

// Implementations
bool init(const char* name, const char* smartspace, const char* address, int port) {
    setlocale(LC_NUMERIC, "C");

    sslog_init();
    register_ontology();

    node = sslog_new_node(name, smartspace, address, port);

    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
        return false;
    } else {
        return true;
    }
}

void shutdown() {
    sslog_node_leave(node);
    sslog_shutdown();
}

static RequestData* process_request(sslog_individual_t* route) {
    fprintf(stderr, "process_request\n");
    list_t* points = sslog_get_properties(route, PROPERTY_HASPOINT);
    
    if (points == NULL) {
        fprintf(stderr, "Route received but has no points\n");
        return NULL;
    }

    RequestData* request_data = malloc(sizeof(RequestData));
    int count = list_count(points);
    double* points_array = malloc(count * 2 * sizeof(double));
    
    int c = 0;
    list_head_t* iter;
    list_for_each(iter, &points->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);

        fprintf(stderr, "Point %lf %lf\n", lat, lon);

        points_array[c] = lat;
        points_array[c + 1] = lon;
        c += 2;
    }

    request_data->extra = route;
    request_data->points = points_array;
    request_data->count = count;

    return request_data;
}

static void subscription_handler(sslog_subscription_t* sub) {
    fprintf(stderr, "Subscription received\n");
    pthread_mutex_lock(&requests_mutex);

    sslog_sbcr_changes_t* changes = sslog_sbcr_get_changes_last(sub);
    const list_t* inserted_list = sslog_sbcr_ch_get_individual_by_action(changes, SSLOG_ACTION_INSERT);

    list_head_t* iter;
    list_for_each(iter, &inserted_list->links) {
        list_t* entry = list_entry(iter, list_t, links);
        const char* request_individual_id = (const char*) entry->data;
        sslog_individual_t* request_individual = sslog_new_individual(CLASS_SCHEDULE, request_individual_id);
        sslog_individual_t* route_individual 
            = (sslog_individual_t*) sslog_node_get_property(node, request_individual, PROPERTY_HASROUTE);

        if (route_individual == NULL) {
            fprintf(stderr, "Received null route individual. Skipping\n");
        }

        sslog_node_populate(node, route_individual);

        RequestData* request_data = process_request(route_individual);
        if (request_data == NULL) {
            continue;
        }
        ptr_array_insert(&requests_array, request_data);
    }

    pthread_mutex_unlock(&requests_mutex);

    pthread_cond_signal(&requests_cond);
}

bool subscribe() {
    sub = sslog_new_subscription(node, true);
    sslog_sbcr_set_changed_handler(sub, &subscription_handler);
    
    sslog_sbcr_add_class(sub, CLASS_SCHEDULE);
    
    ptr_array_init(&requests_array);
    // Ignore existing data, only process new schedule requests
    if (sslog_sbcr_subscribe(sub) != SSLOG_ERROR_NO) {
        return false;
    } else {
        return true;
    }
}

void unsubscribe() {
    sslog_sbcr_stop(sub);
    sslog_sbcr_unsubscribe(sub);
    sslog_free_subscription(sub);
    ptr_array_free(&requests_array);
    sub = NULL;
}

static RequestData* find_last_processed() {
    if (requests_array.size == 0)
        return false;

    RequestData* request_data = ptr_array_remove_last(&requests_array);
    return request_data;
}

bool wait_subscription(int* out_points_count, double** out_points_pairs, void** data) {
    pthread_mutex_lock(&requests_mutex);

    RequestData* request_data = find_last_processed();
    if (request_data == NULL) {
        fprintf(stderr, "Waiting for request\n");
        pthread_cond_wait(&requests_cond, &requests_mutex);
        request_data = find_last_processed();
    }

    if (request_data == NULL) {
        pthread_mutex_unlock(&requests_mutex);
        return false;
    }

    *out_points_count = request_data->count;
    *out_points_pairs = request_data->points;
    *data = request_data->extra;

    free(request_data);

    pthread_mutex_unlock(&requests_mutex);
    return true;
}

void publish(int points_count, double* points_pairs, const char* roadType, void* data) {
    int movements_count = points_count - 1;

    sslog_individual_t* route_individual = data;

    sslog_individual_t* point_individuals[points_count];
    sslog_individual_t* movement_individuals[movements_count];

    for (int i = 0; i < points_count; i++) {
        double lat = points_pairs[2 * i];
        double lon = points_pairs[2 * i + 1];

        point_individuals[i] = create_point_individual(node, lat, lon);
    }

    sslog_individual_t* previous_movement = NULL;
    for (int i = 1; i < points_count; i++) {
        // These points already in smartspace
        sslog_individual_t* point1 = point_individuals[i - 1];
        sslog_individual_t* point2 = point_individuals[i];

        sslog_individual_t* movement = sslog_new_individual(CLASS_MOVEMENT, rand_uuid("movement"));
        sslog_insert_property(movement, PROPERTY_ISSTARTPOINT, point1);
        sslog_insert_property(movement, PROPERTY_ISENDPOINT, point2);
        sslog_insert_property(movement, PROPERTY_USEROAD, (void*) roadType);
        if (previous_movement != NULL) {
            sslog_insert_property(previous_movement, PROPERTY_HASNEXTMOVEMENT, movement);
        }

        sslog_node_insert_individual(node, movement);

        sslog_node_insert_property(node, route_individual, PROPERTY_HASMOVEMENT, movement);

        movement_individuals[i - 1] = movement;
        previous_movement = movement;
    }

    if (movements_count > 0) {
        sslog_node_insert_property(node, route_individual, PROPERTY_HASSTARTMOVEMENT, movement_individuals[0]);
    }
}
