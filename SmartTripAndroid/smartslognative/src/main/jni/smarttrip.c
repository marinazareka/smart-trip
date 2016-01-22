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

static bool is_ontology_registered;
static bool is_smartspace_initialized;

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

// Search subscription handler can crash when runs simultaneous with location update, so lock it all
static pthread_mutex_t ss_mutex = PTHREAD_MUTEX_INITIALIZER;

static void schedule_subscription_error_handler(sslog_subscription_t* sub, int code) {
    __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Subscription error code %d", code);
}

static void schedule_subscription_handler(sslog_subscription_t* sub) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "schedule_subscription_handler");
    SCOPED_MUTEX_LOCK(ss_mutex);

    sslog_individual_t* start_movement = sslog_node_get_property(node, route_individual,
                                                                 PROPERTY_HASSTARTMOVEMENT);

    if (start_movement == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Not start movement in route");
        return;
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Start movement with id %s", start_movement->entity.uri);

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

        if (point_a == NULL || point_b == NULL) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Null start and end point in movement");
            return;
        }

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

        sslog_node_remove_individual_with_local(node, movement);
        sslog_node_remove_individual_with_local(node, point_a);
        if (i == movement_count - 1) {
            sslog_node_remove_individual_with_local(node, point_b);
        }
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "%d movements found", movement_count);
    st_on_schedule_request_ready(movement_array, movement_count);

    sslog_node_remove_property(node, route_individual, PROPERTY_HASSTARTMOVEMENT, NULL);
    sslog_node_remove_property(node, route_individual, PROPERTY_HASPOINT, NULL);

    ptr_array_free(&ptr_array);
    for (int i = 0; i < movement_count; i++) {
        st_free_point(&movement_array[i].point_a);
        st_free_point(&movement_array[i].point_b);
    }
}

// * error checks
// * memory cleanups
static void search_subscription_handler(sslog_subscription_t* sub) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "search_subscription_handler");
    SCOPED_MUTEX_LOCK(ss_mutex);

    sslog_node_populate(node, request_individual);

    list_t* inserted_individuals = sslog_get_properties(request_individual, PROPERTY_HASPOINT);

    int points_number = list_count(inserted_individuals);
    struct Point point_array[points_number];

    if (points_number == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Empty search response");
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

    // TODO: not sure if it is allowed to unsubscribe within handler
    sslog_sbcr_unsubscribe(sub);
    sslog_free_subscription(sub);
    sslog_node_remove_individual_with_local(node, request_individual);

    request_individual = NULL;
    sub_search_request = NULL;
}

// * error checks
// * memory cleanups
static bool subscribe_route_processed(sslog_individual_t* route) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating schedule subscription");

    sub_schedule_request = sslog_new_subscription(node, true);

    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);
    sslog_sbcr_add_individual(sub_schedule_request, route, properties);

    sslog_sbcr_set_changed_handler(sub_schedule_request, &schedule_subscription_handler);
    sslog_sbcr_set_error_handler(sub_schedule_request, &schedule_subscription_error_handler);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Subscribing schedule request");
    if (sslog_sbcr_subscribe(sub_schedule_request) != SSLOG_ERROR_NO) {
        sslog_free_subscription(sub_schedule_request);
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't subscribe schedule response: %s", sslog_error_get_text(node));
        return false;
    }

    return true;
}

/**
 * Убедиться что пользователь с заданным id присутствует в smartspace'е
 */
// * error checks
// * memory cleanups
static bool ensure_user_individual(const char *id) {
    sslog_individual_t* tmp = sslog_node_get_individual_by_uri(node, id);

    if (sslog_error_get_last_code() != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Error receiving user from smartspace: %s",
                            sslog_error_get_last_text());
        return false;
    }

    if (tmp == NULL) {
        tmp = sslog_new_individual(CLASS_USER, id);
        if (sslog_node_insert_individual(node, tmp) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert user individual to smartspace: %s",
                                sslog_error_get_last_text());
            return false;
        }

        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "No user found, created new");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Existing user found with id %s",
                            tmp->entity.uri);
    }

    user_individual = tmp;
    return true;
}

/**
 * Загрузить Schedule и Route для текущего пользователя
 */
static bool load_existing_schedule() {
    schedule_individual = sslog_node_get_property(node, user_individual, PROPERTY_PROVIDE);

    // Check PROPERTY_PROVIDE got successful
    if (sslog_error_get_last_code() != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't get current schedule: %s",
                            sslog_error_get_last_text());
        return false;
    }

    if (schedule_individual != NULL) {
        route_individual = sslog_node_get_property(node, schedule_individual, PROPERTY_HASROUTE);
    } else {
        route_individual = NULL;
    }

    // Check PROPERTY_HASROUTE got successful
    if (sslog_error_get_last_code() != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't get current route: %s",
                            sslog_error_get_last_text());
        return false;
    }

    // Если route уже присутствует для данного пользователя, сразу подписываемся
    if (route_individual != NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Existing route found with id %s",
                            route_individual->entity.uri);

        if (!subscribe_route_processed(route_individual)) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't subscribe_route_processed: %s",
                                sslog_error_get_last_text());
            return false;
        }

        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Populate existing route_individual");
        if (sslog_node_populate(node, route_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't populate existing route_individual: %s",
                                sslog_error_get_last_text());
            return false;
        }
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "No existing route found");
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Exit load_existing_schedule");

    return true;
}

bool st_initialize(const char *user_id, const char *kp_name, const char *smart_space_name,
                   const char *address, int port) {
    init_rand();

    if (!is_smartspace_initialized) {
        if (sslog_init() != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Error sslog_init %s",
                                sslog_error_get_text(node));
            return false;
        } else {
            is_smartspace_initialized = true;
            __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Sslog_init ok");
        }
    }

    if (!is_ontology_registered) {
        is_ontology_registered = true;
        register_ontology();
    }

    node = create_node_resolve(kp_name, smart_space_name, address, port);

    if (node == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Error create node %s",
                            sslog_error_get_text(node));
        return false;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Node created");
    }

    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't join node");
        sslog_free_node(node);
        node = NULL;
        return false;
    }

    if (!ensure_user_individual(user_id)) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "ensure_user_individual failed");
        return false;
    }

    if (!load_existing_schedule()) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "load_existing_schedule failed");
        return false;
    }

    return true;
}

void st_shutdown() {
    if (node != NULL) {
        sslog_node_leave(node);
        node = NULL;
    }

    sslog_shutdown();

    is_ontology_registered = false;
    is_smartspace_initialized = false;
}

// * error checks
// * memory cleanups
bool st_update_user_location(double lat, double lon) {
    SCOPED_MUTEX_LOCK(ss_mutex);

    user_lat = lat;
    user_lon = lon;

    sslog_individual_t* new_location_individual
            = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(new_location_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(new_location_individual, PROPERTY_LONG, double_to_string(lon));

    if (sslog_node_insert_individual(node, new_location_individual) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert existing user location");
        return false;
    }

    sslog_individual_t* existing_user_location = sslog_node_get_property(node, user_individual,
                                                                         PROPERTY_HASLOCATION);
    if (existing_user_location != NULL) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Location already exists");
    } else {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Location not exists");
    }

    if (sslog_node_update_property(node, user_individual, PROPERTY_HASLOCATION,
                                   (void*) existing_user_location, new_location_individual) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't assign new existing user location");
        return false;
    }

    user_location = new_location_individual;

    if (route_individual != NULL) {
        // Update route, so transport_kp can easily subscribe to updates with one subscription
        if (sslog_node_remove_property(node, route_individual, PROPERTY_UPDATED, NULL) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't update 'updated' property");
            return false;
        }

        if (sslog_node_update_property(node, route_individual, PROPERTY_UPDATED, NULL,
                                       rand_uuid("updated")) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't update 'updated' property");
            return false;
        }
    }

    if (existing_user_location != NULL) {
        if (sslog_node_remove_individual_with_local(node, existing_user_location) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't remove old user location");
            return false;
        }
    }

    return true;

// TODO: Delete old user location
//if (existing_user_location != NULL) {
//    sslog_node_remove_individual_with_local(node, existing_user_location);
//}
}

static bool cancel_search_subscription() {
    if (sub_search_request != NULL) {
        if (sslog_sbcr_unsubscribe(sub_search_request) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Error unsubscribing existing subscripton: %d %s",
                                sslog_error_get_last_code(), sslog_error_get_last_text());
            // FIXME: sslog_sbcr_unsubscribe always returns 305 error, ignore for now
            //return false;
        }

        sslog_free_subscription(sub_search_request);
        sub_search_request = NULL;
    }

    if (request_individual != NULL) {
        if (sslog_node_remove_individual(node, request_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Error removing request individual: %s",
                                sslog_error_get_last_text());
            return false;
        }

        request_individual = NULL;
    }

    return true;
}

// TODO: will not work if no user location available
// * error checks
// * memory cleanups
bool st_post_search_request(double radius, const char *pattern) {
    CLEANUP_INDIVIDUAL sslog_individual_t* location_individual = NULL;
    CLEANUP_INDIVIDUAL sslog_individual_t* region_individual = NULL;

    SCOPED_MUTEX_LOCK(ss_mutex);

    if (!cancel_search_subscription()) {
        return false;
    }

    // Create location individual
    location_individual = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));
    sslog_insert_property(location_individual, PROPERTY_LAT, double_to_string(user_lat));
    sslog_insert_property(location_individual, PROPERTY_LONG, double_to_string(user_lon));

    if (sslog_node_insert_individual(node, location_individual) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert location individual: %s",
                            sslog_error_get_last_text());
        return false;
    }

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating searchrequest individual. Location id %s",
                        location_individual->entity.uri);

    // Create circle region individual
    region_individual = sslog_new_individual(CLASS_CIRCLEREGION, rand_uuid("circle_search_region"));
    sslog_insert_property(region_individual, PROPERTY_RADIUS, double_to_string(radius));
    if (sslog_node_insert_individual(node, region_individual) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert region individual: %s",
                            sslog_error_get_last_text());
        return false;
    }

    // Create request individual
    sslog_individual_t* request_individual_l = sslog_new_individual(CLASS_SEARCHREQUEST,
                                                                    rand_uuid("search_request"));
    sslog_insert_property(request_individual_l, PROPERTY_USELOCATION, location_individual);
    sslog_insert_property(request_individual_l, PROPERTY_INREGION, region_individual);
    sslog_insert_property(request_individual_l, PROPERTY_SEARCHPATTERN, (void*) pattern);

    if (sslog_node_insert_individual(node, request_individual_l) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't insert request individual: %s",
                            sslog_error_get_last_text());
        sslog_remove_individual(request_individual_l);
        return false;
    }

    request_individual = request_individual_l;

    // Subscribe to update
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating subsription");

    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);

    sslog_subscription_t* sub_search_request_l = sslog_new_subscription(node, true);

    sslog_sbcr_add_individual(sub_search_request_l, request_individual, properties);
    sslog_sbcr_set_changed_handler(sub_search_request_l, &search_subscription_handler);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Subscribing search request");
    if (sslog_sbcr_subscribe(sub_search_request_l) != SSLOG_ERROR_NO) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Can't subscribe response: %s",
                            sslog_error_get_text(node));
        sslog_free_subscription(sub_search_request_l);
        sslog_node_remove_individual_with_local(node, request_individual);
        request_individual = NULL;
        return false;
    }

    return true;
}

static void clear_local_route_and_schedule(void) {
    sslog_remove_individual(schedule_individual);
    sslog_remove_individual(route_individual);
    schedule_individual = NULL;
    route_individual = NULL;
}

static void remove_old_points_from_route(sslog_individual_t* route_individual) {
    list_t* has_points = sslog_get_properties(route_individual, PROPERTY_HASPOINT);

    list_head_t* iter;
    list_for_each(iter, &has_points->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = entry->data;
        sslog_node_remove_individual_with_local(node, point_individual);
    }

    sslog_node_remove_property(node, route_individual, PROPERTY_HASPOINT, NULL);

    list_free_with_nodes(has_points, NULL);
}

// * error checks
// * memory cleanups
bool st_post_schedule_request(struct Point* points, int points_count, const char* tsp_type) {
    if (points_count == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "No points in schedule request");
        return true;
    }

    SCOPED_MUTEX_LOCK(ss_mutex);

    if (route_individual != NULL) {
        // Удаляем все текущие параметры запроса (hasPoint)
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Updating existing schedule and route");
        remove_old_points_from_route(route_individual);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Creating new schedule and route");

        schedule_individual = sslog_new_individual(CLASS_SCHEDULE, rand_uuid("schedule"));
        route_individual = sslog_new_individual(CLASS_ROUTE, rand_uuid("route"));

        sslog_insert_property(schedule_individual, PROPERTY_HASROUTE, route_individual);

        if (sslog_node_insert_individual(node, route_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Can't insert route individual: %s",
                                sslog_error_get_last_text());
            clear_local_route_and_schedule();
            return false;
        }

        if (sslog_node_insert_individual(node, schedule_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Can't insert schedule individual %s",
                                sslog_error_get_last_text());
            clear_local_route_and_schedule();
            return false;
        }

        if (sslog_node_insert_property(node, user_individual, PROPERTY_PROVIDE,
                                       schedule_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Can't assign provide property to user %s",
                                sslog_error_get_last_text());
            clear_local_route_and_schedule();
            return false;
        }

        if (!subscribe_route_processed(route_individual)) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Can't subscribe to route_individual");
            return false;
        }
    }

    // Состояние smartspace'а: user->schedule->route, route не содержит точек, может содержать updated, processed и tspType

    // TODO: добавлять точки в одну транзакцию
    // TODO: удалять старые точки или придумать какой-нибудь GarbageCollectorKP
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Inserting %d points with tsptype %s", points_count, tsp_type);

    for (int i = 0; i < points_count; i++) {
        sslog_individual_t* point_individual = create_poi_individual(node, points[i].lat,
                                                                     points[i].lon,
                                                                     points[i].title, "nocategory");
        if (sslog_node_insert_property(node, route_individual, PROPERTY_HASPOINT,
                                       point_individual) != SSLOG_ERROR_NO) {
            __android_log_print(ANDROID_LOG_ERROR, APPNAME,
                                "Error inserting point into route: %s", sslog_error_get_last_text());
            return false;
        }
    }

    if (sslog_node_remove_property(node, route_individual, PROPERTY_TSPTYPE, NULL) != SSLOG_ERROR_NO) {
        return false;
    }

    if (sslog_node_update_property(node, route_individual, PROPERTY_TSPTYPE, NULL, (void*) tsp_type) != SSLOG_ERROR_NO) {
        return false;
    }

    if (sslog_node_remove_property(node, route_individual, PROPERTY_UPDATED, NULL) != SSLOG_ERROR_NO) {
        return false;
    }

    if (sslog_node_update_property(node, route_individual, PROPERTY_UPDATED, NULL, rand_uuid("updated")) != SSLOG_ERROR_NO) {
        return false;
    }

    return true;
}

static void test() {

}