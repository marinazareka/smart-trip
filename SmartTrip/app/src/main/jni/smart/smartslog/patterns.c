/**
 * @file patterns.c
 * @Author Alexandr A. Lomov <lomov@cs.karelia.ru>
 * @date   05 December, 2009
 * @brief  Contains functions for work with classes.
 *
 *
 * @section LICENSE
 *
 * SmartSlog KP Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * SmartSlog KP Library is distributed in the hope that it will be useful,
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
 * Contains functions for work with classes.
 * This file is part of SmartSlog KP Library.
 *
 * Copyright (C) 2009 - Alexandr A. Lomov. All rights reserved.
 *
 */


#ifndef _PATTERNS_C
#define	_PATTERNS_C

#include "patterns.h"
#include "properties.h"
#include "kpi_interface.h"
#include "ss_properties.h"

static void filter_list_for_property(list_t* inds, pattern_condition_t *cond);

/**
 * @brief Create and prepare new pattern.
 *
 * @param ont_class class of a pattern.
 * @param uuid ientifier fro pattern.
 *
 * @return new created pattern.
 */
SSLOG_EXTERN pattern_t* sslog_new_pattern(class_t *ont_class, const char *uuid)
{
        pattern_t *pattern = (pattern_t *) malloc(sizeof(pattern_t));

        pattern->rtti = RTTI_PATTERN;
	pattern->uuid = NULL;

        pattern->check_prop_list = NULL;
        pattern->uncheck_prop_list = NULL;

        sslog_set_pattern_class(pattern, ont_class);
        sslog_set_pattern_uuid(pattern, uuid);

        return pattern;
}


SSLOG_EXTERN pattern_condition_t* new_pattern_condition(property_t *prop, void *value, int condition)
{
    if (verify_property(prop) != ERROR_NO) {
        return NULL;
    }

    pattern_condition_t *cond = (pattern_condition_t *) malloc(sizeof(pattern_condition_t));

   cond->condition = RTTI_PATTERN_CONDITION;
   cond->prop = prop;
   cond->value = NULL;

    if (sslog_is_object_property(cond->prop) == true) {
        cond->value = value;
    } else if (value != NULL) {
        cond->value = strdup((const char *) value);
    }

    cond->condition = condition;

    return cond;
}


void free_pattern_condition(pattern_condition_t *cond)
{
    if (cond == NULL) {
        return;
    }

    if (sslog_is_object_property(cond->prop) == true) {
        cond->value = NULL;
    } else if (cond->value != NULL) {
        free(cond->value);
    }

    cond->rtti = RTTI_MIN_VALUE;

    free(cond);
}


SSLOG_EXTERN void sslog_clean_pattern(pattern_t *pattern)
{
    if (pattern == NULL) {
        return;
    }

    list_free_with_nodes(pattern->check_prop_list, (void (*) (void*)) free_pattern_condition);
    list_free_with_nodes(pattern->uncheck_prop_list, (void (*) (void*)) free_pattern_condition);

    pattern->uncheck_prop_list = NULL;
    pattern->check_prop_list = NULL;

    free(pattern->uuid);
    pattern->uuid = NULL;
    pattern->ont_class = NULL;


}


SSLOG_EXTERN void sslog_free_pattern(pattern_t *pattern)
{
    if (pattern == NULL) {
        return;
    }

    list_free_with_nodes(pattern->check_prop_list, (void (*) (void*)) free_pattern_condition);
    list_free_with_nodes(pattern->uncheck_prop_list, (void (*) (void*)) free_pattern_condition);

	if (pattern->uuid != NULL) {
		free(pattern->uuid);
	}

    pattern->rtti = RTTI_MIN_VALUE;

    free(pattern);
}


SSLOG_EXTERN int sslog_set_pattern_class(pattern_t *pattern, class_t *ont_class)
{
    int error_code = verify_pattern(pattern);

    if (error_code != ERROR_NO) {
        return error_code;
    }

    if (ont_class == NULL) {
        pattern->ont_class = NULL;
        return ERROR_NO;
    }

    error_code = verify_class(ont_class);

    if (error_code != ERROR_NO) {
        return error_code;
    }

    pattern->ont_class = ont_class;

    return ERROR_NO;
}


SSLOG_EXTERN int sslog_set_pattern_uuid(pattern_t *pattern, const char *uuid)
{
    if (verify_pattern(pattern) != ERROR_NO) {
        return ERROR_INCORRECT_PATTERN;
    }

    if (uuid == NULL) {
        if (pattern->uuid != NULL) free(pattern->uuid);
        pattern->uuid = NULL;
    } else if (strlen(uuid) == 0) {
        return ERROR_INCORRECT_UUID;
    } else {
        free(pattern->uuid);
        pattern->uuid = strdup(uuid);
    }

    return ERROR_NO;
}


SSLOG_EXTERN int sslog_add_unproperty_to_pattern(pattern_t *pattern, property_t *prop, void *value, int condition)
{
    if (verify_pattern(pattern) != ERROR_NO) {
        return ERROR_INCORRECT_PATTERN;
    }

    pattern_condition_t* cond = new_pattern_condition(prop, value, condition);

    pattern->uncheck_prop_list = list_get_new_list_if_null(pattern->uncheck_prop_list);

    list_add_data(cond, pattern->uncheck_prop_list);

    return EXIT_SUCCESS;
}


SSLOG_EXTERN int sslog_add_property_to_pattern(pattern_t *pattern, property_t *prop, void *value, int condition)
{
    if (verify_pattern(pattern) != ERROR_NO) {
        return ERROR_INCORRECT_PATTERN;
    }

    pattern_condition_t* cond = new_pattern_condition(prop, value, condition);

    pattern->check_prop_list = list_get_new_list_if_null(pattern->check_prop_list);

    list_add_data(cond, pattern->check_prop_list);

    return EXIT_SUCCESS;
}

/**
 * \fn void *print_triples (ss_triple_t *triples)
 *
 * \brief Prints triples to sdtout.
 *
 * Prints each triple in list in stdout.
 *
 * \param[in] ss_triple_t *triples. Pointer to the list of triples.
 */
void print_triples(ss_triple_t * triples)
{
	char *subject;
	char *predicate;
	char *object;
	
	while (triples)
	{
		subject = (char *) triples->subject;
		predicate = (char *) triples->predicate;
		object = (char *) triples->object;
		fprintf(stderr, "%s --- %s --- %s\n", subject, predicate, object);
		
		triples = triples->next;
	}
}

/**
 * \fn void *print_individual(individual_t * ind)
 *
 * \brief Prints individual to sdtout.
 *
 * \param[in] individual_t *ind. Pointer to the individual to print.
 */
void print_individual(individual_t * ind)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	prop_val_t *prop = NULL;
	property_t *proptype = NULL;
	individual_t *object = NULL;
	
	
	fprintf(stderr, "\n\n --- UUID: %s\n", ind->uuid);
	
	if (ind->properties && !list_empty(&ind->properties->links)) {
		list_for_each(pos, &ind->properties->links) {
			node = list_entry(pos, list_t, links);
			prop = (prop_val_t *)node->data;
			proptype = prop->property;
			
			if (proptype == NULL) {
				set_error(ERROR_INCORRECT_PROPLIST);
				return;
			}
			
			if (proptype->type == OBJECTPROPERTY) {
				object = (individual_t *) prop->prop_value;
				fprintf(stderr, "\t%s (OBJECTPROPERTY):\t%s\n", proptype->name, object->uuid);
			}
			else if (proptype->type == DATATYPEPROPERTY) {
				fprintf(stderr, "\t%s (DATATYPEPROPERTY):\t%s\n", proptype->name, (char *)prop->prop_value);
			}
		}
	}
	fprintf(stderr, "\n\n");
}


/**
* \fn list_t* sslog_ss_get_individual_by_pattern_all(pattern_t *individual_pattern)
*
* \brief Finds all individuals matches given pattern in SS
*
* Tries to ﬁnd individual by pattern.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual_pattern. Pointer to the individual struct (pattern).
* \return list_t*. Null if there is no individuals of such class in SS,
*                 list of founded individuals otherwise.
*/
SSLOG_EXTERN list_t* sslog_ss_get_individual_by_pattern_all(pattern_t *individual_pattern)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	ss_triple_t * first_triple = NULL;
	pattern_condition_t *cond = NULL;
	
	/* Transform pattern to triples */
	triples = pattern_to_triples(individual_pattern);

	/* If an error accures the error message would be already set */
	if (triples == NULL)
	{
		return NULL;
	}

	/* Find individuals by pattern without checking OBJECT properties */
	if(ss_query(sslog_get_ss_info(), triples, &first_triple) < 0) {
		set_error(ERROR_UNKNOWN);
		ss_delete_triples(triples);
		return NULL;
	}
	
	/* Clean request triples */
	ss_delete_triples(triples);
	if (first_triple == NULL) {
		return NULL;
	}
	
	/* Transform triples to individuals.
	   If an error accures the error message would be already set */
	list_t *inds = triples_to_individuals (first_triple);
	if (inds == NULL) {
		return NULL;
	}

	ss_delete_triples(first_triple);

	/* If np individual founede by class or by class and UUID return NULL */
	if ((inds==NULL) || list_empty(&inds->links)) {
		return NULL;
	}

	/* Check OBJECT properties recursively */
	pos = NULL;
	if (individual_pattern->check_prop_list && !list_empty(&individual_pattern->check_prop_list->links)) {
		list_for_each (pos, &individual_pattern->check_prop_list->links) {
			node = list_entry(pos, list_t, links);
			cond = (pattern_condition_t *) node->data;

			/* Check all properties and delete from list where properties are not set or wrong */
			filter_list_for_property(inds, cond);
		}
	}

	return inds;
}

static void filter_list_for_property(list_t* inds, pattern_condition_t *cond)
{
	bool founded = false;
	bool clean = false;
	list_t *node3 = NULL;
	list_head_t *pos2 = NULL;
	list_head_t *pos3 = NULL;
	list_head_t *tmp = NULL;
	pattern_t *object = NULL;
	individual_t *ind = NULL;
	list_t *inds2 = NULL;
	
	if ((inds == NULL) || (cond == NULL) || (cond->prop == NULL) || list_empty(&inds->links)) {
		return;
	}

	property_t *proptype = cond->prop;

	list_for_each_safe (pos2, tmp, &inds->links) {
		if (clean) {
			list_del(pos2);
			continue;
		}

		list_t *node = list_entry(pos2, list_t, links);
		if (node->data == NULL) {
			continue;
		}
		
		ind = (individual_t *)node->data;
		
		if (cond->value == NULL) {
			const prop_val_t *prop = sslog_get_property(ind, proptype->name);
			
			if (prop == NULL) {
				list_del(pos2);
			}
			continue;
		}
		
		if (proptype->type == DATATYPEPROPERTY) {
			char *prop_value = (char *) cond->value;
			
			if (!sslog_ss_exists_property_value(ind, proptype, (void *) prop_value)) {
				list_del(pos2);
			}
		}
		else if (proptype->type == OBJECTPROPERTY) {
			int rtti = sslog_get_rtti_type(cond->value);
			
			if (rtti == RTTI_INDIVIDUAL) {
				individual_t *ind_value = (individual_t *) cond->value;
				
				if (!sslog_ss_exists_property_value(ind, proptype, (void *) ind_value)) {
					list_del(pos2);
				}
			}
			else if (rtti == RTTI_PATTERN) {
				object = (pattern_t *) cond->value;
				
				if (inds2 == NULL)
					inds2 = sslog_ss_get_individual_by_pattern_all(object);
				
				if ((inds2 == NULL) || list_empty(&inds2->links)) {
					/* Clean list */
					list_del(pos2);
					clean = true;
					continue;
				}
				
				founded = false;
				list_for_each(pos3, &inds2->links) {
					node3 = list_entry(pos3, list_t, links);
					
					if (sslog_ss_exists_property_value(ind, proptype, (void *) node3->data)) {
						founded = true;
						break;
					}
				}
				if (!founded) {
					list_del(pos2);
				}
			}
		}
	}	
}

#endif	/* _PATTERNS_C */
