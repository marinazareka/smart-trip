
#define _GNU_SOURCE
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <stdarg.h>
#include <string.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

static sslog_node_t* node;

static time_t parse_to_time_t(const char* iso_date_time) {
    struct tm t;
    memset(&t, 0, sizeof(t));
    if (strptime(iso_date_time, "%Y-%m-%dT%H:%M:%S", &t) == NULL) {
        return 0;
    }
    return mktime(&t);
}

static bool parse_date_time_interval(const char* date_time_interval, time_t* start_tt, time_t* end_tt) {
    const char* slash_position = strchr(date_time_interval, '/');

    if (slash_position == NULL || *(slash_position + 1) == '\0') {
        return false;
    }

    char* start_date_time = strndup(date_time_interval, slash_position - date_time_interval);
    char* end_date_time = strdup(slash_position + 1);

    *start_tt = parse_to_time_t(start_date_time);
    *end_tt = parse_to_time_t(end_date_time);

    free(start_date_time);
    free(end_date_time);

    if (*start_tt == 0 || *end_tt == 0) {
        return false;
    }

    return true;
}

static const char* to_iso_time(time_t t) {
    struct tm ts;
    gmtime_r(&t, &ts);

    static char ret[1000];
    strftime(ret, sizeof(ret), "%Y-%m-%dT%H:%M:%S", &ts);

    return ret;
}

static void process_route_individual(sslog_individual_t* route) {
    printf("Processing route individual %s\n", sslog_entity_get_uri(route));
    sslog_node_populate(node, route);

    const char* interval = sslog_get_property(route, PROPERTY_SCHEDULEINTERVAL);
    printf("Times interval %s\n", interval);

    sslog_individual_t* start_movement = (sslog_individual_t*) sslog_get_property(route, PROPERTY_HASSTARTMOVEMENT);

    sslog_individual_t* iter_movement = (sslog_individual_t*) start_movement;

    time_t time_counter = time(NULL);
    while (iter_movement != NULL) {
        sslog_node_populate(node, iter_movement);

        const char* time_length = sslog_get_property(iter_movement, PROPERTY_LENGTH);
        printf("Movement %s %s\n", sslog_entity_get_uri(iter_movement), time_length);

        if (time_length != NULL) {
            int time = (int) parse_double(time_length);
            sslog_node_insert_property(node, iter_movement, PROPERTY_STARTTIME, (void*) to_iso_time(time_counter));
            time_counter += time;
            sslog_node_insert_property(node, iter_movement, PROPERTY_ENDTIME, (void*) to_iso_time(time_counter));
            printf("End time %s\n", to_iso_time(time_counter));
        }

        iter_movement = (sslog_individual_t*) sslog_get_property(iter_movement, PROPERTY_HASNEXTMOVEMENT);
    }

    printf("Insert PROPERTY_PROCESSED\n");
    // sslog_node_insert_property(node, route, PROPERTY_PROCESSED, long_to_string(time(NULL)));
}

static void subscription_handler(sslog_subscription_t* sub) {
    printf("subscription_handler_out\n");
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
    sslog_subscription_t* sub = sslog_new_subscription(node, true);
    sslog_sbcr_set_changed_handler(sub, &subscription_handler);
    
    sslog_sbcr_set_changed_handler(sub, &subscription_handler);
    sslog_triple_t* triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_PROCESSED), SSLOG_TRIPLE_ANY,
            SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_LIT);
    sslog_sbcr_add_triple_template(sub, triple);

    if (sslog_sbcr_subscribe(sub) != SSLOG_ERROR_NO) {
        fprintf(stderr, "Error subscribing subscription %s\n", sslog_error_get_last_text());
        return;
    }

    for (;;) {
        sleep(10);
    }
}

static int do_test() {
    tzset();

    if (parse_to_time_t("2016-02-01T13:00:00") == 0) {
        printf("Can't parse valid iso datetime\n");
    }

    time_t start_tt, end_tt;
    parse_date_time_interval("2016-02-01T13:00:00.000/2016-05-01T23:00:00.000", &start_tt, &end_tt);
    printf("Unix time %d %d\n", (int) start_tt, (int) end_tt);

    const char* invalid[] = {
        "a2016-02-01T13:00:00.000/2016-05-01T23:00:00.000",
        "/2016-05-01T23:00:00.000",
        "2016-02-01T13:00:00.000/",
        "",
    };

    for (unsigned i = 0; i < sizeof(invalid) / sizeof(invalid[0]); i++) {
        const char* invalid_str = invalid[i];
        if (parse_date_time_interval(invalid_str, &start_tt, &end_tt)) {
            printf("Wrong datetime interval %s parsed as valid\n", invalid_str);
        }
    }

    return 0;
}

int main(void) {
    if (getenv("ST_TEST") != NULL) {
        return do_test();
    }

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
