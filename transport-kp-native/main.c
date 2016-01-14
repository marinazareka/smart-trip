#include <stdlib.h>
#include <stdio.h>
#include <locale.h>
#include <pthread.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

// Exported to JNA
typedef struct {
    int count;
    double* points;
    
    char* user_id;
    double user_lat;
    double user_lon;

    char* tsp_type;

    sslog_individual_t** point_individuals;
    sslog_individual_t* route;
} RequestData;

bool init(const char* name, const char* smartspace, const char* address, int port);
void shutdown();

bool subscribe();
void unsubscribe();

RequestData* wait_subscription(void);
void publish(int points_count, int* ids, const char* roadType, RequestData* requestData); 

static sslog_node_t* node;
static sslog_subscription_t* sub;

static PtrArray requests_array;
static pthread_mutex_t requests_mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t requests_cond = PTHREAD_COND_INITIALIZER;


// Implementations
bool init(const char* name, const char* smartspace, const char* address, int port) {
    setlocale(LC_NUMERIC, "C");

    sslog_init();
    register_ontology();

    node = create_node_resolve(name, smartspace, address, port);

    if (node == NULL || sslog_node_join(node) != SSLOG_ERROR_NO) {
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
    sslog_individual_t** point_individuals = malloc(count * sizeof(sslog_individual_t*));
    
    int c = 0;
    list_head_t* iter;
    list_for_each(iter, &points->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);

        fprintf(stderr, "Point %lf %lf\n", lat, lon);

        points_array[2 * c] = lat;
        points_array[2 * c + 1] = lon;

        point_individuals[c] = point_individual;

        c += 1;
    }

    request_data->route = route;
    request_data->points = points_array;
    request_data->point_individuals = point_individuals;
    request_data->count = count;

    return request_data;
}

static void handle_updated_request(sslog_individual_t* user, sslog_individual_t* schedule, sslog_individual_t* route) {
    sslog_individual_t* location = (sslog_individual_t*) sslog_node_get_property(node, user, PROPERTY_HASLOCATION);

    sslog_node_populate(node, location);
    sslog_node_populate(node, route);

    list_t* points = sslog_get_properties(route, PROPERTY_HASPOINT);
    int count;

    // TODO: free list if not null
    if (points == NULL || (count = list_count(points)) == 0) {
        fprintf(stderr, "Route received but has no points\n");
        return;
    }

    RequestData* request_data = malloc(sizeof(RequestData));

    double* points_array = malloc(count * 2 * sizeof(double));
    sslog_individual_t** point_individuals = malloc(count * sizeof(sslog_individual_t*));
    
    int c = 0;
    list_head_t* iter;
    list_for_each(iter, &points->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);

        fprintf(stderr, "Point %lf %lf\n", lat, lon);

        points_array[2 * c] = lat;
        points_array[2 * c + 1] = lon;

        point_individuals[c] = point_individual;

        c += 1;
    }

    request_data->user_id = strdup(sslog_entity_get_uri(user));
    request_data->route = route;
    request_data->points = points_array;
    request_data->point_individuals = point_individuals;
    request_data->count = count;
    request_data->user_lat = parse_double((const char*) sslog_get_property(location, PROPERTY_LAT));
    request_data->user_lon = parse_double((const char*) sslog_get_property(location, PROPERTY_LONG));
    request_data->tsp_type = strdup(sslog_get_property(route, PROPERTY_TSPTYPE));

    ptr_array_insert(&requests_array, request_data);
}

static void handle_updated_property_update(const char* route_id) {
    fprintf(stderr, "handle_updated_property_update %s\n", route_id);
    sslog_individual_t* route_individual = sslog_node_get_individual_by_uri(node, route_id);
    sslog_individual_t* schedule_individual = st_get_subject_by_object(node, route_id, PROPERTY_HASROUTE);   

    if (schedule_individual == NULL) {
        fprintf(stderr, "Can't get schedule for updated route\n");
        return;
    }

    const char* schedule_uri = sslog_entity_get_uri(schedule_individual);
    fprintf(stderr, "schedule uri %s\n", schedule_uri);

    sslog_individual_t* user_individual = st_get_subject_by_object(node, schedule_uri, PROPERTY_PROVIDE);

    if (user_individual == NULL) {
        fprintf(stderr, "Can't get user for updated schedule\n");
        return;
    }

    handle_updated_request(user_individual, schedule_individual, route_individual);
}

static void handle_updated_property_location(const char* user_id, const char* location_individual) {
    fprintf(stderr, "handle_updated_property_location\n");
    sslog_individual_t* user_individual = sslog_node_get_individual_by_uri(node, user_id);
    sslog_individual_t* schedule_individual = (sslog_individual_t*) sslog_node_get_property(node, user_individual, PROPERTY_PROVIDE);
    sslog_individual_t* route_individual = (sslog_individual_t*) sslog_node_get_property(node, schedule_individual, PROPERTY_HASROUTE);

    if (!user_individual || !schedule_individual || !route_individual) {
        fprintf(stderr, "Can't handle location request\n");
        return;
    }

    handle_updated_request(user_individual, schedule_individual, route_individual);
}

static bool is_triple_updated_property(sslog_triple_t* triple) {
    // TODO: sslog_entity_get_uri can be optimized
    return strcmp(triple->predicate, sslog_entity_get_uri(PROPERTY_UPDATED)) == 0;
}

static bool is_triple_location_property(sslog_triple_t* triple) {
    return strcmp(triple->predicate, sslog_entity_get_uri(PROPERTY_HASLOCATION)) == 0;
}

static void subscription_handler_2(sslog_subscription_t* sub) {
    fprintf(stderr, "subscription_handler_2\n");
    pthread_mutex_lock(&requests_mutex);

    sslog_sbcr_changes_t* changes = sslog_sbcr_get_changes_last(sub);
    const list_t* changes_list = sslog_sbcr_ch_get_triples(changes, SSLOG_ACTION_INSERT);

    list_head_t* iter;
    list_for_each (iter, &changes_list->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_triple_t* triple = entry->data;

        fprintf(stderr, "Triple received: <%s> <%s> <%s>\n", triple->subject, triple->predicate, triple->object);

        if (is_triple_updated_property(triple)) {
            handle_updated_property_update(triple->subject);
        }

        if (is_triple_location_property(triple)) {
            handle_updated_property_location(triple->subject, triple->object);
        }
    }

    pthread_mutex_unlock(&requests_mutex);
    pthread_cond_signal(&requests_cond);
}

// @Deprecated
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
    sslog_sbcr_set_changed_handler(sub, &subscription_handler_2);

    sslog_triple_t* updated_triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_UPDATED), 
            SSLOG_TRIPLE_ANY, SSLOG_RDF_TYPE_URI, SS_RDF_TYPE_LIT);
    sslog_sbcr_add_triple_template(sub, updated_triple);

    //sslog_triple_t* location_triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_HASLOCATION),
    //        SSLOG_TRIPLE_ANY, SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_URI);
//    sslog_sbcr_add_triple_template(sub, location_triple);

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

RequestData* wait_subscription() {
    pthread_mutex_lock(&requests_mutex);

    RequestData* request_data = find_last_processed();
    if (request_data == NULL) {
        fprintf(stderr, "Waiting for request\n");
        pthread_cond_wait(&requests_cond, &requests_mutex);
        request_data = find_last_processed();
    }

    if (request_data == NULL) {
        pthread_mutex_unlock(&requests_mutex);
        return NULL;
    }

    pthread_mutex_unlock(&requests_mutex);
    return request_data;
}

static void clear_current_results(sslog_individual_t* route_individual) {
    // TODO: remove old movements
    sslog_node_remove_property(node, route_individual, PROPERTY_HASMOVEMENT, NULL);
    sslog_node_remove_property(node, route_individual, PROPERTY_HASSTARTMOVEMENT, NULL);
}

void publish(int points_count, int* ids, const char* roadType, RequestData* request_data) {
    pthread_mutex_lock(&requests_mutex);
    int movements_count = points_count - 1;

    sslog_individual_t* route_individual = request_data->route;

    clear_current_results(route_individual);

    sslog_individual_t* point_individuals[points_count];
    sslog_individual_t* movement_individuals[movements_count];

    sslog_individual_t* user_point_individual = 
        create_poi_individual(node, request_data->user_lat, request_data->user_lon, "user-location", "system");

    for (int i = 0; i < points_count; i++) {
        int id = ids[i];
        if (id == -1) {
            point_individuals[i] = user_point_individual;
        } else {
            point_individuals[i] = request_data->point_individuals[id];
        }
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
            sslog_node_insert_property(node, previous_movement, PROPERTY_HASNEXTMOVEMENT, movement);
        }

        sslog_node_insert_individual(node, movement);

        sslog_node_insert_property(node, route_individual, PROPERTY_HASMOVEMENT, movement);

        movement_individuals[i - 1] = movement;
        previous_movement = movement;
    }

    if (movements_count > 0) {
        sslog_node_insert_property(node, route_individual, PROPERTY_HASSTARTMOVEMENT, movement_individuals[0]);
    }

    sslog_node_update_property(node, route_individual, PROPERTY_PROCESSED, NULL, rand_uuid("processed"));

    pthread_mutex_unlock(&requests_mutex);

    free(request_data->points);
    free(request_data->user_id);
    free(request_data->tsp_type);
    free(request_data);
    
    // Clean local stored points
    // This is required, because populate doesn't remove local points, that was removed in sib
    sslog_remove_properties(route_individual, PROPERTY_HASPOINT);
}
