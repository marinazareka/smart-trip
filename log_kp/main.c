/**
 * @file consumer_kp.c
 * @Author Aleksandr A. Lomov <lomov@cs.karelia.ru>
 * @date   Created on August 19, 2012
 * @brief  Cunsumer of the example 'Hello World' with subscription.
 *
 * This is file is a part of the C KPI library project and it has the same licence. 
 *
 * @section LICENSE
 * 
 * C KPI library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * C KPI Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SmartSlog KP Library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 *
 *
 * @section DESCRIPTION
 *
 * This file is a part of the 'Hello World' example with subscription.
 * This KP make a subscription to triples using <KP - says - *> triple-pattern.
 * It gets information about chnages and output old and new values. 
 * To stop the KP use CTRL+C 
 *
 * Copyright (C) SmartSlog Team (Aleksandr A. Lomov).
 * All rights reserved.
 * Mail-list: smartslog@cs.karelia.ru
 */


#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <sys/time.h>

#include <ckpi/ckpi.h>

// ---Constants---
// Describes connection to the SS.
#define KP_SS_NAME "X"
//#define KP_SS_ADDRESS "194.85.173.9"  // Public PetrSU SIB
#define KP_SS_ADDRESS "127.0.0.1"  // Local SIB
//#define KP_SS_PORT 20203
#define KP_SS_PORT 10010

// Name of the KP.
#define KP_NAME "HelloConsumerSub"

// Parts of the triple for the subscription.
#define KP_NS_URI "http://X/Hello/#"
#define KP_SUBJECT KP_NS_URI"KP"
#define KP_PREDICATE KP_NS_URI"says"

// Time to wait a subscription's indication.
#define KP_SUBS_CHECKING_TIME 5000

/**
 *  @brief Flag to stop the program activity.
 *
 * This flag is used when CTR+C is pressed 
 * or when subscription indication is a error.
 */
static sig_atomic_t exit_flag = 0;

/**
 * @brief Contains data for subscripition.
 */
typedef struct subs_s 
{
    ss_subs_info_t info;
    bool is_subscribed;
    ss_triple_t *new_triples;
    ss_triple_t *old_triples;
} subs_t;

/****************** Static functions list **********************/
static void signal_callback_handler(int signum);

static int make_subscription(ss_info_t *info, subs_t *subs);
static void handle_subscription(ss_info_t *info, subs_t *subs);
static void handle_unsubscription(ss_info_t *info, subs_t *subs);
static int handle_indication(ss_info_t *info, subs_t *subs, int timeout);

static void update_subscription_triples(
            subs_t *subs, ss_triple_t *new_triples, ss_triple_t *old_triples);
static void print_triples(ss_triple_t *triple);
/*******************************************************/        


/**
 * @brief Starts the KP, initializes state and cals all necessary functions.
 *
 * @return 0.
 */
int main()
{
    // Sets handler for CTR+C
    signal(SIGINT, &signal_callback_handler);

    // Initialize smart space information and data about subscription.
    ss_info_t info;
    subs_t subs;
        
    ss_init_space_info(&info, KP_SS_NAME, KP_SS_ADDRESS, KP_SS_PORT);
    
    subs.is_subscribed = false;
    subs.new_triples = NULL;
    subs.old_triples = NULL;
       
   // In the smart space the name of the KP must be unique, if you try to 
   // join without leave, then you received an error.
    if (ss_join(&info, KP_NAME) == -1) {
        printf("Can't join to SS.\n");
        return 0;
    }

    printf("KP join to SS.\n");
    
    // Create a subscription to <KP-says-*> triples.
    if (make_subscription(&info, &subs) != 0) {
        printf("Can't subscribe.\n");
        ss_leave(&info);    
        return 0;
    }
    
    printf("\nWaiting new data.\n");
    
    // Start subscription processing, it is ends:
    // - when yo press CTRL+C;
    // - when unsubscription indication will be received from the SS;
    // - on error.
    handle_subscription(&info, &subs);
    
    // Before leave we need to unsubscribe our subscription.
    handle_unsubscription(&info, &subs);
    
    // Delete triples list.
    update_subscription_triples(&subs, NULL, NULL);
    
    ss_leave(&info);    

    printf("\nKP leave SS...\n");

    return 0;
}



/******************* Static functions ************************/

/**
 * @brief Function to handle ctrl+c (SIGINT) signal. If you press 
 * CTRL+C twice, then program will be closed immediately.
 *
 * @param[in] signum signal identifier number.
 */
static void signal_callback_handler(int signum)
{
    if (exit_flag == 0) {
        printf("Please waiting, i'm going to shutdown the program...");
        exit_flag = 1;  // Set a flag to end the program.
    } else {
        printf("OK, am exiting...");
        exit(signum);
    }
}

/**
 * @brief Makes a subscription to triples.
 *
 * @param[in] info information about the smart space.
 * @param[in] subs subscription.
 *
 * @return 0 on success or -1 otherwise.
 */
static int make_subscription(ss_info_t *info, subs_t *subs)
{
     ss_triple_t *req_triple = NULL;
     ss_triple_t *res_triple = NULL; 
    
    // Create the triple for a subscription operation.    
    ss_add_triple(&req_triple, SS_RDF_SIB_ANY, SS_RDF_SIB_ANY, SS_RDF_SIB_ANY, 
                SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
        
    if (req_triple == NULL) {
        printf("Not enough memory to create a triple.\n");
        return -1;
    }
    
    if (ss_subscribe(info, &subs->info, req_triple, &res_triple) < 0)
   {
        printf("Failed to subscribe.\n");
        ss_delete_triples(req_triple); 
        return -1;
    } 
    
    ss_delete_triples(req_triple); 
    
    // Update information about triples that were received with the subscription 
    // result. This triples are used as new triples. After  subscribe operation we 
    // have only new triples, because we do not know about what was n the SS, 
    // before it.
    update_subscription_triples(subs, res_triple, NULL);
    subs->is_subscribed = true;
    
    return 0;
 }

/**
 * @brief Checks notifications from the smart space. 
 * It works while exit_flag is 0 or while error/unsubscription indication is not 
 * received.   
 *
 * @param[in] info information about the smart space.
 * @param[in] subs subscription.
 */
 static void handle_subscription(ss_info_t *info, subs_t *subs) 
{  
    if (info == NULL || subs == NULL) {
        return;
    }
    
    // Cheking indication fdfrom the SS. 
    while (exit_flag == 0 && subs->is_subscribed == true) {
                
        int result = handle_indication(info, subs, KP_SUBS_CHECKING_TIME);
        struct timeval tval;
        gettimeofday(&tval, NULL);
            
        switch (result) {
           //  Timeout 
           case 0: continue;     
           break;

           // Subscribe
            case 1:  
                printf("\n\ntime:%li.%li\n", tval.tv_sec, tval.tv_usec);
                printf(" --- Old values: \n");
                print_triples(subs->old_triples);
                printf(" --- New values: \n");
                print_triples(subs->new_triples);
            break;
            
            // Error
            default: printf("Checking fails.\n"); 
                exit_flag = 1;
            break;
        }
    }
}

 /**
  * @brief Unsubscribes the subscription.   
  *
  * @param[in] info information about the smart space.
  * @param[in] subs subscription.
  */
static void handle_unsubscription(ss_info_t *info, subs_t *subs) 
{
    if (info == NULL || subs == NULL) {
        return;
    }
    
    // Send message about the unsubscription to the smart space.
    if (subs->is_subscribed == true) {
        printf("\nUnsubscribe status: %i.\n", ss_unsubscribe(info, &subs->info));
     }
    
    // Waiting unsubscription indication, before unsibscription notification 
    // can be receied indication about changes may be send to the KP,.
    for (int i = 0; i < 5; ++i) {
        handle_indication(info, subs, KP_SUBS_CHECKING_TIME);
        
        if (subs->is_subscribed == false) {
            break;
        }
    }
    
    // Close subscription if the unsubscription indication can not be received.
     if (subs->is_subscribed == true) {
        ss_close_subscription(&subs->info);
     }
}

/**
 * @brief Gets notiification using subscription and made apropriate actions with the
 * subscription.
 * 
 * @param[in] info information about the smart space.
 * @param[in] subs subscription.
 */
static int handle_indication(ss_info_t *info, subs_t *subs, int timeout) 
{  
    if (info == NULL || subs == NULL) {
        return -1;
    }
    
    ss_triple_t *new_triples = NULL;
    ss_triple_t *old_triples = NULL;
        
    int result = ss_subscribe_indication(
                            info, &subs->info, &new_triples, &old_triples,  timeout);

    // Subscribe indication
    if (result == 1)  {
        update_subscription_triples(subs, new_triples, old_triples);
    }
    
   // Unsubscribe
    if (result == 2) {
            printf("Unsubscribe indication is received...\n"); 
            subs->is_subscribed = false;
    }

    return result;
}

/**
 * @brief Removes old triples list from the subscription structure and sets new.
 * 
 * @param[in] subs subscription.
 * @param[in] new_triples list with new triples of the subscription indication.
 * @param[in] old_triples list with old triples of the subscription indication.
 */
static void update_subscription_triples(
                    subs_t *subs, ss_triple_t * new_triples, ss_triple_t *old_triples)
{
    if (subs == NULL) {
        return;
    }
    
    ss_delete_triples(subs->new_triples) ;
    ss_delete_triples(subs->old_triples) ;
    
    subs->new_triples = new_triples;
    subs->old_triples = old_triples;
}

/**
 * @brief Prints triples.
 * 
 * @param[in] triples list of the triples.
 */
static void print_triples(ss_triple_t *triples)
{
    while (triples != NULL) {
        printf("'%s' -> '%s' -> '%s'\n", triples->object, triples->predicate, triples->subject);            
        triples = triples->next;
    }
}




