/* 
 * File:   main.c
 * Author: kulakov
 *
 * Created on 21 апреля 2016 г., 10:53
 */

#include <stdio.h>
#include <stdlib.h>

// описание онтологии
#include "ontology.h"

// общие функции
#include "common.h"

// описание точки
#include "st_point.h"

// интерфейс загрузчика
#include "loader.h"

// общий функционал
#include "geo_common.h"

// специфика работы с geonames
#include "geonames-loader.h"

/*
 * создание загрузчика
 */

static bool create_loader(struct LoaderInterface* loader) {
    char* namesloader_server = get_config_value("config.ini", "GeoNamesLoader", "Server");
    bool created = true;

    if (namesloader_server != NULL) {
        *loader = create_geonames_loader(namesloader_server);
    } else {
        *loader = create_geonames_loader(BASE_GEONAMES_SERVER);
    }

    free(namesloader_server);

    return created;
}



/*
 * основная функция
 */
int main(void) {

    struct LoaderInterface loader;

    init_rand();

    if (!create_loader(&loader)) {
        fprintf(stderr, "Something was wrong\n");
        return 1;
    }

    sslog_init();
    register_ontology();

    static char kp_name[1000];
    snprintf(kp_name, sizeof(kp_name), "geo_kp_%s", loader.get_name());

    sslog_node_t* node = create_node(kp_name, "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}
    
    geo_common_serve_kp(node, loader);

	sslog_node_leave(node);
	sslog_shutdown();

    return (EXIT_SUCCESS);
}

