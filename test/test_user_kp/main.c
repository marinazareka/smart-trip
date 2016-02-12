
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <stdarg.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static const double lat_a = 61.780911, lon_a = 34.338844, lat_b = 61.787738, lon_b = 34.357556;

static double random_lat(void) {
    return erand48(rand_state) * (lat_b - lat_a)  + lat_a;
}

static double random_lon(void) {
    return erand48(rand_state) * (lon_b - lon_a)  + lon_a;
}

int main(void) {
    init_rand();
	sslog_init();
    register_ontology();

    sslog_node_t* node = create_node("test_user_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    sslog_individual_t* user = sslog_new_individual(CLASS_USER, rand_uuid("test_user_"));
    sslog_individual_t* shed = sslog_new_individual(CLASS_USER, rand_uuid("test_schedule_"));
    sslog_individual_t* route = sslog_new_individual(CLASS_USER, rand_uuid("test_route_"));

    sslog_individual_t* location = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location_"));

    sslog_insert_property(location, PROPERTY_LAT, double_to_string(random_lat()));
    sslog_insert_property(location, PROPERTY_LONG, double_to_string(random_lon()));

    sslog_insert_property(user, PROPERTY_HASLOCATION, location);

    sslog_insert_property(user, PROPERTY_PROVIDE, shed);
    sslog_insert_property(shed, PROPERTY_HASROUTE, route);

    sslog_node_insert_individual(node, route);
    sslog_node_insert_individual(node, shed);
    sslog_node_insert_individual(node, location);
    sslog_node_insert_individual(node, user);

    for (;;) {
        printf("Publishing request\n");
        sslog_node_remove_property(node, route, PROPERTY_HASPOINT, NULL); 
        
        for (int i = 0; i < 10; i++) {
            sslog_individual_t* point = create_poi_individual(node, random_lat(), random_lon(), rand_uuid("title_"), "no-category");
            sslog_node_insert_property(node, route, PROPERTY_HASPOINT, point);
        }

        remove_and_insert_property(node, route, PROPERTY_TSPTYPE, "open");
        remove_and_insert_property(node, route, PROPERTY_ROADTYPE, "foot");
        remove_and_insert_property(node, route, PROPERTY_UPDATED, rand_uuid("updated"));
        sleep(3);
    }

	sslog_node_leave(node);
	sslog_shutdown();
}
