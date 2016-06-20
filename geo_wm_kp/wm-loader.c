#define _GNU_SOURCE

#include "wm-loader.h"

#include <stdlib.h>
#include <curl/curl.h>
#include <stdio.h>
#include <string.h>

#include "st_point.h"

#include "cJSON.h"

static const char* URL_FORMAT = "%s/?key=%s&function=place.search&q=%s&lat=%lf&lon=%lf&format=json&data_blocks=main%%2Clocation%%2C"
                                "&page=1&count=50&distance=%d";
static const char* URL_PREFIX = "http://api.wikimapia.org";
static char wm_key[1024];

// количество возвращаемых элементов или 0 если бесконечно
int return_size = 0;

static char* get_points_as_json(double lat, double lon, double radius, const char* pattern) {
    char* ret;
    CURL* curl = curl_easy_init();
    char* url;
    struct MemoryStruct memory_struct = {.memory = NULL, .size = 0};

    if (pattern == NULL) {
        pattern = "";
    }

    char* pattern_escaped = curl_easy_escape(curl, pattern, 0);

    // Build URL
    int bytes = asprintf(&url, URL_FORMAT, URL_PREFIX, wm_key, pattern_escaped, lat, lon, (int) radius);
    if (bytes < 0) {
        fputs("Can't allocate memory for Wikimapia URL\n", stderr);
        abort();
    }

    curl_free(pattern_escaped);

#ifdef DEBUG
    fprintf(stderr, "Wikimapia request: %s\n", url);
#endif
    curl_easy_setopt(curl, CURLOPT_URL, url);
    free(url);
    
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &memory_struct);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &WriteMemoryCallback);
    
    CURLcode code = curl_easy_perform(curl);
    if (code == CURLE_OK) {
        ret = memory_struct.memory; 
    } else {
        fprintf(stderr, "%s:%i: CURL error %s\n", __FILE__, __LINE__, curl_easy_strerror(code));
        ret = NULL;
    }

    curl_easy_cleanup(curl);
    return ret;
}

static void load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    if (strchr(pattern, '"') != NULL) {
        fprintf(stderr, "%s:%i: Wrong pattern received\n", __FILE__, __LINE__);
        return;
    }

    char* points_json = get_points_as_json(lat, lon, radius, pattern);
    if (points_json == NULL) {
        fprintf(stderr, "%s:%i: Can't load points\n", __FILE__, __LINE__);
        return;
    }

#ifdef DEBUG
    fprintf(stderr, "Loaded points json %s\n", points_json);
#endif
    cJSON* json = cJSON_Parse(points_json);

    if (cJSON_GetObjectItem(json, "count") == NULL) {
        fprintf(stderr, "%s:%i: Wikimapia output format error\n", __FILE__, __LINE__);
        return;
    }

    // TODO: only first page
    int page_count = cJSON_GetObjectItem(json, "count")->valueint;
    int found = cJSON_GetObjectItem(json, "found")->valueint;

    cJSON* places = cJSON_GetObjectItem(json, "places");
    int count = cJSON_GetArraySize(places);

    struct Point* points = malloc(count * sizeof(struct Point));

    fprintf(stderr, "%s:%i: Found %d points\n", __FILE__, __LINE__, count);

    int i = 0;
    for (cJSON* place = places->child; place != NULL ; place = place->next) {
        if (return_size > 0 && i>=return_size)
            break;
        
        int id = cJSON_GetObjectItem(place, "id")->valueint;
        cJSON* title_object = cJSON_GetObjectItem(place, "title");

        if (title_object == NULL) {
#ifdef DEBUG
            fprintf(stderr, "%s:%i: Untitled place found\n", __FILE__, __LINE__);
#endif
            continue;
        }

        const char* title = title_object->valuestring;
        if (strlen(title) == 0) {
#ifdef DEBUG
            fprintf(stderr, "Place with empty title found\n");
#endif
            continue;
        }


        cJSON* location = cJSON_GetObjectItem(place, "location");
        if (location == NULL) {
#ifdef DEBUG
            fprintf(stderr, "Point without location found\n");
#endif
            continue;
        }

        double lat = cJSON_GetObjectItem(location, "lat")->valuedouble;
        double lon = cJSON_GetObjectItem(location, "lon")->valuedouble;

        char* id_str;
        asprintf(&id_str, "wm%d", id);
        st_init_point(&points[i++], id_str, title, lat, lon);
        free(id_str);
    }

    cJSON_Delete(json);

    *out_points = points;
    *out_point_count = i;
}

static const char* get_name(void) {
    return "wm_loader";
}

struct LoaderInterface create_wm_loader(const char* key) {
    snprintf(wm_key, 1024, "%s", key);

    struct LoaderInterface loader = {
        .load_points = load_points,
        .get_name = get_name
    };

    return loader;
}
