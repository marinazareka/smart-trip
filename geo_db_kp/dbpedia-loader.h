#ifndef SMARTTRIP_DBPEDIA_LOADER_
#define SMARTTRIP_DBPEDIA_LOADER_

#include "loader.h"

struct LoaderInterface create_dbpedia_loader(const char* endpoint);
void dbpedia_loader_test(void);

#endif
