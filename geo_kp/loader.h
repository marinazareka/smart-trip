#ifndef SMARTTRIP_POINT_LOADER_
#define SMARTTRIP_POINT_LOADER_

struct Point;

struct LoaderInterface {
    void (*load_points)(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count);
    const char* (*get_name)(void);
};

#endif
