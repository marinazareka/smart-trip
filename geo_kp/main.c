
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static volatile bool cont = true;

static void publish_point(sslog_node_t* node, sslog_individual_t* request_individual, double lat, double lon) {
    printf("Inserting point %lf %lf\n", lat, lon);

    CLEANUP_INDIVIDUAL sslog_individual_t* point_individual = sslog_new_individual(CLASS_POINT, rand_uuid("point"));

    sslog_insert_property(point_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(point_individual, PROPERTY_LONG, double_to_string(lon));

    sslog_node_insert_individual(node, point_individual);

    sslog_node_insert_property(node, request_individual, PROPERTY_HASPOINT, point_individual);
}

static void find_and_publish_points(sslog_node_t* node, sslog_individual_t* request_individual, double lat, double lon) {
    publish_point(node, request_individual, 61.787351, 34.354369);
    publish_point(node, request_individual, 61.787026, 34.365269);
    publish_point(node, request_individual, 61.787859, 34.375612);
    publish_point(node, request_individual, 61.792167, 34.369475);
    publish_point(node, request_individual, 61.783023, 34.360334);

    sslog_node_insert_property(node, request_individual, PROPERTY_PROCESSED, long_to_string(time(NULL)));
}

static void process_inserted_request(sslog_node_t* node, const char* request_uuid) {
    CLEANUP_INDIVIDUAL sslog_individual_t* request_individual = sslog_get_individual(request_uuid);
    if (request_individual == NULL) {
        printf("Can't get request individual\n");
        return;
    }

    CLEANUP_INDIVIDUAL sslog_individual_t* location_individual
        = (sslog_individual_t*) sslog_node_get_property(node, request_individual, PROPERTY_USELOCATION);
    if (request_individual == NULL) {
        printf("Can't get request location individual\n");
        return;
    }

    sslog_node_populate(node, location_individual);
    double lat = parse_double(sslog_get_property(location_individual, PROPERTY_LAT));
    double lon = parse_double(sslog_get_property(location_individual, PROPERTY_LONG));
    printf("User location: %lf %lf\n", lat, lon);
    find_and_publish_points(node, request_individual, lat, lon);
}

static void process_subscription_request(sslog_node_t* node, sslog_subscription_t* subscription) {
    sslog_sbcr_changes_t* changes = sslog_sbcr_get_changes_last(subscription);
    if (changes == NULL) {
        return;
    }

    const list_t* inserted_requests = sslog_sbcr_ch_get_individual_by_action(changes, SSLOG_ACTION_INSERT);

    list_head_t* iter;
    list_for_each(iter, &inserted_requests->links) {
        list_t* entry = list_entry(iter, list_t, links);
        const char* request_uuid = (const char*) entry->data;
        printf("Request inserted %s\n", request_uuid);
        process_inserted_request(node, request_uuid);
    }
}

static void subscribe_request(sslog_node_t* node) {
    sslog_subscription_t* subscription = sslog_new_subscription(node, false);
    sslog_sbcr_add_class(subscription, CLASS_SEARCHREQUEST);

    sslog_sbcr_subscribe(subscription);

    do {
       process_subscription_request(node, subscription);         
       sslog_sbcr_wait(subscription);
    } while (cont);

    sslog_sbcr_unsubscribe(subscription);
    sslog_free_subscription(subscription);
}

int main(void) {
    init_rand();
	sslog_init();
    register_ontology();

	sslog_node_t* node = sslog_new_node("geo_kp", "X", "127.0.0.1", 10010);
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    subscribe_request(node);

	sslog_node_leave(node);
	sslog_shutdown();
}
