#include "smarttrip.h"
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#include <pthread.h>
#include "st_point.h"

static void* handle_search_request(void* data) {
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

void st_initialize(const char *user_id) {

}

void st_shutdown() {

}

void st_update_user_location(double lat, double lon) {

}

void st_post_search_request(double radius, const char *pattern) {
    pthread_t thread;
    pthread_create(&thread, NULL, &handle_search_request, NULL);
    pthread_detach(thread);
}

void st_post_schedule_request(struct Point *points, int points_count) {

}


