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
void publish(int points_count, double* points_pairs, void* data);


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

        const char* lat_str = sslog_get_property(point_individual, PROPERTY_LAT);
        const char* lon_str = sslog_get_property(point_individual, PROPERTY_LONG);

        double lat = parse_double(lat_str);
        double lon = parse_double(lon_str);

        fprintf(stderr, "Point %lf %lf %s %s\n", lat, lon, lat_str, lon_str);

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
        sslog_node_populate(node, request_individual);

        if (process_request(request_individual, out_points_count, out_points_pairs)) {
            *data = (void*) request_individual;
            return true;
        }
    }

    return false;
}

void publish(int points_count, double* points_pairs, void* data) {

}
