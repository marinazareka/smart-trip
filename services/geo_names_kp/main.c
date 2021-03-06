/* 
 * File:   main.c
 * Author: kulakov
 *
 * Created on 21 апреля 2016 г., 10:53
 */

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

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
        free(namesloader_server);
    } else {
        *loader = create_geonames_loader(BASE_GEONAMES_SERVER);
    }
    
    char *config_return_size = get_config_value("config.ini", "GeoNamesLoader", "ReturnSize");
    if (config_return_size != NULL) {
        return_size = atoi(config_return_size);
        free(config_return_size);
    }


    return created;
}

struct LoaderInterface loader;


static void signal_handler(int sig) {
    if (loader.isProcessed) {
    fprintf(stdout, "Finish all\n");
    loader.isProcessed = false;
    sslog_sbcr_stop_all(); 
    } else {
        exit(0);
    }
}

static void subscribe_signals() {
    signal(SIGINT, &signal_handler);
    signal(SIGTERM, &signal_handler);
}

/*
 * основная функция
 */
int main(void) {
    subscribe_signals();
    
    init_rand();

    if (!create_loader(&loader)) {
        fprintf(stderr, "%s:%i: Something was wrong\n", __FILE__, __LINE__);
        return 1;
    }

    sslog_init();
    register_ontology();

    static char kp_name[1000];
    snprintf(kp_name, sizeof(kp_name), "geo_kp_%s", loader.get_name());

    sslog_node_t* node = create_node(kp_name, "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "%s:%i: Can't join node\n", __FILE__, __LINE__);
		return 1;
	}
    
    geo_common_serve_kp(node, loader);

    sslog_sbcr_unsubscribe_all(node, true);
    sslog_node_leave(node);
    sslog_shutdown();

    return (EXIT_SUCCESS);
}

