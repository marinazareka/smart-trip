
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"
#include "st_point.h"

#include "test-loader.h"
#include "wm-loader.h"

static volatile bool cont = true;

static struct LoaderInterface point_loader;

static void publish_point(sslog_node_t* node, sslog_individual_t* request_individual, struct Point* point) {
    printf("Inserting point %lf %lf %s\n", point->lat, point->lon, point->title);
    // TODO: uuid isn't being copied
    sslog_individual_t* point_individual = create_poi_individual(node, point->lat, point->lon, point->title, "nocategory");
    printf("URI %s\n", sslog_entity_get_uri(point_individual));
    sslog_node_insert_property(node, request_individual, PROPERTY_HASPOINT, point_individual);
}

static void find_and_publish_points(sslog_node_t* node, sslog_individual_t* request_individual, 
        double lat, double lon, double radius, const char* pattern) {
    struct Point* points = NULL;
    int count = 0;

    point_loader.load_points(lat, lon, radius, pattern,  &points, &count);

    for (int i = 0; i < count; i++) {
        publish_point(node, request_individual, &points[i]);
    }

    st_free_point_array(points, count);
    free(points);

    printf("Inserting updated property\n");
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
    if (location_individual == NULL) {
        printf("Can't get request location individual\n");
        return;
    }

    CLEANUP_INDIVIDUAL sslog_individual_t* region_individual
        = (sslog_individual_t*) sslog_node_get_property(node, request_individual, PROPERTY_INREGION);
    if (region_individual == NULL) {
        printf("Can't gen request region individual\n");
        return;
    }

    const char* lat_string = sslog_node_get_property(node, location_individual, PROPERTY_LAT);
    const char* lon_string = sslog_node_get_property(node, location_individual, PROPERTY_LONG);
    const char* radius_string = sslog_node_get_property(node, region_individual, PROPERTY_RADIUS);

    if (lat_string == NULL || lon_string == NULL) {
        printf("Null latitude and longitude in USELOCATION property\n");
        return;
    }

    if (radius_string == NULL) {
        printf("Null raduis in RADIUS property\n");
        return;
    }

    double lat = parse_double(lat_string);
    double lon = parse_double(lon_string);

    double radius = parse_double(radius_string);

    const char* pattern = sslog_node_get_property(node, request_individual, PROPERTY_SEARCHPATTERN);
    if (pattern == NULL) {
        printf("Null pattern property in request\n");
        return;
    }

    printf("User location: %lf %lf. Search radius: %lf\n", lat, lon, radius);

    find_and_publish_points(node, request_individual, lat, lon, radius, pattern);
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

    sslog_node_t* node = create_node("user_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    point_loader = create_wm_loader(get_config_value("config.ini", "WMLoader", "Key"));

    subscribe_request(node);

	sslog_node_leave(node);
	sslog_shutdown();
}
