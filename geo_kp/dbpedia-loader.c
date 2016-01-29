#include "dbpedia-loader.h"

#include <string.h>
#include <stdio.h>
#include <math.h>

static char endpoint[1024];

// static double EARTH_RADIUS = 
//
static double to_radians(double degrees) {
    return M_PI * degrees / 180;
}

static double to_degrees(double radians) {
    return 180 * radians / M_PI;
}

struct BoundingBox {
    double lat1, lon1, lat2, lon2;
};

static const double EARTH_RADIUS = 6371000;

// Get lat/lon aligned bounding box for query
// All angles in radians
// http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
static void find_bounding_box(double lat, double lon, double radius, struct BoundingBox* out) {
    double r = radius / EARTH_RADIUS; 

    out->lat1 = lat - r;
    out->lat2 = lat + r;

    // double latT = asin(sin(lat) / cos(r)); 
    double dLon = asin(sin(r) / cos(lat));

    out->lon1 = lon - dLon;
    out->lon2 = lon + dLon;
}

static void find_bounding_box_degree(double lat, double lon, double radius, struct BoundingBox* out) {
    find_bounding_box(to_radians(lat), to_radians(lon), radius, out);
    out->lat1 = to_degrees(out->lat1);
    out->lat2 = to_degrees(out->lat2);
    out->lon1 = to_degrees(out->lon1);
    out->lon2 = to_degrees(out->lon2);
}

static void load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    double lat_rad = to_radians(lat);
    double lon_rad = to_radians(lon);

}

static const char* get_name(void) {
    return "dbpedia_loader";
}

struct LoaderInterface create_dbpedia_loader(const char* endpoint_) {
    snprintf(endpoint, 1024, "%s", endpoint_);
    struct LoaderInterface loader = {
        .load_points = load_points,
        .get_name = get_name
    };

    return loader;
}


void dbpedia_loader_test(void) {
    struct BoundingBox bbox;

    find_bounding_box_degree(61.787335, 34.354328, 1000, &bbox);

    printf("%lf %lf %lf %lf\n", bbox.lat1, bbox.lon1, bbox.lat2, bbox.lon2);
}
