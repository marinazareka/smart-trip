#include "smarttrip.h"
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#include <pthread.h>

#include <android/log.h>
#include <common/common.h>
#include <entity_internal.h>

#include "common/st_point.h"
#include "st_movement.h"
#include "ontology/ontology.h"
#include "common/common.h"
#include "smartslog.h"

static sslog_node_t *node;

static sslog_individual_t* user_individual;
static sslog_individual_t* user_location;

static double user_lat;
static double user_lon;

static sslog_subscription_t* sub_search_request;
static sslog_individual_t* request_individual;

static sslog_subscription_t* sub_schedule_request;

static sslog_individual_t* schedule_individual;
static sslog_individual_t* route_individual;

static void schedule_subscription_handler(sslog_subscription_t* sub) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "schedule_subscription_handler");


    sslog_individual_t* start_movement = sslog_node_get_property(node, route_individual,
                                                                 PROPERTY_HASSTARTMOVEMENT);

    if (start_movement == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Empty schedule response");
        return;
    }

    sslog_individual_t* current_movement = start_movement;

    PtrArray ptr_array;
    ptr_array_init(&ptr_array);
    while (current_movement != NULL) {
        ptr_array_insert(&ptr_array, current_movement);
        current_movement = sslog_node_get_property(node, current_movement, PROPERTY_HASNEXTMOVEMENT);
    }

    int movement_count = ptr_array.size;
    struct Movement movement_array[movement_count];

    for (int i = 0; i < movement_count; i++) {
        sslog_individual_t* movement = ptr_array.array[i];

        sslog_individual_t* point_a = sslog_node_get_property(node, movement, PROPERTY_ISSTARTPOINT);
        sslog_individual_t* point_b = sslog_node_get_property(node, movement, PROPERTY_ISENDPOINT);

        double lat, lon;

        get_point_coordinates(node, point_a, &lat, &lon);
        const char* title = sslog_get_property(point_a, PROPERTY_POITITLE);
        if (title == NULL)
            title = "Untitled";
        st_init_point(&movement_array[i].point_a, point_a->entity.uri, title, lat, lon);

        get_point_coordinates(node, point_b, &lat, &lon);
        title = sslog_get_property(point_b, PROPERTY_POITITLE);
        if (title == NULL)
            title = "Untitled";
        st_init_point(&movement_array[i].point_b, point_b->entity.uri, title, lat, lon);
    }

    st_on_schedule_request_ready(movement_array, movement_count);

    ptr_array_free(&ptr_array);
    for (int i = 0; i < movement_count; i++) {
        st_free_point(&movement_array[i].point_a);
        st_free_point(&movement_array[i].point_b);
    }
}

static void search_subscription_handler(sslog_subscription_t* sub) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "search_subscription_handler");

    sslog_node_populate(node, request_individual);

    list_t* inserted_individuals = sslog_get_properties(request_individual, PROPERTY_HASPOINT);

    int points_number = list_count(inserted_individuals);
    struct Point point_array[points_number];

    if (points_number == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Empty search response");
        return;
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Request with %d points", points_number);

    int counter = 0;
    list_head_t* iter;
    list_for_each(iter, &inserted_individuals->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);
        const char* title = sslog_get_property(point_individual, PROPERTY_POITITLE);

        if (title == NULL) {
            title = "Untitled";
        }

        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Point found %s", title);
        st_init_point(&point_array[counter], point_individual->entity.uri, title, lat, lon);

        counter++;
    }

    st_on_search_request_ready(point_array, points_number);
    for (int i = 0; i < points_number; i++) {
        st_free_point(&point_array[i]);
    }
}

static void subscribe_route_processed(sslog_individual_t* route) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating schedule subscription");

    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);

    sub_schedule_request = sslog_new_subscription(node, true);
    sslog_sbcr_add_individual(sub_schedule_request, route, properties);
    sslog_sbcr_set_changed_handler(sub_schedule_request, &schedule_subscription_handler);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Subscribing schedule request");
    if (sslog_sbcr_subscribe(sub_schedule_request) != SSLOG_ERROR_NO) {
        sslog_free_subscription(sub_schedule_request);
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Can't subscribe schedule response: %s", sslog_error_get_text(node));
        return;
    }
}

/**
 * Убедиться что пользователь с заданным id присутствует в smartspace'е
 */
static void ensure_user_individual(const char *id) {
    sslog_individual_t* tmp = sslog_node_get_individual_by_uri(node, id);

    if (tmp == NULL) {
        tmp = sslog_new_individual(CLASS_USER, id);
        sslog_node_insert_individual(node, tmp);

        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "No user found");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Existing user found with id %s",
                            tmp->entity.uri);
    }

    user_individual = tmp;
}

/**
 * Загрузить Schedule и Route для текущего пользователя
 */
static void load_existing_schedule() {
    schedule_individual = sslog_node_get_property(node, user_individual, PROPERTY_PROVIDE);
    if (schedule_individual != NULL) {
        route_individual = sslog_node_get_property(node, schedule_individual, PROPERTY_HASROUTE);
    } else {
        route_individual = NULL;
    }

    // Если route уже присутствует для данного пользователя, сразу подписываемся
    if (route_individual != NULL) {
        subscribe_route_processed(route_individual);
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Existing route found with id %s",
                            route_individual->entity.uri);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "No existing route found");
    }
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
    load_existing_schedule();

    return true;
}

void st_shutdown() {
    sslog_node_leave(node);
    sslog_shutdown();
}

void st_update_user_location(double lat, double lon) {
    user_lat = lat;
    user_lon = lon;

    sslog_individual_t* new_location_individual
            = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(new_location_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(new_location_individual, PROPERTY_LONG, double_to_string(lon));

    sslog_node_insert_individual(node, new_location_individual);

    sslog_individual_t* existing_user_location = sslog_get_property(user_individual, PROPERTY_HASLOCATION);
    if (existing_user_location != NULL) {
        printf("Location already exists\n");
    } else {
        printf("Location not exists\n");
    }

    sslog_node_update_property(node, user_individual, PROPERTY_HASLOCATION,
                               (void*) existing_user_location, new_location_individual);

    user_location = new_location_individual;

    // TODO: Delete old user location
    //if (existing_user_location != NULL) {
    //    sslog_node_remove_individual_with_local(node, existing_user_location);
    //}
}

// TODO: will not work if no user location available
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

    // Create location individual
    sslog_individual_t* location_individual
            = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(location_individual, PROPERTY_LAT, double_to_string(user_lat));
    sslog_insert_property(location_individual, PROPERTY_LONG, double_to_string(user_lon));

    if (sslog_node_insert_individual(node, location_individual) != SSLOG_ERROR_NO) {
        // TODO: cleanup
        const char* error_text = sslog_error_get_last_text();
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert location individual: %s", error_text);
        st_on_request_failed(error_text);
        return;
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating searchrequest individual");

    // Create circle region individual
    sslog_individual_t* region_individual = sslog_new_individual(CLASS_CIRCLEREGION,
                                                                 rand_uuid("circle_search_region"));
    sslog_insert_property(region_individual, PROPERTY_RADIUS, double_to_string(radius));
    sslog_node_insert_individual(node, region_individual);

    // Create request individual
    sslog_individual_t* request_individual_l = sslog_new_individual(CLASS_SEARCHREQUEST,
                                                                    rand_uuid("search_request"));
    sslog_insert_property(request_individual_l, PROPERTY_USELOCATION, location_individual);
    sslog_insert_property(request_individual_l, PROPERTY_INREGION, region_individual);

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
}

void st_post_schedule_request(struct Point* points, int points_count, const char* tsp_type) {
    if (route_individual != NULL) {
        // Удаляем все текущие параметры запроса (hasPoint)
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Updating existing schedule and route");

        sslog_node_remove_property(node, route_individual, PROPERTY_HASPOINT, NULL);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating new schedule and route");

        schedule_individual = sslog_new_individual(CLASS_SCHEDULE, rand_uuid("schedule"));
        route_individual = sslog_new_individual(CLASS_ROUTE, rand_uuid("route"));

        sslog_insert_property(schedule_individual, PROPERTY_HASROUTE, route_individual);

        sslog_node_insert_individual(node, route_individual);
        sslog_node_insert_individual(node, schedule_individual);
        int res = sslog_node_insert_property(node, user_individual, PROPERTY_PROVIDE, schedule_individual);

        if (res != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Can't assign provide property to user %s",
                                sslog_error_get_last_text());
        }

        subscribe_route_processed(route_individual);
    }

    // Состояние smartspace'а: user->schedule->route, route не содержит точек, может содержать updated, processed и tspType

    // TODO: добавлять точки в одну транзакцию
    // TODO: удалять старые точки или придумать какой-нибудь GarbageCollectorKP
    for (int i = 0; i < points_count; i++) {
        sslog_individual_t* point_individual = create_poi_individual(node, points[i].lat, points[i].lon,
                                                                     points[i].title, "nocategory");
        sslog_node_insert_property(node, route_individual, PROPERTY_HASPOINT, point_individual);
    }

    sslog_node_update_property(node, route_individual, PROPERTY_TSPTYPE, NULL, (void*) tsp_type);
    sslog_node_update_property(node, route_individual, PROPERTY_UPDATED, NULL, rand_uuid("updated"));
}


