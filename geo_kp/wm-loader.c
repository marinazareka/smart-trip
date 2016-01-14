#include "wm-loader.h"


#define _GNU_SOURCE
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


// source http://curl.haxx.se/libcurl/c/getinmemory.html
struct MemoryStruct {
    char *memory;
    size_t size;
};

static size_t
WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *)userp;

    mem->memory = realloc(mem->memory, mem->size + realsize + 1);
    if(mem->memory == NULL) {
        /* out of memory! */ 
        printf("not enough memory (realloc returned NULL)\n");
        return 0;
    }

    memcpy(&(mem->memory[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;

    return realsize;
}

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

    fprintf(stderr, "Wikimapia request: %s\n", url);
    curl_easy_setopt(curl, CURLOPT_URL, url);
    free(url);
    
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &memory_struct);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &WriteMemoryCallback);
    
    CURLcode code = curl_easy_perform(curl);
    if (code == CURLE_OK) {
        ret = memory_struct.memory; 
    } else {
        fprintf(stderr, "CURL error %s\n", curl_easy_strerror(code));
        ret = NULL;
    }

    curl_easy_cleanup(curl);
    return ret;
}

static void load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    char* points_json = get_points_as_json(lat, lon, radius, pattern);
    if (points_json == NULL) {
        fprintf(stderr, "Can't load points\n");
        return;
    }

    fprintf(stderr, "Loaded points json %s\n", points_json);
    cJSON* json = cJSON_Parse(points_json);

    if (cJSON_GetObjectItem(json, "count") == NULL) {
        fprintf(stderr, "Wikimapia error\n");
        return;
    }

    // TODO: only first page
    int page_count = cJSON_GetObjectItem(json, "count")->valueint;
    int found = cJSON_GetObjectItem(json, "found")->valueint;

    cJSON* places = cJSON_GetObjectItem(json, "places");
    int count = cJSON_GetArraySize(places);

    struct Point* points = malloc(count * sizeof(struct Point));

    fprintf(stderr, "Found %d points\n", count);

    int i = 0;
    for (cJSON* place = places->child; place != NULL ; place = place->next) {
        int id = cJSON_GetObjectItem(place, "id")->valueint;
        cJSON* title_object = cJSON_GetObjectItem(place, "title");

        if (title_object == NULL) {
            fprintf(stderr, "Untitled place found\n");
            continue;
        }

        const char* title = title_object->valuestring;
        if (strlen(title) == 0) {
            fprintf(stderr, "Place with empty title found\n");
            continue;
        }


        cJSON* location = cJSON_GetObjectItem(place, "location");
        if (location == NULL) {
            fprintf(stderr, "Point without location found\n");
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

struct LoaderInterface create_wm_loader(const char* key) {
    snprintf(wm_key, 1024, "%s", key);

    struct LoaderInterface loader = {
        .load_points = load_points
    };

    return loader;
}
