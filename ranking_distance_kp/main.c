/* 
 * File:   main.c
 * Author: kulakov
 *
 * Created on 22 апреля 2016 г., 16:00
 */

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
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

// время посадки перед движением
static void user_moving_handler(sslog_subscription_t* sub) {
    fprintf(stderr, "=== (time_landing_handler) start\n");
    sslog_sbcr_changes_t* ch = sslog_sbcr_get_changes_last(sub);

    sslog_sbcr_ch_print(ch);
    
    if (ch == NULL) {
        return;
    }

    
    list_t* triples = sslog_sbcr_ch_get_triples(ch, SSLOG_ACTION_INSERT);
    if (triples == NULL) {
        return;
    }
    fprintf(stderr, "--- (user_moving_handler) finish\n");
}

/**
 * @brief оформление подписки на появление/обновление точек и изменение местоположения пользователя
 * @param node Точка подключения к ИП
 */
static void make_subscriptions(sslog_node_t* node)
{
    // изменение местоположения пользователя
    sslog_subscription_t* sub_tl = sslog_new_subscription(node, true);
    sslog_sbcr_set_changed_handler(sub_tl, &user_moving_handler);

    sslog_triple_t* triple = sslog_new_triple_detached(SSLOG_TRIPLE_ANY, sslog_entity_get_uri(PROPERTY_HASLOCATION), SSLOG_TRIPLE_ANY,
            SSLOG_RDF_TYPE_URI, SSLOG_RDF_TYPE_LIT);
    sslog_sbcr_add_triple_template(sub, triple);

    
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


/*
 * @brief Main function
 */
int main(void) {

    // Sets handler for CTR+C
    signal(SIGINT, &signal_callback_handler);

    init_rand();

        // подключение к интеллектуальному пространству
    sslog_init();
    register_ontology();

    node = create_node("ranking_distance_kp", "config.ini");
    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
	fprintf(stderr, "Can't join node\n");
	return 1;
    }

    // регистрация подписок и обработка
    make_subscriptions(node);

    // отключение от интеллектуального пространства
    sslog_node_leave(node);
    sslog_shutdown();

    return (EXIT_SUCCESS);
}

