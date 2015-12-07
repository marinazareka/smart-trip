#include "smarttrip.h"
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#include <pthread.h>

#include <android/log.h>

#include "st_point.h"
#include "ontology/ontology.h"
#include "common/common.h"
#include "smartslog.h"

static sslog_node_t *node;

static sslog_individual_t *user_individual;
static sslog_individual_t *user_location;

static sslog_subscription_t* sub_search_request;
static sslog_individual_t* request_individual;

static void search_subscription_handler(sslog_subscription_t* sub) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "search_subscription_handler");

    sslog_node_populate(node, request_individual);

    list_t* inserted_individuals = sslog_get_properties(request_individual, PROPERTY_HASPOINT);

    int points_number = list_count(inserted_individuals);
    struct Point point_array[points_number];

    int counter = 0;
    list_head_t* iter;
    list_for_each(iter, &inserted_individuals->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);

        char buf[1000];
        sprintf(buf, "id%lf%lf", lat, lon);

        st_init_point(&point_array[counter], buf, buf, lat, lon);

        counter++;
    }

    st_on_search_request_ready(point_array, points_number);
    for (int i = 0; i < points_number; i++) {
        st_free_point(&point_array[i]);
    }
}

static void* handle_search_request_test(void* data) {
    sleep(1);
    struct Point test_array[10];
    for (int i = 0; i < 10; i++) {
        char name[100];
        sprintf(name, "id%d", i);

        st_init_point(&test_array[i], name, "Test point", i, -i);
    }

    st_on_search_request_ready(test_array, 10);

    for (int i = 0; i < 10; i++) {
        st_free_point(&test_array[i]);
    }

    return NULL;
}

static void ensure_user_individual(const char *id) {
    sslog_individual_t* tmp = sslog_node_get_individual_by_uri(node, id);

    if (tmp == NULL) {
        sslog_individual_t* user_individual = sslog_new_individual(CLASS_USER, rand_uuid("user"));
        sslog_node_insert_individual(node, user_individual);
    }

    user_individual = tmp;
}

bool st_initialize(const char *user_id, const char *kp_name, const char *smart_space_name,
                   const char *address, int port) {
    init_rand();

    if (sslog_init() != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Error sslog_init %s",
                            sslog_error_get_text(node));
        return false;
    } else {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Sslog_init ok");
    }

    register_ontology();

    node = create_node_resolve(kp_name, smart_space_name, address, port);

    if (node == NULL) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Error create node %s",
                            sslog_error_get_text(node));
        return false;
    } else {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Node created");
    }

    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Can't join node");
        return false;
    }

    ensure_user_individual(user_id);

    return true;
}

void st_shutdown() {
    sslog_node_leave(node);
    sslog_shutdown();
}

void st_update_user_location(double lat, double lon) {
    sslog_individual_t* new_location_individual
            = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(new_location_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(new_location_individual, PROPERTY_LONG, double_to_string(lon));

    sslog_node_insert_individual(node, new_location_individual);

    const void* existing_location = sslog_get_property(user_individual, PROPERTY_HASLOCATION);
    if (existing_location != NULL) {
        printf("Location already exists\n");
    } else {
        printf("Location not exists\n");
    }

    sslog_node_update_property(node, user_individual, PROPERTY_HASLOCATION,
                               (void*) existing_location, new_location_individual);

    user_location = new_location_individual;

    // TODO: delete old location?
}

// TODO: will no work if no user location available
void st_post_search_request(double radius, const char *pattern) {
    if (sub_search_request != NULL) {
        sslog_sbcr_stop(sub_search_request);
        sslog_sbcr_unsubscribe(sub_search_request);
        sslog_free_subscription(sub_search_request);
        sub_search_request = NULL;
    }

    if (request_individual != NULL) {
        sslog_node_remove_individual(node, request_individual);
        request_individual = NULL;
    }

    // Create individual
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating searchrequest individual");

    sslog_individual_t* region_individual = sslog_new_individual(CLASS_CIRCLEREGION,
                                                                 rand_uuid("circle_search_region"));
    sslog_insert_property(region_individual, PROPERTY_RADIUS, double_to_string(radius));

    sslog_individual_t* request_individual_l = sslog_new_individual(CLASS_SEARCHREQUEST,
                                                                  rand_uuid("search_request"));
    sslog_insert_property(request_individual_l, PROPERTY_USELOCATION, user_location);

    sslog_node_insert_individual(node, request_individual_l);

    request_individual = request_individual_l;


    // Subscribe to update
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating subsription");

    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);

    sub_search_request = sslog_new_subscription(node, true);
    sslog_sbcr_add_individual(sub_search_request, request_individual, properties);
    sslog_sbcr_set_changed_handler(sub_search_request, &search_subscription_handler);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Subscribing search request");
    if (sslog_sbcr_subscribe(sub_search_request) != SSLOG_ERROR_NO) {
        sslog_free_subscription(sub_search_request);
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Can't subscribe response: %s", sslog_error_get_text(node));
        return;
    }

//    pthread_t thread;
//    pthread_create(&thread, NULL, &handle_search_request_test, NULL);
//    pthread_detach(thread);
}

void st_post_schedule_request(struct Point *points, int points_count) {

}


