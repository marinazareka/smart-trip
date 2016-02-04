
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <stdarg.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static sslog_node_t* node;

static void process_route_individual(sslog_individual_t* route) {
    printf("Processing route individual %s\n", sslog_entity_get_uri(route));
}

static void subscription_handler(sslog_subscription_t* sub) {
    sslog_sbcr_changes_t* ch = sslog_sbcr_get_changes_last(sub);

    if (ch == NULL) {
        return;
    }

    
    list_t* triples = sslog_sbcr_ch_get_triples(ch, SSLOG_ACTION_INSERT);
    if (triples == NULL) {
        return;
    }

    list_head_t* iter;
    list_for_each(iter, &triples->links) {
        sslog_triple_t* triple = list_entry(iter, list_t, links)->data;
        sslog_individual_t* individual = sslog_node_get_individual_by_uri(node, triple->subject);    

        if (individual == NULL) {
            fprintf(stderr, "No individual for triple %s\n", triple->object);
            continue;
        }

        sslog_triple_t* individual_triple = sslog_individual_to_triple(individual);
        if (strcmp(individual_triple->object, sslog_entity_get_uri(CLASS_ROUTE)) == 0) {
            process_route_individual(individual);
        } else {
            printf("Not a route individual %s %s:%s\n", sslog_entity_get_uri(individual),
                    individual_triple->object, sslog_entity_get_uri(CLASS_ROUTE));
        }

        sslog_remove_individual(individual);
    }

}

static void work(sslog_node_t* node) {
    sslog_subscription_t* sub = sslog_new_subscription(node, false);
    
    sslog_triple_t* triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_PROCESSED), SSLOG_TRIPLE_ANY,
            SSLOG_RDF_TYPE_URI, SS_RDF_TYPE_LIT);
    sslog_sbcr_add_triple_template(sub, triple);

    if (sslog_sbcr_subscribe(sub) != SSLOG_ERROR_NO) {
        fprintf(stderr, "Error subscribing subscription %s\n", sslog_error_get_last_text());
        return;
    }
    
    do {
        subscription_handler(sub);
        if (sslog_sbcr_wait(sub) != SSLOG_ERROR_NO) {
            fprintf(stderr, "Error waiting subscription %s\n", sslog_error_get_last_text());
            return;
        }
    } while(true);

}

int main(void) {
    init_rand();
	sslog_init();
    register_ontology();

    node = create_node("time_plan_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    work(node);


	sslog_node_leave(node);
	sslog_shutdown();
}
