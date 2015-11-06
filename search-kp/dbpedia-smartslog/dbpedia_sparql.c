#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <locale.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>

#include <ckpi/ckpi.h>
#include <scew/scew.h>
#include <smartslog.h>

#include "ontology/etourism.h"
#define KP_SS_NAME "X"
#define KP_SS_ADDRESS "194.85.173.9" 
#define KP_SS_PORT 10010
 
/**
 * @brief Convert special characters in the sparql query.
 * @param[in] sparql_query SPAQL query to accesspoint.
 */
static char *special_convert_sparql_query(char *sparql_query);

/**
 * @brief Print query results in file.
 * @param[in] result Query result from accesspoint.
 */
static int print_file(char *result);

/**
 * @brief Socket request with SPARQL query to accesspoint.
 * @param[in] sparql_query SPAQL query to accesspoint.
 * @param[in] accesspoint Accesspoint host url
 */
static sslog_sparql_result_t *process_accesspoint_spaql_query(char *sparql_query, char *accesspoint);

/**
* TODO: need implementation
* @brief Create http get request from request string (add GET prefix and HTTP/1.1\r\n\r\n, convert special symbols)
* @param[in] request Accesspoint request
*/
static char *create_accesspoint_get_request(char *request);

/**
* @brief Create scew element from xml
*/
static scew_element *create_scew_element_from_xml(char *xml);

/**
* @brief Print scew element
*/
static void print_element (scew_element *element, unsigned int indent);

/**
* @brief Print scew indent
*/
static void print_indent (unsigned int indent);

/**
* @brief Print scew attributes
*/
static void print_attributes (scew_element *element);

/**
* @brief Conversion ss_sparql_result_t to sslog_sparql_result_t
*/
static sslog_sparql_result_t* sslog_to_sslog_sparql(ss_sparql_result_t *kpi_results, int number_of_bindings);

/**
* @brief Get column variable index of sslog_sparql_result_t
*/
static inline int sslog_sparql_get_column_index(sslog_sparql_result_t *result, const char *variable_name)
{
    int i = 0;
    
    for (i = 0; i < result->bindings_count; ++i) {
        if (strncmp(result->names[i], variable_name, SSLOG_TRIPLE_URI_LEN) == 0) {
            return i;
        }
    }

    return -1;
}

int main(int argc, char** argv)
{	
    sslog_init();    
    sslog_node_t *node = sslog_new_node("KP", KP_SS_NAME, KP_SS_ADDRESS, KP_SS_PORT);
	register_ontology();
    if (sslog_node_join(node) != SSLOG_ERROR_NO) {
        printf("Can't join to SS\n");
    return 0;
    }
    printf("\nKP join to SS\n");

	// cultural information by geo location
/*	char *sparql_query_example = "SELECT * WHERE { ?url geo:lat \"48.858223\"^^xsd:float; geo:long \"2.294500\"^^xsd:float; rdfs:label ?POItitle; rdfs:comment ?CulturalInfo. filter langMatches( lang(?CulturalInfo), \"EN\"). filter langMatches( lang(?POItitle), \"EN\").} LIMIT 10";*/

    // Architect of the Eiffel Tower
/*    char *sparql_query_example = "SELECT * WHERE { ?url dbo:significantBuilding dbr:Eiffel_Tower; foaf:name ?architect. filter langMatches( lang(?architect), \"EN\").}";*/
    
    // Some information about the Disneyland
/*    char *sparql_query_example = "SELECT * WHERE { ?url foaf:isPrimaryTopicOf <http://en.wikipedia.org/wiki/Disneyland>; rdfs:label ?POItitle; rdfs:comment ?CulturalInfo; dbp:openingDate ?creationDate. filter langMatches( lang(?CulturalInfo), \"EN\"). filter langMatches( lang(?POItitle), \"EN\").}";*/
    
    // Attractions of the Disneyland 
    char *sparql_query_example = "SELECT * WHERE { ?url dbo:location dbr:Disneyland; rdfs:label ?POItitle; rdfs:comment ?CulturalInfo. filter langMatches( lang(?POItitle), \"EN\"). filter langMatches( lang(?CulturalInfo), \"EN\").}";
    
    sslog_sparql_result_t *sparql_result = process_accesspoint_spaql_query(sparql_query_example,"http://dbpedia.org/sparql");
    
    int i, j = 0;

    if (sparql_result == NULL) {
        printf("SmartSlog SPARQL result is NULL\n");
    } else {
        for (i = 0; i < sparql_result->rows_count; ++i) {

			char *poi_uri = sslog_generate_uri(CLASS_POI);
			printf("%s\n", poi_uri);
    		sslog_individual_t *poi = sslog_new_individual(CLASS_POI, poi_uri); 
    		if (poi == NULL) {
       			printf("\nError poi: %s\n", sslog_error_get_last_text());
        		return 0;
    		}
            for (j = 0; j < sparql_result->bindings_count; ++j) {
                // Types decription: incorrect type (-1), RDF_TYPE_URI (1),
                // SS_RDF_TYPE_LIT (2), RDF_TYPE_BNODE (3), RDF_TYPE_UNBOUND (4)
		
                printf("%i - name: %s type: %d value: %s\n", i, sparql_result->names[j], 
                        sparql_result->rows[i]->types[j], sparql_result->rows[i]->values[j]);
				if (strcmp(sparql_result->names[j], "url") == 0) {
					sslog_insert_property(poi, PROPERTY_URL, sparql_result->rows[i]->values[j]);
				}
				if (strcmp(sparql_result->names[j], "POItitle") == 0) {
					sslog_insert_property(poi, PROPERTY_POITITLE, sparql_result->rows[i]->values[j]);
				}
				if (strcmp(sparql_result->names[j], "CulturalInfo") == 0) {
				}

            }
			sslog_node_insert_individual(node, poi);
        } // for - rows
    } 

	sslog_node_leave(node);
	printf("\nKP leave SS...\n");
    
	return 0;    
}

static char *special_convert_sparql_query(char *sparql_query){
    int i = 0; int j = 0;

    char *result_query = malloc(strlen(sparql_query)*3*sizeof(char));

    while (i < strlen(sparql_query)) {
		if (sparql_query[i] == ' ') { result_query[j] = '+'; j++;
		} else { if (sparql_query[i] == '?') { 
			result_query[j] = '%'; result_query[j+1] = '3'; result_query[j+2] = 'F';
			j+=3;} else { if (sparql_query[i] == ':') { 
				result_query[j] = '%';	result_query[j+1] = '3'; result_query[j+2] = 'A';
				j+=3;} else {if (sparql_query[i] == '"') { 
					result_query[j] = '%';	result_query[j+1] = '2'; result_query[j+2] = '2';
					j+=3;} else { if (sparql_query[i] == '@'){ 
						result_query[j] = '%';	result_query[j+1] = '4';result_query[j+2] = '0';
						j+=3;} else {result_query[j] = sparql_query[i]; j++;}}}}}
        i++;
    }

	return result_query;
}

static int print_file(char *result)
{
	FILE *fp;    
    fp = fopen("answer.xml", "wb");
	if(fp == NULL) {
        perror("Failure creating file");
        return EXIT_FAILURE;
    }

	fwrite (result, strlen(result), 1, fp);
    fclose(fp);   
 
    return 0;
}
// TODO: Use advanced parameters, e.g. timeout request, type of result sparql query
static sslog_sparql_result_t *process_accesspoint_spaql_query(char *sparql_query, char *accesspoint)
{
    struct addrinfo hints;
    int sockfd, newsock;  
    struct addrinfo *ai;  
    struct sockaddr_in *sin;
    const char *addr;
    char abuf[INET_ADDRSTRLEN]; 
    
    int number_of_bindings;
    ss_sparql_result_t *ckpi_sparql_result;
    sslog_sparql_result_t *smartslog_sparql_result;
    
    char accesspoint_request[1000] = "GET http://dbpedia.org/sparql?default-graph-uri=&query=";
    char *accesspoint_extra_parameters = "&format=application%2Fxml&timeout=30000&debug=onHTTP/1.1\r\n\r\n";
//char *accesspoint_extra_parameters = "&format=application%2Frdf%2Bxml&timeout=30000&debug=onHTTP/1.1\r\n\r\n";
    size_t nbytes;

    int result;

    char buffer[256];
    char accesspoint_result[65536];

    char *accesspoint_sparql_query;

    accesspoint_sparql_query = special_convert_sparql_query(sparql_query);

    strcat(accesspoint_request, accesspoint_sparql_query);
    strcat(accesspoint_request, accesspoint_extra_parameters);


    nbytes = strlen(accesspoint_request); 

    (void) setlocale(LC_ALL, "");
    /* Создаём сокет */
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if(sockfd == -1)
        exit(EXIT_FAILURE);

    memset(&hints, 0, sizeof(hints));
    hints.ai_flags = 0;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = 0;
    hints.ai_addrlen = 0;
    hints.ai_addr = NULL;
    hints.ai_canonname = NULL;
    hints.ai_next = NULL;

    if (0 != (result = getaddrinfo("dbpedia.org", "80", &hints, &ai))) {
        fprintf(stderr, "getaddrinfo() error: %s\n", gai_strerror(result));
        exit(EXIT_FAILURE);
    }

    sin = (struct sockaddr_in *) (ai->ai_addr);
    if (NULL == (addr = inet_ntop(AF_INET, &sin->sin_addr,
                                  abuf, INET_ADDRSTRLEN))) {
        perror("inet_ntop");
        exit(EXIT_FAILURE);
    }

    newsock = sockfd;

    if (connect(sockfd, ai->ai_addr, ai->ai_addrlen) == -1) {
        perror("Result");
        exit(EXIT_FAILURE);
    }

	//printf("%s\n" , accesspoint_request);

    send(newsock, accesspoint_request, nbytes, 0);
    
    int n = 0;
    
    while ((n = read(newsock, buffer, sizeof(buffer))) > 0 ) {
        buffer[n] = '\0';
        strcat(accesspoint_result, buffer);
    }

    print_file(accesspoint_result);
    
    // XML string to scew_element
    scew_element *scew_sparql_result = create_scew_element_from_xml(accesspoint_result);
    
    if (scew_sparql_result == NULL) {
        printf("Unable to create scew element from xml");
        return NULL;
    }
    
    // scew_element to ss_sparql_result_t
    ckpi_sparql_result = parse_sparql_xml_select(scew_sparql_result, &number_of_bindings);
    // ss_sparql_result_t to sslog_sparql_result_t
    smartslog_sparql_result = sslog_to_sslog_sparql(ckpi_sparql_result, number_of_bindings);
    
    close(sockfd);
    
    return smartslog_sparql_result;
   
}

static scew_element *create_scew_element_from_xml(char *xml)
{
    scew_reader *reader = NULL;
    scew_parser *parser = NULL;
    scew_tree *tree = NULL;
    scew_element *root = NULL;
    scew_writer *writer = NULL;
    scew_printer *printer = NULL;
    
    /* Creates an SCEW parser. This is the first function to call. */
    parser = scew_parser_create ();

    scew_parser_ignore_whitespaces (parser, SCEW_TRUE);
    
    reader = scew_reader_buffer_create (xml, scew_strlen (xml));
    
    if (reader == NULL) {
        scew_error code = scew_error_code ();
        printf("Unable to load xml (error #%d: %s)\n", code, scew_error_string (code));
    }
    
    tree = scew_parser_load (parser, reader);
    
    if (tree == NULL) {
        scew_error code = scew_error_code ();
        scew_printf (_XT("Unable to parse file (error #%d: %s)\n"),
                   code, scew_error_string (code));

        /* Frees the SCEW parser and reader. */
        scew_reader_free (reader);
        scew_parser_free (parser);

        return NULL;
    }
    
    root = scew_tree_root (tree);
    
    //print_element (root, 0);    
/*    writer = scew_writer_fp_create (stdout);*/
/*    printer = scew_printer_create (writer);*/
/*    scew_printer_print_tree (printer, tree);*/
/*    scew_printf (_XT("\n"));*/
    
    return root;
}

static void print_element(scew_element *element, unsigned int indent)
{
    XML_Char const *contents = NULL;
    scew_list *list = NULL;

    if (element == NULL) {
        return;
    }

    /* Prints the starting element tag with its attributes. */
    print_indent (indent);
    scew_printf (_XT("<%s"), scew_element_name (element));
    print_attributes (element);
    scew_printf (_XT(">"));

    contents = scew_element_contents (element);

    if (contents == NULL) {
        scew_printf (_XT("\n"));
    }

    /**
    * Call print_element function again for each child of the current
    * element.
    */
    list = scew_element_children (element);
    while (list != NULL) {
        scew_element *child = scew_list_data (list);
        print_element (child, indent + 1);
        list = scew_list_next (list);
    }

    /* Prints element's content. */
    if (contents != NULL) {
        scew_printf (_XT("%s"), contents);
    } else {
        print_indent (indent);
    }

    /* Prints the closing element tag. */
    scew_printf (_XT("</%s>\n"), scew_element_name (element));
}

static void print_indent (unsigned int indent)
{
    /* Indentation size (in whitespaces). */
    static unsigned int const INDENT_SIZE = 4;

    if (indent > 0) {
        scew_printf (_XT("%*s"), indent * INDENT_SIZE, " ");
    }
}

static void print_attributes (scew_element *element)
{
    if (element != NULL) {
        /**
        * Iterates through the element's attribute list, printing the
        * pair name-value.
        */
        scew_list *list = scew_element_attributes (element);
        while (list != NULL) {
            scew_attribute *attribute = scew_list_data (list);
            scew_printf (_XT(" %s=\"%s\""),
                       scew_attribute_name (attribute),
                       scew_attribute_value (attribute));
            list = scew_list_next (list);
        }
    }
}

static sslog_sparql_result_t* sslog_to_sslog_sparql(ss_sparql_result_t *kpi_results, int number_of_bindings)
{
    int rows_count = 0;
    int i = 0;

    ss_sparql_result_t *result_walker = kpi_results;

    while (result_walker != NULL) {
        ++rows_count;
        result_walker = result_walker->next;
    }

    sslog_sparql_result_t *result = sslog_new_sparql_result((const char **) kpi_results->name, number_of_bindings, rows_count);

    if (kpi_results == NULL) {
        return result;
    }

    int row_index =0;
    while (kpi_results != NULL) {

        sslog_sparql_result_row_t *row = sslog_new_sparql_result_row(number_of_bindings);

        for (i = 0; i < number_of_bindings; ++i) {

            int index = sslog_sparql_get_column_index(result, kpi_results->name[i]);

            if (index >= 0) {
                row->types[i] = kpi_results->type[i];

                // Do not copy string, just set kpi pointer to NULL.
                if (row->types[i] == SSLOG_RDF_TYPE_UNBOUND) {
                    row->values[i] = NULL;//sslog_strndup("", 1);
                } else {
                    row->values[i] = kpi_results->value[i];
                    kpi_results->value[i] = NULL;
                }

            }

            //result->name[i] = kpi_results->name[i];
            //kpi_results->name[i] = NULL;
        }

        result->rows[row_index] = row;
        ++row_index;
        //list_add_data(&result->rows, row);

        kpi_results = kpi_results->next;
    }

    return result;
}


