#include "st_point.h"

#include <stdlib.h>
#include <string.h>

void st_init_point(struct Point *point, const char *id, const char *title, double lat, double lon) {
    point->id = strdup(id);
    point->title = strdup(title);
    point->lat = lat;
    point->lon = lon;
}

void st_free_point(struct Point *point) {
    free(point->id);
    free(point->title);
    point->id = NULL;
    point->title = NULL;
}

/**
 * Free points array of given size
 * Doesn't frees array memory, only point content
 */
void st_free_point_array(struct Point* point_array, int size) {
    for (int i = 0; i < size; i++) {
        st_free_point(&point_array[i]);
    }
}

void st_init_point_clone(struct Point *point, struct Point *source) {
    point->id = strdup(source->id);
    point->title = strdup(source->title);
    point->lat = source->lat;
    point->lon = source->lon;
}
