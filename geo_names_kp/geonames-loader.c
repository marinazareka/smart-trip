#define _GNU_SOURCE

#include "geonames-loader.h"
#include "st_point.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <curl/curl.h>
#include <libxml/parser.h>
#include <libxml/tree.h>

#include "common.h"
#include "geo_common.h"

// используемый сервер, например http://api.geonames.org/
static char geo_names_server[1024];
// формат запроса к серверу http://www.geonames.org/export/geonames-search.html
//static const char URL_FORMAT[] = "%s/search?name_startsWith=%s&username=smarttrip";
static const char URL_FORMAT[] = "%s/search?username=smarttrip";

// количество возвращаемых элементов или 0 если бесконечно
int return_size = 0;

static xmlNodePtr findNodeByName(xmlNodePtr rootNode, const char *name) {
    if (xmlStrEqual(rootNode->name, (const xmlChar *)name) == 1)
        return rootNode;
    xmlNodePtr childNode = xmlFirstElementChild(rootNode);
    while(childNode != NULL) {
        xmlNodePtr ret = findNodeByName(childNode, name);
        if (ret != NULL)
            return ret;
        else
            childNode = xmlNextElementSibling(childNode);
    }
    return NULL;
}

// обработка запроса на получение объектов
static void load_points(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count) {
    // если поиск по координатам
    if (pattern == NULL) {
        //TODO: реализовать поиск ближайших мест по координатам
        fprintf(stderr, "%s:%i: NOT IMPLEMENTED!!!\n", __FILE__, __LINE__);
        return;
    }
    
    //call request to the server
    char* ret;
    char buffer[300];
    CURL* curl = curl_easy_init();
    char *url = malloc(sizeof(char) * 2048);
    if (url == NULL) {
        fprintf(stderr, "%s:%i: Cant' allocate memory for url\n", __FILE__, __LINE__);
        return;
    }
    // 1. базовый адрес
    sprintf(url, URL_FORMAT, geo_names_server);
    // 2. поисковая фраза
    if (pattern != NULL) {
        char* pattern_escaped = curl_easy_escape(curl, pattern, 0);
        url = strcat(url, "&name_startsWith=");
        url = strncat(url, pattern_escaped, 2048-strlen(url));
        curl_free(pattern_escaped);
    }
    // 3. размер результата
    if (return_size > 0) {
        url = strcat(url, "&maxRows=");
        snprintf(buffer, 300, "%d", return_size);
        url = strcat(url, buffer);
    }
    // 4. область
    if (radius > 0) {
        double diff = length_to_radians(radius);
        snprintf(buffer, 300, "&east=%f&west=%f&south=%f&north=%f", lon+diff,lon-diff,lat-diff,lat+diff);
        url = strncat(url,buffer,2048-strlen(url));
    }
    
    struct MemoryStruct memory_struct = {.memory = NULL, .size = 0};


#ifdef DEBUG
    fprintf(stderr, "Geonames request: %s\n", url);
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
    
    // parse response
    if (ret == NULL) {
#ifdef DEBUG
        fprintf(stderr, "Empty return value\n");
#endif
        return;
    }

    //fprintf(stderr, "%s\n", ret);
    xmlDocPtr doc = xmlReadMemory(memory_struct.memory, memory_struct.size, "result.xml", NULL, 0);
    free(memory_struct.memory);

    // количество найденных точек
    int countPoints = 0;
    struct PointListItem *startPoint = NULL;
    struct PointListItem *lastPoint = NULL;
    
    xmlNodePtr root_element = xmlDocGetRootElement(doc);
    
    if (root_element == NULL) {
        fprintf(stderr, "%s:%i: Can't load root element", __FILE__, __LINE__);
    } else {
        xmlNodePtr currentNode = xmlFirstElementChild(root_element);
    
        while(currentNode != NULL) {
            if (return_size > 0 && countPoints > return_size)
                break;
            if (xmlStrEqual(currentNode->name, (const xmlChar *)"geoname") == 1) {
                // нашли точку, добавляем элемент в конец списка
                if (startPoint == NULL) {
                    startPoint = malloc(sizeof(struct PointListItem));
                    lastPoint = startPoint;
                } else {
                    lastPoint->next = malloc(sizeof(struct PointListItem));
                    lastPoint = lastPoint->next;
                }
                lastPoint->next = NULL;
                lastPoint->value = malloc(sizeof(struct Point));
                countPoints++;
                
                // парсим контент точки
                xmlNodePtr currentPointNode = xmlFirstElementChild(currentNode);
                while(currentPointNode != NULL) {
                    xmlChar *value = xmlNodeGetContent(currentPointNode);
                    if (xmlStrEqual(currentPointNode->name, (const xmlChar*)"name"))
                        lastPoint->value->title = strdup(value);
                    if (xmlStrEqual(currentPointNode->name, (const xmlChar*)"lat"))
                        lastPoint->value->lat = atof(value);
                    if (xmlStrEqual(currentPointNode->name, (const xmlChar*)"lng"))
                        lastPoint->value->lon = atof(value);
                    if (xmlStrEqual(currentPointNode->name, (const xmlChar*)"geonameId")) {
                        lastPoint->value->id = malloc(strlen(value) + 10);
                        sprintf(lastPoint->value->id, "geonames%s",value);
                    }
                    
                    xmlFree(value);
                    currentPointNode = xmlNextElementSibling(currentPointNode);
                }
                //fprintf(stderr,"point %s %s %f %f\n", lastPoint->value->id, lastPoint->value->title, lastPoint->value->lat, lastPoint->value->lon);
            }
        
            currentNode = xmlNextElementSibling(currentNode);
        }
    }
    
    //строим результирующий массив
    *out_point_count = countPoints;
    *out_points = malloc(countPoints * sizeof(struct Point));
    int i = 0;
    while (startPoint != NULL) {
        lastPoint = startPoint->next;
        (*out_points)[i].id = startPoint->value->id;
        (*out_points)[i].lat = startPoint->value->lat;
        (*out_points)[i].lon = startPoint->value->lon;
        (*out_points)[i].title = startPoint->value->title;
        free(startPoint);
        startPoint = lastPoint;
        i++;
    }
    
    // очищаем работу с XML
    xmlFreeDoc(doc);
    xmlCleanupParser();
}

// имя КР
static const char* get_name(void) {
    return "geonames_loader";
}

// установка привязок к серверу
struct LoaderInterface create_geonames_loader(const char* server) {
    snprintf(geo_names_server, 1024, "%s", server);
    
    struct LoaderInterface loader = {
        .load_points = load_points,
        .get_name = get_name
    };

    return loader;
}
