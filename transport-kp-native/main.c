#include <stdlib.h>
#include <stdio.h>
#include <locale.h>

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

bool subscribe() {
    sub = sslog_new_subscription(node, false);
    
    sslog_sbcr_add_class(sub, CLASS_SCHEDULE);
    
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
    sub = NULL;
}

static bool process_request(sslog_individual_t* request, int* out_points_count, double** out_points_pairs) {
    fprintf(stderr, "process_request\n");
    list_t* points = sslog_get_properties(request, PROPERTY_HASPOINT);

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

        sslog_remove_individual(point_individual);
    }

    *out_points_count = count;
    *out_points_pairs = points_array;

    return true;
}

// TODO: this method processes only first request
bool wait_subscription(int* out_points_count, double** out_points_pairs, void** data) {
    if (sslog_sbcr_wait(sub) != SSLOG_ERROR_NO) {
        return false;
    }

    fprintf(stderr, "Wait completed\n");
    sslog_sbcr_changes_t* changes = sslog_sbcr_get_changes_last(sub);
    const list_t* inserted_list = sslog_sbcr_ch_get_individual_by_action(changes, SSLOG_ACTION_INSERT);

    fprintf(stderr, "Changes loaded\n");
    list_head_t* iter;
    list_for_each(iter, &inserted_list->links) {
        list_t* entry = list_entry(iter, list_t, links);
        const char* request_individual_id = (const char*) entry->data;
        sslog_individual_t* request_individual = sslog_new_individual(CLASS_SCHEDULE, request_individual_id);
        sslog_individual_t* route_individual 
            = (sslog_individual_t*) sslog_node_get_property(node, request_individual, PROPERTY_HASROUTE);
        sslog_node_populate(node, route_individual);

        if (process_request(route_individual, out_points_count, out_points_pairs)) {
            *data = route_individual;
            return true;
        }
    }

    return false;
}

void publish(int points_count, double* points_pairs, const char* roadType, void* data) {
    int movements_count = points_count - 1;

    sslog_individual_t* route_individual = data;

    sslog_individual_t* point_individuals[points_count];
    sslog_individual_t* movement_individuals[movements_count];

    for (int i = 0; i < points_count; i++) {
        double lat = points_pairs[2 * i];
        double lon = points_pairs[2 * i + 1];

        sslog_individual_t* location_individual = sslog_new_individual(CLASS_LOCATION, rand_uuid("response_location"));
        sslog_insert_property(location_individual, PROPERTY_LAT, double_to_string(lat));
        sslog_insert_property(location_individual, PROPERTY_LONG, double_to_string(lon));
        sslog_node_insert_individual(node, location_individual);

        point_individuals[i] = sslog_new_individual(CLASS_POINT, rand_uuid("response_point"));
        sslog_insert_property(point_individuals[i], PROPERTY_HASLOCATION, location_individual);
        sslog_node_insert_individual(node, point_individuals[i]);
    }

    for (int i = 1; i < points_count; i++) {
        // These points already in smartspace
        sslog_individual_t* point1 = point_individuals[i - 1];
        sslog_individual_t* point2 = point_individuals[i];

        sslog_individual_t* movement = sslog_new_individual(CLASS_MOVEMENT, rand_uuid("movement"));
        sslog_insert_property(movement, PROPERTY_ISSTARTPOINT, point1);
        sslog_insert_property(movement, PROPERTY_ISENDPOINT, point2);
        sslog_insert_property(movement, PROPERTY_USEROAD, (void*) roadType);

        sslog_node_insert_individual(node, movement);

        sslog_node_insert_property(node, route_individual, PROPERTY_HASMOVEMENT, movement);

        movement_individuals[i] = movement;
    }

    if (movements_count > 1) {
        sslog_node_insert_property(node, route_individual, PROPERTY_HASSTARTMOVEMENT, movement_individuals[0]);
    }
}
