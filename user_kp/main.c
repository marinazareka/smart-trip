#include <stdlib.h>
#include <unistd.h>
#include <signal.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static const double TEST_RADIUS = 20000.0;
static const double TEST_LAT = 61.78;
static const double TEST_LONG = 34.35;
static const char* TEST_PATTERN = "петроз";

static volatile bool cont = true;

static void signal_handler(int sig) {
    cont = false;
    sslog_sbcr_stop_all(); 
}

static void subscribe_signals() {
    signal(SIGINT, &signal_handler);
    signal(SIGTERM, &signal_handler);
}

static sslog_individual_t* update_user_location(sslog_node_t* node, sslog_individual_t* user_individual, double lat, double lon) {
    sslog_individual_t* new_location_individual = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(new_location_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(new_location_individual, PROPERTY_LONG, double_to_string(lon));

    sslog_node_insert_individual(node, new_location_individual);

    const void* existing_location = sslog_get_property(user_individual, PROPERTY_HASLOCATION);
    if (existing_location != NULL) {
        printf("Location already exists\n");
    } else {
        printf("Location not exists\n");
    }

    //sslog_node_insert_property(node, user_individual, PROPERTY_HASLOCATION, new_location_individual);
    sslog_node_update_property(node, user_individual, PROPERTY_HASLOCATION, (void*) existing_location, new_location_individual);
    return new_location_individual;
    // TODO: existing search request still uses old location
}

static sslog_individual_t* publish_search_request(sslog_node_t* node, sslog_individual_t* location_individual, char *pattern, double radius) {
    sslog_individual_t* region_individual = sslog_new_individual(CLASS_CIRCLEREGION, rand_uuid("circle_search_region"));
    sslog_insert_property(region_individual, PROPERTY_RADIUS, double_to_string(radius));

    sslog_individual_t* request_individual = sslog_new_individual(CLASS_SEARCHREQUEST, rand_uuid("search_request"));
    sslog_insert_property(request_individual, PROPERTY_USELOCATION, location_individual);
    sslog_insert_property(request_individual, PROPERTY_SEARCHPATTERN, pattern);
    sslog_insert_property(request_individual, PROPERTY_INREGION, region_individual);

    sslog_node_insert_individual(node, region_individual);
    sslog_node_insert_individual(node, request_individual);
    return request_individual;
}

static bool process_subscription_result(sslog_node_t* node, sslog_individual_t* request_individual) {
    fprintf(stderr, "Parse search result\n");
    sslog_individual_t* schedule_individual = sslog_new_individual(CLASS_SCHEDULE, rand_uuid("schedule"));
    sslog_individual_t* route_individual = sslog_new_individual(CLASS_ROUTE, rand_uuid("route"));

    sslog_insert_property(schedule_individual, PROPERTY_HASROUTE, route_individual);

    list_t* inserted_individuals = sslog_get_properties(request_individual, PROPERTY_HASPOINT);

    int counter = 0;
    list_head_t* iter;
    list_for_each(iter, &inserted_individuals->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon); 

        printf("%d: Found point %lf %lf\n", counter, lat, lon);

        /*sslog_individual_t* point2 = sslog_new_individual(CLASS_POINT, rand_uuid("route_point"));
        sslog_insert_property(point2, PROPERTY_LAT, double_to_string(lat));
        sslog_insert_property(point2, PROPERTY_LONG, double_to_string(lon));

        sslog_node_insert_individual(node, point2);
        */
        // Reuse previous point
        sslog_insert_property(route_individual, PROPERTY_HASPOINT, point_individual);

        counter++;
    }

    list_free(inserted_individuals);

    // сам маршрут
    sslog_node_insert_individual(node, route_individual);
    sslog_node_insert_individual(node, schedule_individual);

    return true;
}

static void subscribe_response(sslog_node_t* node, sslog_individual_t* request_individual) {
    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);

    sslog_subscription_t* subscription = sslog_new_subscription(node, false);
    sslog_sbcr_add_individual(subscription, request_individual, properties);

    fprintf(stderr, "Subscribing response\n");
    if (sslog_sbcr_subscribe(subscription) != SSLOG_ERROR_NO) {
        sslog_free_subscription(subscription);
        fprintf(stderr, "Can't subscribe request\n");
        return;
    }

    sslog_sbcr_wait(subscription);
    fprintf(stderr, "Subscription_wait finished\n");

    sslog_node_populate(node, request_individual);
    process_subscription_result(node, request_individual);

    fprintf(stderr, "Unsubscribing response\n");
    sslog_sbcr_unsubscribe(subscription);
    sslog_free_subscription(subscription);
}

int main() {
    subscribe_signals();

    init_rand();
	sslog_init();
    register_ontology();

	sslog_node_t* node = create_node("user_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

        char * user = rand_uuid("user");
        printf("user is %s\n", user);
    // Publish user individual
    sslog_individual_t* user_individual = sslog_new_individual(CLASS_USER, user);
    sslog_node_insert_individual(node, user_individual);

    // Update user location
    sslog_individual_t* location_individual = update_user_location(node, user_individual, TEST_LAT, TEST_LONG);

    // поисковый запрос (тест 1)
    sslog_individual_t* request_individual = publish_search_request(node, location_individual, (char *)TEST_PATTERN, TEST_RADIUS);
    
    // маршрут (тест 2)
    subscribe_response(node, request_individual);

    sslog_node_remove_individual(node, request_individual);
    sslog_node_remove_individual(node, user_individual);

	sslog_node_leave(node);
	sslog_shutdown();
}

