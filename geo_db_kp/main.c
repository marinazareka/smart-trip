#include <unistd.h>
#include <smartslog.h>
#include <signal.h>

#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#include "ontology.h"
#include "common.h"
#include "st_point.h"

#include "loader.h"
#include "geo_common.h"
#include "dbpedia-loader.h"

static bool create_loader(struct LoaderInterface* loader) {
    char* endpoint = get_config_value("config.ini", "Sparql", "Endpoint");
    bool created = true;

    if (endpoint != NULL) {
        *loader = create_dbpedia_loader(endpoint);
    } else {
        created = false;
    }

    free(endpoint);

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

int main(void) {
    subscribe_signals();

    init_rand();

    if (!create_loader(&loader)) {
        fprintf(stderr, "No point loader specified\n");
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
}
