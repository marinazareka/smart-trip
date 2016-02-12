#define _GNU_SOURCE

#include "dbpedia-loader.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

#include <curl/curl.h>
#include "st_point.h"
#include "cJSON.h"
#include "common.h"

static char endpoint[1024];

static const char MIME_ESCAPED[] = "application%2Fsparql-results%2Bjson";

static const char QUERY_FORMAT[] = "%s?default-graph-uri=&query=%s&format=%s";
static const char SPARQL_FORMAT[] = " \
SELECT ?poi, ?label, ?lat, ?long WHERE { ?poi a geo:SpatialThing ; foaf:name ?label ;  geo:lat ?lat ; geo:long ?long . \
FILTER ( ?long > %lf && ?long < %lf && ?lat > %lf && ?lat < %lf) . \
FILTER (regex(?label, \"%s\")) \
} \
";


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

// Haversine great circle distance
static double distance(double lat1, double lon1, double lat2, double lon2) {
    double sLat = sin((lat2 - lat1) / 2);
    double sLon = sin((lon2 - lon1) / 2);

    double d = 2 * EARTH_RADIUS * asin(sqrt(sLat * sLat + cos(lat1) * cos(lat2) * sLon * sLon));
    return d;
}


static double distance_degree(double lat1, double lon1, double lat2, double lon2) {
    return distance(to_radians(lat1), to_radians(lon1), to_radians(lat2), to_radians(lon2));
}

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

static char* load_points_as_json(double lat, double lon, double radius, const char* pattern) {
    struct BoundingBox bbox;
    find_bounding_box_degree(lat, lon, radius, &bbox);

    char* sparql_query;
    char* http_query;

    asprintf(&sparql_query, SPARQL_FORMAT, bbox.lon1, bbox.lon2, bbox.lat1, bbox.lat2, pattern);
    
    CURL* curl = curl_easy_init();
    char* sparql_query_escaped = curl_easy_escape(curl, sparql_query, 0);

    asprintf(&http_query, QUERY_FORMAT, endpoint, sparql_query_escaped, MIME_ESCAPED);

    printf("%s\n", http_query);

    curl_easy_setopt(curl, CURLOPT_URL, http_query);

    struct MemoryStruct memory_struct = {NULL, 0};
    
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &memory_struct);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &WriteMemoryCallback);

    free(http_query);
    free(sparql_query);
    curl_free(sparql_query_escaped);
    
    CURLcode code = curl_easy_perform(curl);
    char* ret = NULL;
    if (code == CURLE_OK) {
        ret = memory_struct.memory; 
    } else {
        fprintf(stderr, "CURL error %s\n", curl_easy_strerror(code));
        ret = NULL;
    }

    curl_easy_cleanup(curl);
    return ret;
}

static bool parse_json_number(cJSON* json, double* number) {
    if (json->type == cJSON_Number) {
        *number = json->valuedouble;
        return true;
    } else if (json->type == cJSON_String) {
        return sscanf(json->valuestring, "%lf", number) != 0;
    } else {
        return false;
    }
}

static bool process_json(double cLat, double cLon, double radius, struct Point** out_points, int* out_point_count, cJSON* json) {
    cJSON* results = cJSON_GetObjectItem(json, "results");
    if (results == NULL) {
        fprintf(stderr, "Null results object\n");
        return false;
    }

    cJSON* bindings = cJSON_GetObjectItem(results, "bindings");
    if (bindings == NULL) {
        fprintf(stderr, "Null bindings object\n");
        return false;
    }

    FlatArray points_array;
    flat_array_init(&points_array, sizeof(struct Point));

    for (cJSON* poi = bindings->child; poi != NULL; poi = poi->next)  {
        cJSON* label_json = cJSON_GetObjectItem(poi, "label");
        cJSON* poi_json = cJSON_GetObjectItem(poi, "poi");
        cJSON* lat_json = cJSON_GetObjectItem(poi, "lat");
        cJSON* lon_json = cJSON_GetObjectItem(poi, "long");

        if (label_json == NULL || lat_json == NULL || lon_json == NULL || poi_json == NULL) {
            continue;
        }

        cJSON* label_value = cJSON_GetObjectItem(label_json, "value");
        cJSON* poi_value = cJSON_GetObjectItem(poi_json, "value");
        cJSON* lat_value = cJSON_GetObjectItem(lat_json, "value");
        cJSON* lon_value = cJSON_GetObjectItem(lon_json, "value");

        if (label_value == NULL || lat_value == NULL || lon_value == NULL || poi_value == NULL) {
            continue;
        }

        char* label = label_value->valuestring;
        char* poi = poi_value->valuestring;

        double lat, lon;
        
        if (!parse_json_number(lat_value, &lat)) {
            continue;
        }

        if (!parse_json_number(lon_value, &lon)) {
            continue;
        }

        if (distance_degree(cLat, cLon, lat, lon) < radius) {
            struct Point* point = flat_array_insert(&points_array);
            st_init_point(point, poi, label, lat, lon);
        }
    }

    *out_points = points_array.array;
    *out_point_count = points_array.size;

    return true;
}

static void load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    *out_point_count = 0;

    if (strchr(pattern, '"') != NULL) {
        fprintf(stderr, "Wrong pattern argument\n");
        return;
    }

    char* points_json = load_points_as_json(lat, lon, radius, pattern);
    if (points_json == NULL) {
        fprintf(stderr, "Can't load points\n");
        return;
    }

    printf("%s\n", points_json);

    cJSON* json = cJSON_Parse(points_json);

    if (json == NULL || !process_json(lat, lon, radius, out_points, out_point_count, json)) {
        fprintf(stderr, "Can't process json\n");
        *out_point_count = 0;
        *out_points = NULL;
    }

    cJSON_Delete(json);
    free(points_json);
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

    printf("AABB %lf %lf %lf %lf\n", bbox.lat1, bbox.lon1, bbox.lat2, bbox.lon2);

    printf("Hypo %lf\n", distance_degree(bbox.lat1, bbox.lon1, bbox.lat2, bbox.lon2));
    printf("Test %lf\n", distance_degree(36.12, -86.67, 33.94, -118.40));

    struct Point* points_array;
    int count = 0;

    // load_points(55.755833, 37.617778, 10000, "Hotel", &points_array, &count);
    // printf("Loaded %d points\n", count); 

    load_points(61.78, 34.35, 10000, "Z̝̱͓̠͡A̪̞̥̟̪͙͞L̨G͢Ó͉̪̱!̙̗̣̻͍͘", &points_array, &count);
    printf("Loaded %d points\n", count); 

    for (int i = 0; i < count; i++) {
        printf("%s\n", points_array[i].title);
    }
}
