#include "geo_common.h"

#include <stdio.h>
#include <stdlib.h>

#include "common.h"
#include "st_point.h"
#include "ontology.h"

static void publish_point(sslog_node_t* node, sslog_individual_t* request_individual, struct Point* point) {
#ifdef DEBUG
    printf("Inserting point %lf %lf %s\n", point->lat, point->lon, point->title);
#endif
    // TODO: uuid isn't being copied
    
    CLEANUP_INDIVIDUAL sslog_individual_t* point_individual 
        = create_poi_individual(node, point->lat, point->lon, point->title, "nocategory");
    sslog_node_insert_property(node, request_individual, PROPERTY_HASPOINT, point_individual);
}

static void find_and_publish_points(sslog_node_t* node, sslog_individual_t* request_individual, 
        double lat, double lon, double radius, const char* pattern, struct LoaderInterface loader) {
    struct Point* points = NULL;
    int count = 0;

    loader.load_points(lat, lon, radius, pattern,  &points, &count);

    for (int i = 0; i < count; i++) {
        publish_point(node, request_individual, &points[i]);
    }

    st_free_point_array(points, count);
    free(points);

#ifdef DEBUG
    printf("%s:%i: Inserting updated property (%i)\n", __FILE__, __LINE__, count);
#endif
    sslog_node_insert_property(node, request_individual, PROPERTY_PROCESSED, long_to_string(time(NULL)));
}

static void process_inserted_request(sslog_node_t* node, const char* request_uuid, struct LoaderInterface loader) {
    CLEANUP_INDIVIDUAL sslog_individual_t* request_individual = sslog_get_individual(request_uuid);
    if (request_individual == NULL) {
        printf("%s:%i: Can't get request individual\n", __FILE__, __LINE__);
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
#ifdef DEBUG
        printf("Null pattern property in request\n");
#endif
    }

    fprintf(stderr, "User location: %lf %lf. Search radius: %lf, pattern: %s\n", lat, lon, radius, pattern);

    find_and_publish_points(node, request_individual, lat, lon, radius, pattern, loader);
}

static void process_subscription_request(sslog_node_t* node, sslog_subscription_t* subscription, struct LoaderInterface loader) {
    sslog_sbcr_changes_t* changes = sslog_sbcr_get_changes_last(subscription);
    // если нет изменений или сработала подписка на уже существующие данные
    if (changes == NULL || sslog_sbcr_ch_get_sequence_number(changes) == 1) {
        return;
    }

    const list_t* inserted_requests = sslog_sbcr_ch_get_individual_by_action(changes, SSLOG_ACTION_INSERT);

    if (inserted_requests == NULL)
        return;
    
    list_head_t* iter;
    list_for_each(iter, &inserted_requests->links) {
        list_t* entry = list_entry(iter, list_t, links);
        const char* request_uuid = (const char*) entry->data;
        printf("%s:%i: Request inserted %s\n", __FILE__, __LINE__, request_uuid);
        process_inserted_request(node, request_uuid, loader);
    }
}

static void subscribe_request(sslog_node_t* node, struct LoaderInterface loader) {
    sslog_subscription_t* subscription = sslog_new_subscription(node, false);
    sslog_sbcr_add_class(subscription, CLASS_SEARCHREQUEST);

    if (sslog_sbcr_subscribe(subscription) != SSLOG_ERROR_NO) {
        fprintf(stderr, "%s:%i: Error subscribing to CLASS_SEARCHREQUEST\n",__FILE__, __LINE__);
        return;
    }

    do {
       process_subscription_request(node, subscription, loader);
       if (sslog_sbcr_wait(subscription) != SSLOG_ERROR_NO) {
           fprintf(stderr, "Error waiting subscription\n");
       }
    } while (true);

    sslog_sbcr_unsubscribe(subscription);
    sslog_free_subscription(subscription);
}

void geo_common_serve_kp(sslog_node_t* node, struct LoaderInterface loader) {
    fprintf(stderr, "%s: waiting for subscriptions\n", loader.get_name());
    subscribe_request(node, loader);
}

double length_to_radians(double length) {
    return length * 3.1415 / 111300 / 180;
}