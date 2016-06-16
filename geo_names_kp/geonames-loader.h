/* 
 * File:   geonames-loader.h
 * Author: kulakov
 *
 * Created on 21 апреля 2016 г., 11:05
 */

#ifndef GEONAMES_LOADER_H
#define	GEONAMES_LOADER_H

#ifdef	__cplusplus
extern "C" {
#endif

#include "loader.h"

static const char BASE_GEONAMES_SERVER[] = "http://api.geonames.org/";
struct LoaderInterface create_geonames_loader(const char* server);

// количество возвращаемых элементов или 0 если неограниченно
extern int return_size;

#ifdef	__cplusplus
}
#endif

#endif	/* GEONAMES_LOADER_H */

