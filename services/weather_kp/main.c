
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <libxml/parser.h>
#include <curl/curl.h>
#include <smartslog.h>

#include "ontology.h"
#include "common.h"

// начальное значение точки отсчета ожидания
#define MAX_BUF	65536
#define CONF "file.xml"

// флаг прерывания
static volatile bool cont = true;

// узел доступа к ИП
sslog_node_t* node;

char* cond;
char url[150];
char wr_buf[MAX_BUF+1];
int wr_index;

size_t write_data( void *buffer, size_t size, size_t nmemb, void *userp ) {
    int segsize = size * nmemb;

 /* Check to see if this data exceeds the size of our buffer. If so,
 * set the user-defined context value and return 0 to indicate a
 * problem to curl.
 */
    if ( wr_index + segsize > MAX_BUF ) {
        *(int *)userp = 1;
        return 0;
    }

 /* Copy the data from the curl buffer into our buffer */
    memcpy( (void *)&wr_buf[wr_index], buffer, (size_t)segsize );

 /* Update the write index */
    wr_index += segsize;

 /* Null terminate the buffer */
    wr_buf[wr_index] = 0;

 /* Return the number of bytes received, indicating to curl that all is okay */
    return segsize;
}

// определяем сезон по дате
char* month(char* date) {
    char* month = strtok(date, "-");
    int number = atoi(strtok(NULL, "-"));
    if (number > 2 && number < 6)
        month = "spring";
        else if (number > 5 && number < 9)
            month = "summer";
            else if (number > 8 && number < 12)
                month = "autumn";
            else month = "winter";
    return month;
}

static bool subscription_result(sslog_individual_t* point_individual) {
    // получаем идентификатор точки
    fprintf(stdout, "New point: %s!\n", sslog_entity_get_uri(point_individual));
    sslog_node_populate(node, point_individual);
    // получаем координаты
    sslog_individual_t* loc = sslog_get_property(point_individual, PROPERTY_HASLOCATION);
    sslog_node_populate(node, loc);
    char* lat = sslog_get_property(loc, PROPERTY_LAT);
    char* lon = sslog_get_property(loc, PROPERTY_LONG);
    fprintf(stdout, "LAT is %s\n", lat);
    fprintf(stdout, "LONG is %s\n", lon);
    fprintf(stdout, "Finish parsing result\n");

    CURLcode ret;
    char buffer[300];
    CURL* curl;
    int wr_error;

    wr_error = 0;
    wr_index = 0;

    // получаем погоду
    curl= curl_easy_init();
    if (!curl) {
        printf("Coudln't init to curl\n");
        return 0;
    }

    strcat(url, "http://api.openweathermap.org/data/2.5/forecast/daily?lat=");
    strcat(url, lat);
    strcat(url, "&lon=");
    strcat(url, lon);
    strcat(url, "&APPID=a92ef9089a456acd8e9c0a76e697d223&units=metric&mode=xml&cnt=4");
    printf("Url is %s\n", url);

    curl_easy_setopt(curl, CURLOPT_URL, url);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&wr_error);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);

    ret = curl_easy_perform(curl);
    printf( "ret = %d (write_error = %d)\n", ret, wr_error );

    /* Emit the page if curl indicates that no errors occurred */
    if (ret == 0)
        printf( "%s\n", wr_buf );

    curl_easy_cleanup( curl );

    // Разбор XML строки с помощью библиотеки libxml2
    xmlNodePtr cur;
    xmlNodePtr cur2;
    xmlNodePtr cur3;
    xmlDocPtr doc;

    char weather[200];
    strcpy(weather, "");

    doc = xmlParseMemory(wr_buf, strlen(wr_buf));
    if(doc == NULL) {
        fprintf(stderr,"Error parsing \"file.xml\"\n");
        return -1;
    }
    cur = xmlDocGetRootElement(doc);
    if(xmlStrcmp(cur->name,(const xmlChar *)"weatherdata")) {
        fprintf(stderr,"Error getting weather\n");
        return -1;
    }

    cur = cur->xmlChildrenNode;
    while(cur != NULL) {
        if((!xmlStrcmp(cur->name,(const xmlChar *)"forecast"))) {
            cur2 = cur->xmlChildrenNode;
            while(cur2 != NULL) {
                if((!xmlStrcmp(cur2->name,(const xmlChar *)"time"))) {
                    char* day = xmlGetProp(cur2,"day");
                    strcat(weather, day);
                    strcat(weather, ",");
                    strcat(weather, month(day));
                    strcat(weather, ",");
                    cur3 = cur2->xmlChildrenNode;
                    while(cur3 != NULL) {
                        if((!xmlStrcmp(cur3->name,(const xmlChar *)"precipitation"))) {
                            cond = xmlGetProp(cur3,"type");
                            if (cond != NULL)
                                strcat(weather, cond);
                                else strcat(weather, "no precipitation");
                            strcat(weather, ",");
                        }
                        if((!xmlStrcmp(cur3->name,(const xmlChar *)"temperature"))) {
                            strcat(weather, xmlGetProp(cur3,"day"));
                            strcat(weather, ",");
                        }
                        cur3 = cur3 -> next;
                    }
                }
                cur2 = cur2 -> next;
            }
        }
        cur = cur->next;
    }
    printf("weather is %s\n", weather);

    // разбираем строку на токены и в цикле записываем полученные значения как свойства климата

    char* token = strtok(weather, ",");
    char* date = token;

    sslog_individual_t* climate[4];
    int i;
    for(i = 0; i < 4; i++) {
        climate[i] = sslog_new_individual(CLASS_CLIMATE, rand_uuid("climate_point_"));
        sslog_insert_property(climate[i], PROPERTY_EXACTTIME, token);
        sslog_insert_property(climate[i], PROPERTY_DOWNLOADTIME, date);
        token = strtok(NULL, ",");
        sslog_insert_property(climate[i], PROPERTY_WEATHERSEASON, token);
        token = strtok(NULL, ",");
        sslog_insert_property(climate[i], PROPERTY_METEOCONDITIONS, token);
        token = strtok(NULL, ",");
        sslog_insert_property(climate[i], PROPERTY_TEMPERATURE, token);
        sslog_node_insert_individual(node, climate[i]);
        sslog_node_insert_property(node, point_individual, PROPERTY_HASCLIMATE, climate[i]);
        token = strtok(NULL, ",");
    }
	sslog_node_insert_property(node, point_individual, PROPERTY_UPDATED, "1");

    cont = false;
    return true;
}

void search_handler(sslog_subscription_t* sub) {
    fprintf(stdout, "Process search subscription\n");

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
        sslog_individual_t* individual = sslog_get_individual(triple->subject);

        if (individual == NULL) {
            fprintf(stderr, "No individual for triple %s\n", triple->subject);
            continue;
        }
        subscription_result(individual);
        break;
    }
    list_free_with_nodes(triples, NULL);
}

int main(void) {
    init_rand();
    sslog_init();
    register_ontology();
    node = create_node("weather_kp", "config.ini");
	if (sslog_node_join(node) != SSLOG_ERROR_NO) {
		fprintf(stderr, "Can't join node\n");
		return 1;
	}

    // подписываемся на добавление нового объекта в классе "Точка"
    sslog_subscription_t* subscription = sslog_new_subscription(node, true);
    sslog_sbcr_add_class(subscription, CLASS_POINT);
    sslog_sbcr_set_changed_handler(subscription, search_handler);

    fprintf(stdout, "Subscribing search response\n");
    if (sslog_sbcr_subscribe(subscription) != SSLOG_ERROR_NO) {
        sslog_free_subscription(subscription);
        fprintf(stderr, "Can't subscribe request\n");
        return 2;
    }
    fprintf(stdout, "Process requests...\n");
    // основной цикл работы
    for (; cont == true;) {
        usleep(10);
    }

    // отключение подписок
    fprintf(stdout, "Unsubscribing response\n");
    sslog_sbcr_unsubscribe(subscription);
    sslog_free_subscription(subscription);

    sslog_node_leave(node);
    sslog_shutdown();
}

