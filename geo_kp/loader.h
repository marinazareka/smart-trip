#ifndef SMARTTRIP_POINT_LOADER_
#define SMARTTRIP_POINT_LOADER_

struct Point;

struct LoaderInterface {
    void (*load_points)(double lat, double lon, double radius, struct Point** out_points, int* out_point_count);
};

#endif
