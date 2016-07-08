#ifndef GEO_COMMON_
#define GEO_COMMON_

#include <smartslog.h>
#include "loader.h"

void geo_common_serve_kp(sslog_node_t* node, struct LoaderInterface loader);

// перевод метров в радианы
double length_to_radians(double length);

#endif
