#ifndef SMARTTRIP_WM_LOADER_
#define SMARTTRIP_WM_LOADER_

#include "loader.h"

struct LoaderInterface create_wm_loader(const char* key);

// количество возвращаемых элементов или 0 если неограниченно
extern int return_size;

#endif
