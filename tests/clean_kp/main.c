
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <stdarg.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static void remove_properties(sslog_node_t* node, sslog_property_t* prop, ...) {
    va_list va;
    va_start(va, prop);
    
    while (prop != NULL) {
        sslog_triple_t* template = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(prop), SSLOG_TRIPLE_ANY,
                SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_URI);
        sslog_node_remove_triple(node, template);
        prop = va_arg(va, sslog_property_t*);
    }

    va_end(va);
}

static void remove_individuals(sslog_node_t* node, sslog_class_t* cls, ...) {
    va_list va;
    va_start(va, cls);
    
    while (cls != NULL) {
        sslog_triple_t* template = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, SSLOG_TRIPLE_RDF_TYPE, sslog_entity_get_uri(cls),
                SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_URI);
        sslog_node_remove_triple(node, template);
        cls = va_arg(va, sslog_class_t*);
    }

    va_end(va);
}

int main(void) {
    init_rand();
	sslog_init();
    register_ontology();

    sslog_node_t* node = create_node("clean_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    remove_properties(node,
            PROPERTY_HASLOCATION,
            PROPERTY_HASMOVEMENT,
            PROPERTY_HASNEXTMOVEMENT,
            PROPERTY_HASPOINT,
            PROPERTY_HASROUTE,
            PROPERTY_HASSTARTMOVEMENT,
            PROPERTY_INREGION,
            PROPERTY_ISENDPOINT,
            PROPERTY_ISSTARTPOINT,
            PROPERTY_LAT,
            PROPERTY_LONG,
            PROPERTY_NAME,
            PROPERTY_POICATEGORY,
            PROPERTY_POITITLE,
            PROPERTY_PROCESSED,
            PROPERTY_PROVIDE,
            PROPERTY_RADIUS,
            PROPERTY_SEARCHPATTERN,
            PROPERTY_TSPTYPE,
            PROPERTY_UPDATED,
            PROPERTY_URL,
            PROPERTY_USELOCATION,
            PROPERTY_USEROAD,
            NULL);

    remove_individuals(node,
            CLASS_POINT,
            CLASS_SEARCHREQUEST,
            CLASS_LOCATION,
            CLASS_CIRCLEREGION,
            CLASS_ROUTE,
            CLASS_SCHEDULE,
            CLASS_USER,
            CLASS_POI,
            CLASS_MOVEMENT,
            NULL
            );


	sslog_node_leave(node);
	sslog_shutdown();
}
