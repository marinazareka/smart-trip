#include <stdlib.h>
#include <unistd.h>
#include <signal.h>

#include <smartslog.h>
#include <sys/time.h>
#include <smartslog/entity.h>
#include <smartslog/triple.h>
#include <smartslog/high_api.h>

#include "ontology.h"
#include "common.h"

double TEST_RADIUS = 20000.0;
double TEST_LAT = 61.78;
double TEST_LONG = 34.35;
char TEST_PATTERN[1000] = "петроз";

// начальное значение точки отсчета ожидания
#define init_timeout 5000;

// таймер отсчета ожидания (кол-во попыток)
int timeout = init_timeout;

// флаг прерывания
static volatile bool cont = true;

// узел доступа к ИП
sslog_node_t* node;

char *userId = NULL;
char *searchId = NULL;
char *scheduleId = NULL;

// количество узлов в маршруте
int route_size_max = 15;
int route_current_size = 0;

// статистика
struct timeval stat_init_time;
struct timeval stat_first_search;
struct timeval stat_last_search;
struct timeval stat_route_start_search;
struct timeval stat_route_finish_search;
int stat_search_count = 0;
int stat_scount[3];
double stat_route_distance = 0;

static void signal_handler(int sig) {
    fprintf(stdout, "Finish all (timeout=%i)\n", timeout);
    cont = false;
    sslog_sbcr_stop_all();
}

static void subscribe_signals() {
    signal(SIGINT, &signal_handler);
    signal(SIGTERM, &signal_handler);
}

/*
 * Вычитаем из dest init;
 */
static void timeval_diff(struct timeval *dest, const struct timeval init) {
    dest->tv_sec = dest->tv_sec - init.tv_sec;
    dest->tv_usec = dest->tv_usec - init.tv_usec;
    if (dest->tv_usec < 0) {
        dest->tv_sec--;
        dest->tv_usec += 1000000;
    }
}

static sslog_individual_t* update_user_location(sslog_individual_t* user_individual, double lat, double lon) {
    sslog_individual_t* new_location_individual = sslog_new_individual(CLASS_LOCATION, rand_uuid("user_location"));

    sslog_insert_property(new_location_individual, PROPERTY_LAT, double_to_string(lat));
    sslog_insert_property(new_location_individual, PROPERTY_LONG, double_to_string(lon));

    sslog_node_insert_individual(node, new_location_individual);

    const void* existing_location = sslog_get_property(user_individual, PROPERTY_HASLOCATION);
    if (existing_location != NULL) {
        printf("Location already exists\n");
    } else {
        printf("Location not exists\n");
    }
    printf("Use location: (%f, %f)\n", lat, lon);

    sslog_node_insert_property(node, user_individual, PROPERTY_HASLOCATION, new_location_individual);
    //sslog_node_update_property(node, user_individual, PROPERTY_HASLOCATION, (void*) existing_location, new_location_individual);
    return new_location_individual;
    // TODO: existing search request still uses old location
}

static sslog_individual_t* publish_search_request(sslog_individual_t* location_individual, char *pattern, double radius) {
    sslog_individual_t* region_individual = sslog_new_individual(CLASS_CIRCLEREGION, rand_uuid("circle_search_region"));
    sslog_insert_property(region_individual, PROPERTY_RADIUS, double_to_string(radius));

    if (searchId == NULL) {
        char *request = rand_uuid("search_request");
        searchId = malloc(sizeof (char) *(strlen(request) + 2));
        strcpy(searchId, request);
    }

    sslog_individual_t* request_individual = sslog_new_individual(CLASS_SEARCHREQUEST, searchId);
    sslog_insert_property(request_individual, PROPERTY_USELOCATION, location_individual);
    sslog_insert_property(request_individual, PROPERTY_SEARCHPATTERN, pattern);
    sslog_insert_property(request_individual, PROPERTY_INREGION, region_individual);

    sslog_node_insert_individual(node, region_individual);
    sslog_node_insert_individual(node, request_individual);
    return request_individual;
}

static bool process_route_result(sslog_individual_t* route_individual) {
        struct timeval cur_time;
        gettimeofday(&cur_time, NULL);

        timeval_diff(&cur_time, stat_route_start_search);

        stat_route_finish_search.tv_sec = cur_time.tv_sec;
        stat_route_finish_search.tv_usec = cur_time.tv_usec;
        sslog_node_populate(node, route_individual);
        
        // обход по передвижениям
        list_t * movements = sslog_get_properties(route_individual, PROPERTY_HASMOVEMENT);
        
        double distance = 0;
        list_head_t* iter;
        
        list_for_each(iter, &movements->links) {
            list_t* entry = list_entry(iter, list_t, links);
            sslog_individual_t* movement = (sslog_individual_t*) entry->data;

            sslog_node_populate(node, movement);
            const char * length = sslog_get_property(movement, PROPERTY_LENGTH);
            double length_value = atof(length);
            //fprintf(stdout, "%s:%i: movement length = %s\n", __FILE__, __LINE__, length);
            
            distance +=length_value;
        }
        
        stat_route_distance = distance;
        
        list_free_with_nodes(movements, NULL);

    return true;
}

static bool process_subscription_result(sslog_individual_t* request_individual) {
    fprintf(stdout, "Parse search result for %s\n", sslog_entity_get_uri(request_individual));
    sslog_node_populate(node, request_individual);
    
    sslog_individual_t* schedule_individual = NULL;
    sslog_individual_t* route_individual = NULL;
    sslog_individual_t* user_individual = NULL;

    if (scheduleId == NULL) {
        char *chId = rand_uuid("schedule");
        scheduleId = malloc(sizeof (char) *(strlen(chId) + 2));
        strcpy(scheduleId, chId);

        schedule_individual = sslog_new_individual(CLASS_SCHEDULE, scheduleId);

        user_individual = sslog_get_individual(userId);
        sslog_node_populate(node, user_individual);
        
        if (user_individual == NULL) {
            fprintf(stderr, "No user individual for id %s\n", userId);
            return false;
        }

        route_individual = sslog_new_individual(CLASS_ROUTE, rand_uuid("route"));

        sslog_node_insert_property(node, user_individual, PROPERTY_PROVIDE, schedule_individual);

        sslog_insert_property(schedule_individual, PROPERTY_HASROUTE, route_individual);
    } else {
        schedule_individual = sslog_get_individual(scheduleId);
        sslog_node_populate(node, schedule_individual);

        route_individual = (sslog_individual_t*)sslog_get_property(schedule_individual, PROPERTY_HASROUTE);
        sslog_node_populate(node, route_individual);
    }
    
    fprintf(stdout, "schedule <%s>, user <%s>\n", scheduleId, userId);


    list_t* inserted_individuals = sslog_get_properties(request_individual, PROPERTY_HASPOINT);

    int counter = 0;
    list_head_t* iter;

    list_for_each(iter, &inserted_individuals->links) {
        list_t* entry = list_entry(iter, list_t, links);
        sslog_individual_t* point_individual = (sslog_individual_t*) entry->data;
        sslog_node_populate(node, point_individual);

        double lat, lon;
        get_point_coordinates(node, point_individual, &lat, &lon);

        // Reuse previous point
        if (route_individual != NULL && route_current_size < route_size_max) {
            sslog_insert_property(route_individual, PROPERTY_HASPOINT, point_individual);
            route_current_size ++;
        }

        counter++;
    }

    list_free_with_nodes(inserted_individuals, NULL);
    
    // статистика
    if (stat_search_count < 3) {
        stat_scount[stat_search_count - 1] = counter;
    }

    // сам маршрут
    if (route_current_size > 0) {
        if (route_individual != NULL) {
            sslog_insert_property(route_individual, PROPERTY_UPDATED, "0");
            gettimeofday(&stat_route_start_search, NULL);
        }

        //sslog_node_insert_individual(node, user_individual);
        sslog_node_insert_individual(node, route_individual);
        sslog_node_insert_individual(node, schedule_individual);
    }

    fprintf(stdout, "Finish parsing result (%i)\n", counter);

    return true;
}

void search_handler(sslog_subscription_t* sub) {
    // восстановление таймера
    fprintf(stdout, "Process search subscription\n");

    sslog_sbcr_changes_t* ch = sslog_sbcr_get_changes_last(sub);

    //sslog_sbcr_ch_print(ch);

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
        if (strcmp(triple->subject, searchId) != 0) {
            fprintf(stdout, "Unknown search id %s\n", triple->subject);
            continue;
        }

        //sslog_individual_t* individual = sslog_node_get_individual_by_uri(node, triple->subject);    
        sslog_individual_t* individual = sslog_get_individual(triple->subject);

        if (individual == NULL) {
            fprintf(stderr, "No individual for triple %s\n", triple->subject);
            continue;
        }

        // нашли нужный индивид. обновляем статистику
        timeout = init_timeout;

        stat_search_count++;
        struct timeval cur_time;
        gettimeofday(&cur_time, NULL);

        timeval_diff(&cur_time, stat_init_time);

        if (stat_search_count == 1) {
            stat_first_search.tv_sec = cur_time.tv_sec;
            stat_first_search.tv_usec = cur_time.tv_usec;
        }
        stat_last_search.tv_sec = cur_time.tv_sec;
        stat_last_search.tv_usec = cur_time.tv_usec;

        process_subscription_result(individual);
        break;
    }

    list_free_with_nodes(triples, NULL);
}

void route_handler(sslog_subscription_t* sub) {
    // восстановление таймера
    timeout = init_timeout;

    sslog_sbcr_changes_t* ch = sslog_sbcr_get_changes_last(sub);

    //sslog_sbcr_ch_print(ch);

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

        //fprintf(stdout, "Process triple %s->%s->%s\n", triple->object, triple->predicate, triple->subject);

        sslog_individual_t* individual = sslog_get_individual((char *) triple->subject);
        
        if (individual == NULL) {
            fprintf(stderr, "No individual for triple %s\n", triple->subject);
            continue;
        }
        
        sslog_triple_t * ind_triple = sslog_individual_to_triple(individual);
        
        //fprintf(stdout, "%s:%i: Found triple %s->%s->%s\n", __FILE__, __LINE__, ind_triple->object, ind_triple->predicate, ind_triple->subject);
        
        if (strcmp(ind_triple->object, sslog_entity_get_uri(CLASS_ROUTE)) == 0) {
            fprintf(stdout, "%s:%i: Found route\n", __FILE__, __LINE__);

            process_route_result(individual);

            break;
        } else {
            //fprintf(stdout, "%s:%i: Unknown class for individual %s vs. %s\n", __FILE__, __LINE__, ind_triple->object, sslog_entity_get_uri(CLASS_ROUTE));
            continue;
        }
    }

    list_free_with_nodes(triples, NULL);
}

static void subscribe_response(sslog_individual_t* request_individual) {
    list_t* properties = list_new();
    list_add_data(properties, PROPERTY_PROCESSED);

    // подписываемся на результаты поиска точек
    sslog_subscription_t* subscription = sslog_new_subscription(node, true);
    sslog_sbcr_add_individual(subscription, request_individual, properties);
    sslog_sbcr_set_changed_handler(subscription, search_handler);

    fprintf(stdout, "Subscribing search response\n");
    if (sslog_sbcr_subscribe(subscription) != SSLOG_ERROR_NO) {
        sslog_free_subscription(subscription);
        fprintf(stderr, "Can't subscribe request\n");
        return;
    }

    // подписываемся на результаты построения маршрута
    sslog_subscription_t* route_subscription = sslog_new_subscription(node, true);
    sslog_triple_t* triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_PROCESSED), SSLOG_TRIPLE_ANY,
            SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_LIT);
    sslog_sbcr_add_triple_template(route_subscription, triple);
    sslog_sbcr_set_changed_handler(route_subscription, &route_handler);

    fprintf(stdout, "Subscribing route response\n");
    if (sslog_sbcr_subscribe(route_subscription) != SSLOG_ERROR_NO) {
        sslog_free_subscription(route_subscription);
        fprintf(stderr, "Can't subscribe request\n");
        return;
    }

    fprintf(stdout, "Process requests...\n");
    // основной цикл работы
    for (; cont == true && timeout > 0;) {
        usleep(10);
        timeout--;
    }

    // отключение подписок
    fprintf(stdout, "Unsubscribing response (timeout=%i)\n", timeout);
    sslog_sbcr_unsubscribe(subscription);
    sslog_free_subscription(subscription);
}

void print_help(const char *name) {
    printf("Usage: %s [-abqrh]\n\n", name);
    printf("\t-a=latitude\t Set latitude value\n");
    printf("\t-b=longitude\t Set longitude value\n");
    printf("\t-q=query\t Set search query\n");
    printf("\t-r=radius\t Set search radius in meters\n");
    printf("\t-h\t Print this help\n\n");
    exit(0);
}

int main(int argc, char *argv[]) {
    // значение параметра getopt
    int optval = 0;
    while ((optval = getopt(argc, argv, "a:b:q:r:h")) != -1) {
        switch (optval) {
            case 'a': TEST_LAT = atof(optarg);
                break;
            case 'b': TEST_LONG = atof(optarg);
                break;
            case 'q': strcpy(TEST_PATTERN, optarg);
                break;
            case 'r': TEST_RADIUS = atof(optarg);
                break;
            case 'h': print_help(argv[0]);
                break;
            case '?': printf("Error found !\n");
                break;
        };
    };

    subscribe_signals();

    init_rand();
    sslog_init();
    register_ontology();

    node = create_node("user_kp", "config.ini");
    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
        fprintf(stderr, "Can't join node\n");
        return 1;
    }

    gettimeofday(&stat_init_time, NULL);
    char * user = rand_uuid("user");
    userId = malloc(sizeof (char) * (strlen(user) + 2));
    strcpy(userId, user);
    printf("User is %s\n", user);
    // Publish user individual
    sslog_individual_t* user_individual = sslog_new_individual(CLASS_USER, user);
    sslog_node_insert_individual(node, user_individual);

    // Update user location
    sslog_individual_t* location_individual = update_user_location(user_individual, TEST_LAT, TEST_LONG);

    // поисковый запрос (тест 1)
    sslog_individual_t* request_individual = publish_search_request(location_individual, (char *) TEST_PATTERN, TEST_RADIUS);

    // маршрут (тест 2)
    subscribe_response(request_individual);

    //sslog_node_remove_individual(node, request_individual);
    //sslog_node_remove_individual(node, user_individual);

    sslog_node_leave(node);
    sslog_shutdown();

    // печать статистики
    fprintf(stderr, "user:%s;init_time;%li,%li;searchers;%i;first_search;%li,%li;last_search;%li,%li;scount1;%i;scount2;%i;scount3;%i;route_search;%li,%li;distance;%f\n",
            userId,
            stat_init_time.tv_sec, stat_init_time.tv_usec,
            stat_search_count,
            stat_first_search.tv_sec, stat_first_search.tv_usec,
            stat_last_search.tv_sec, stat_last_search.tv_usec,
            stat_scount[0], stat_scount[1], stat_scount[2],
            stat_route_finish_search.tv_sec, stat_route_finish_search.tv_usec, stat_route_distance);

    free(userId);
    free(searchId);
}

