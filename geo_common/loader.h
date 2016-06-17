#ifndef SMARTTRIP_POINT_LOADER_
#define SMARTTRIP_POINT_LOADER_

#include <sys/types.h>
#include <stdbool.h>

struct Point;

struct LoaderInterface {
    // функция обработки запросов
    void (*load_points)(double lat, double lon, double radius, const char* pattern, struct Point** out_points, int* out_point_count);
    
    // им сервиса
    const char* (*get_name)(void);
    
    // флаг обработки запросов (false = выход)
    bool isProcessed;
};

struct MemoryStruct {
    char *memory;
    size_t size;
};

size_t WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp); 

#endif
