#include <stdio.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>

#include <smartslog.h>
#include "ontology.h"
#include "common.h"

/**
 * @brief Точка подключения к интеллектуальному пространству.
 * 
 * Используется для организации работы с ИП и подписки.
 */
static sslog_node_t* node;

/**
 *  @brief Flag to stop the program activity.
 *
 * This flag is used when CTR+C is pressed 
 * or when subscription indication is a error.
 */
static sig_atomic_t exit_flag = 0;

/*********************/
// обработка сигналов
static void signal_callback_handler(int signum);

// регистрация подписок
static void make_subscriptions(sslog_node_t* node);

// время посадки перед движением
static void time_landing_handler(sslog_subscription_t* sub);
/*********************/

int main(void) {
    init_rand();

//    if (getenv("ST_TEST") != NULL) {
//        return do_test();
//    }

    // Sets handler for CTR+C
    signal(SIGINT, &signal_callback_handler);
    
    // подключение к интеллектуальному пространству
    sslog_init();
    register_ontology();

    node = create_node("rating_kp", "config.ini");
    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
	fprintf(stderr, "Can't join node\n");
	return 1;
    }

    // регистрация подписок и обработка
    make_subscriptions(node);

    // отключение от интеллектуального пространства
    sslog_node_leave(node);
    sslog_shutdown();
}

/**
 * @brief Function to handle ctrl+c (SIGINT) signal. If you press 
 * CTRL+C twice, then program will be closed immediately.
 *
 * @param[in] signum signal identifier number.
 */
static void signal_callback_handler(int signum)
{
    if (exit_flag == 0) {
        fprintf(stderr, "Please waiting, i'm going to shutdown the program...");
        exit_flag = 1;  // Set a flag to end the program.
    } else {
        fprintf(stderr, "OK, am exiting...");
        exit(signum);
    }
}

static void make_subscriptions(sslog_node_t* node)
{
    // время посадки перед передвижением
    sslog_subscription_t* sub_tl = sslog_new_subscription(node, true);
    sslog_sbcr_add_class(sub_tl, CLASS_MOVEMENT);
    sslog_sbcr_set_changed_handler(sub_tl, &time_landing_handler);

    if (sslog_sbcr_subscribe(sub_tl) != SSLOG_ERROR_NO) {
        fprintf(stderr, "Error subscribing to CLASS_MOVEMENT\n");
        return;
    }

    fprintf(stderr, "process requests...\n");
    // основной цикл работы
    for (;exit_flag == 0;) {
        sleep(10);
    }

    // отключение подписок
    sslog_sbcr_unsubscribe(sub_tl);
    sslog_free_subscription(sub_tl);

}

static void time_landing_handler(sslog_subscription_t* sub) {
    fprintf(stderr, "=== (time_landing_handler) start\n");
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
        if (strcmp(individual_triple->object, sslog_entity_get_uri(CLASS_MOVEMENT)) == 0) {
            printf("Current movement: %s -> %s \n", individual_triple->object, individual_triple->subject);
        } else {
            printf("Not a movement individual %s %s:%s\n", sslog_entity_get_uri(individual),
                    individual_triple->object, sslog_entity_get_uri(CLASS_MOVEMENT));
        }

        sslog_remove_individual(individual);
    }
    fprintf(stderr, "--- (time_landing_handler) finish\n");

//TODO: реализовать установку времени посадки перед движением
}
