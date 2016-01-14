#include "test-loader.h"

#include <stdlib.h>

#include "common.h"
#include "st_point.h"

#define BUF_SIZE (500)

static double TEST_POINTS[] = {
    61.78464963754708,34.34697389602661,
    61.787351, 34.354369,
    61.787026, 34.365269,
    61.787859, 34.375612,
    61.792167, 34.369475,
    61.783023, 34.360334,
    61.78734806396082,34.34877634048462,
    61.78857546526346,34.3526816368103,
    61.783696001642575,34.35381889343262,
    61.78598873590156,34.36145782470703,
    61.77894767194888,34.376220703125,
    61.773589701610355,34.35892581939697,
    61.78779439737812,34.37544822692871
};

static void test_load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    int count = (sizeof(TEST_POINTS) / sizeof(double)) / 2;
    struct Point* points = malloc(count * sizeof(struct Point));

    for (int i = 0; i < count; i++) {
        char point_id[BUF_SIZE];
        char point_title[BUF_SIZE];

        rand_uuid_buf("testpoint", point_id, BUF_SIZE);
        rand_uuid_buf("testtitle", point_title, BUF_SIZE);

        st_init_point(&points[i], point_id, point_title, TEST_POINTS[2 * i], TEST_POINTS[2 * i + 1]);
    }

    *out_points = points;
    *out_point_count = count;
}

struct LoaderInterface create_test_loader(void) {
    struct LoaderInterface loader = {
        .load_points = &test_load_points
    };

    return loader;
}
