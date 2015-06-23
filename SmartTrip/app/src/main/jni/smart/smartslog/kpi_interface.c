/**
* @file kpi_interface.c
* @Author Vanag Pavel <vanag@cs.karelia.ru>
* @date   20 April, 2009
* @brief  Contains functions for providing interface to kpi_low.
* @version 0.2alpha
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
* @section DESCRIPTION
*
* Contains functions for providing interface to kpi_low.
* This file is part of SmartSlog KP Library.
*
*/


#ifndef _KPI_INTERFACE_C_
#define	_KPI_INTERFACE_C_

#include "kpi_interface.h"
#include "properties.h"
#include "utils/util_func.h"

/** @cond INTERNAL_FUNCTIONS */


/**
* \fn ss_triple_t *individual_to_triples (individual_t *ind)
*
* \brief Converts individual to triples.
*
* Converts individual properties with values and uuid with classtype to triples.
* Subject is UUID of individual for all triples
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual. Pointer to the individual struct.
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *individual_to_triples (individual_t *ind)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	prop_val_t *prop = NULL;
	property_t *proptype = NULL;
	individual_t *object = NULL;

	reset_error();

	/* Set RDF type for given individual (type is a class of object) */
	ss_add_triple(&triples, ind->uuid, RDF_TYPE, ind->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

	/* Set all properties */
	if (ind->properties && !list_empty(&ind->properties->links)) {
		list_for_each(pos, &ind->properties->links) {
			node = list_entry(pos, list_t, links);
			prop = (prop_val_t *)node->data;
			proptype = prop->property;

			if (proptype == NULL) {
				set_error(ERROR_INCORRECT_PROPLIST);
				return NULL;
			}

			if (proptype->type == OBJECTPROPERTY) {
				if (sslog_get_rtti_type(prop->prop_value) != RTTI_INDIVIDUAL) {
					set_error(ERROR_INCORRECT_PROPERTY_VALUE);
					return NULL;
				}
				object = (individual_t *) prop->prop_value;
				/* Set object value RDF TYPE for future data integrity */
				ss_add_triple(&triples, ind->uuid, proptype->name, object->uuid, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				ss_add_triple(&triples, object->uuid, RDF_TYPE, object->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
			else if (proptype->type == DATATYPEPROPERTY)
			{
				ss_add_triple(&triples, ind->uuid, proptype->name, (char *) prop->prop_value, SS_RDF_TYPE_URI, SS_RDF_TYPE_LIT);
			}
		}
	}
	else set_error(ERROR_CANT_FIND_PROPERTY);

	return triples;
}

/**
* \fn ss_triple_t *individual_to_triples_any (individual_t *ind)
*
* \brief Converts individual to triples.
*
* Converts individual properties without values and uuid with classtype to triples.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual. Pointer to the individual struct.
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *individual_to_triples_any (individual_t *ind)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	property_t *proptype = NULL;

	reset_error();

	/* Set RDF type for given individual (type is a class of object) */
	ss_add_triple(&triples, ind->uuid, RDF_TYPE, ind->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

	/* Set all properties */
	if (ind->parent_class->properties && !list_empty(&ind->parent_class->properties->links)) {
		list_for_each(pos, &ind->parent_class->properties->links) {
			node = list_entry(pos, list_t, links);
			proptype = (property_t *)node->data;

			if (proptype == NULL) {
				set_error(ERROR_INCORRECT_PROPLIST);
				return NULL;
			}

			if (proptype->type == OBJECTPROPERTY) {
				ss_add_triple(&triples, ind->uuid, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
			else if (proptype->type == DATATYPEPROPERTY)
			{
				ss_add_triple(&triples, ind->uuid, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
		}
	}
	else set_error(ERROR_CANT_FIND_PROPERTY);

	return triples;
}

/**
* \fn ss_triple_t *individual_to_triples_by_pattern (individual_t *ind, pattern_t *pattern)
*
* \brief Converts individual to triples.
*
* Converts individual properties by pattern with values and uuid with classtype to triples.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual. Pointer to the individual struct.
* \param[in] void *pattern. Entity of pettern (class or individual).
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *individual_to_triples_by_pattern (individual_t *ind, pattern_t *pattern)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	prop_val_t *prop = NULL;
	property_t *proptype = NULL;
	list_t *prop_pattern = NULL;
	individual_t *object = NULL;
	int error_code;

	reset_error();

	if (sslog_get_rtti_type(pattern) == RTTI_PATTERN) {
		error_code = verify_class(pattern -> ont_class);
		prop_pattern = pattern->check_prop_list;
	}
	else {
		set_error(ERROR_INCORRECT_ENTITY);
		return NULL;
	}

	/* Set RDF type for given individual (type is a class of object) */
	ss_add_triple(&triples, ind->uuid, RDF_TYPE, ind->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

	/* Set all properties */
	if (!list_empty(&ind->properties->links)) {
		list_for_each(pos, &ind->properties->links) {
			node = list_entry(pos, list_t, links);
			prop = (prop_val_t *)node->data;
			proptype = prop->property;

			if (is_in_property_list(proptype, prop_pattern) || is_in_property_values_list(prop, prop_pattern))
			{
				if (proptype == NULL) {
					set_error(ERROR_INCORRECT_PROPLIST);
					return NULL;
				}

				if (proptype->type == OBJECTPROPERTY) {
					if (sslog_get_rtti_type(prop->prop_value) != RTTI_INDIVIDUAL) {
						set_error(ERROR_INCORRECT_PROPERTY_VALUE);
						return NULL;
					}
					object = (individual_t *) prop->prop_value;
					/* Set object value RDF TYPE for future data integrity */
					ss_add_triple(&triples, ind->uuid, proptype->name, object->uuid, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
					ss_add_triple(&triples, object->uuid, RDF_TYPE, object->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				}
				else if (proptype->type == DATATYPEPROPERTY)
				{
					ss_add_triple(&triples, ind->uuid, proptype->name, (char *) prop->prop_value, SS_RDF_TYPE_URI, SS_RDF_TYPE_LIT);
				}
			}
		}
	}
	else set_error(ERROR_CANT_FIND_PROPERTY);

	return triples;
}


/**
* \fn ss_triple_t *individual_to_triples_by_pattern_any (individual_t *ind, pattern_t *pattern)
*
* \brief Converts individual to triples.
*
* Converts individual properties by pattern without values and uuid with classtype to triples.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual. Pointer to the individual struct.
* \param[in] void *pattern. Entity of pettern (class or individual).
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *individual_to_triples_by_pattern_any (individual_t *ind, pattern_t *pattern)
{
	int error_code;
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	prop_val_t *prop = NULL;
	property_t *proptype = NULL;
	list_t *prop_pattern = NULL;

	reset_error();

	if (sslog_get_rtti_type(pattern) == RTTI_PATTERN) {
		error_code = verify_class(pattern -> ont_class);
		prop_pattern = pattern->check_prop_list;
	}
	else {
		set_error(ERROR_INCORRECT_ENTITY);
		return NULL;
	}

	/* Set RDF type for given individual (type is a class of object) */
	ss_add_triple(&triples, ind->uuid, RDF_TYPE, ind->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

	/* Set all properties */
	if (!list_empty(&ind->properties->links)) {
		list_for_each(pos, &ind->properties->links) {
			node = list_entry(pos, list_t, links);
			proptype = (property_t *)node->data;

			if (is_in_property_list(proptype, prop_pattern) || is_in_property_values_list(prop, prop_pattern))
			{
				if (proptype == NULL) {
					set_error(ERROR_INCORRECT_PROPLIST);
					return NULL;
				}

				if (proptype->type == OBJECTPROPERTY) {
					ss_add_triple(&triples, ind->uuid, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				}
				else if (proptype->type == DATATYPEPROPERTY)
				{
					ss_add_triple(&triples, ind->uuid, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				}
			}
		}
	}
	else set_error(ERROR_CANT_FIND_PROPERTY);

	return triples;
}

/**
* \fn ss_triple_t *class_to_triples (class_t *ont_class)
*
* \brief Converts individual to triples.
*
* Converts class properties with values with classtype to triples.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] class_t *ont_class. Pointer to the class struct.
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *class_to_triples (class_t *ont_class)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	property_t *proptype;

	reset_error();

	/* Set all properties of class	 */
	if (!list_empty(&ont_class->properties->links)) {
		list_for_each(pos, &ont_class->properties->links) {
			node = list_entry(pos, list_t, links);
			proptype = (property_t *) node->data;

			if (proptype == NULL) {
				set_error(ERROR_INCORRECT_PROPLIST);
				return NULL;
			}

			if (proptype->type == OBJECTPROPERTY) {
				ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
			else if (proptype->type == DATATYPEPROPERTY)
			{
				ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
		}
	}
	else set_error(ERROR_CANT_FIND_PROPERTY);

	return triples;
}

/**
* \fn ss_triple_t *pattern_to_triples (pattern_t *pattern)
*
* \brief Converts pattern to triples.
*
* Converts pattern structures to triples.
* If set UUID then Subject is UUID, SS_RDF_SIB_ANY otherwise.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] individual_t * individual. Pointer to the individual struct.
* \param[in] void *pattern. Entity of pettern (class or individual).
* \return ss_triple_t *. List of triples or NULL if error accures.
*/
ss_triple_t *pattern_to_triples (pattern_t *pattern)
{
	list_t *node = NULL;
	list_head_t *pos = NULL;
	ss_triple_t * triples = NULL;
	property_t *proptype = NULL;
	list_t *prop_pattern = NULL;
	pattern_t *object = NULL;
	individual_t *object_ind = NULL;
	pattern_condition_t *cond = NULL;
	int error_code;

	reset_error();

	/* Check if valid given pattern */
	if (sslog_get_rtti_type(pattern) == RTTI_PATTERN) {
		error_code = verify_class(pattern -> ont_class);
		prop_pattern = pattern->check_prop_list;
	}
	else {
		set_error(ERROR_INCORRECT_ENTITY);
		return NULL;
	}

	if (error_code != ERROR_NO) {
		set_error(ERROR_INCORRECT_ENTITY);
		return NULL;
	}

	/* Check if valid class given for pattern */
	error_code = verify_class(pattern -> ont_class);

	/* Set RDF type for given individual (type is a class of object) */
	if ((pattern -> ont_class != NULL) && (error_code == ERROR_NO) && (pattern -> ont_class -> classtype != NULL))
	{
		if (pattern -> uuid != NULL)
			ss_add_triple(&triples, pattern -> uuid, RDF_TYPE, pattern -> ont_class->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
		else
			ss_add_triple(&triples, SS_RDF_SIB_ANY, RDF_TYPE, pattern -> ont_class->classtype, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
	}

	/* Set all properties */
	if (pattern->check_prop_list && !list_empty(&pattern->check_prop_list->links)) {
		list_for_each(pos, &pattern->check_prop_list->links) {
			node = list_entry(pos, list_t, links);
			cond = (pattern_condition_t *)node->data;
			proptype = cond->prop;

			if (proptype == NULL) {
				set_error(ERROR_INCORRECT_PROPLIST);
				return NULL;
			}

			if (proptype->type == OBJECTPROPERTY) {
				if (cond->value == NULL)
				{
					ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				}
				else
				{
					if (sslog_get_rtti_type(cond->value) == RTTI_PATTERN) {
						object = (pattern_t *) cond->value;
						if (object->uuid != NULL)
							ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, object->uuid, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
						else
							ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
					}
					else if (sslog_get_rtti_type(cond->value) == RTTI_INDIVIDUAL) {
						object_ind = (individual_t *) cond->value;
						if (object_ind->uuid == NULL) {
							set_error(ERROR_INCORRECT_PROPERTY_VALUE);
							return NULL;
						}
						else {
							ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, object_ind->uuid, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
						}
					}
					else {
						set_error(ERROR_INCORRECT_PROPERTY_VALUE);
						return NULL;
					}
				}
			}
			else if (proptype->type == DATATYPEPROPERTY)
			{
				if (cond->value == NULL)
					ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
				else
					ss_add_triple(&triples, SS_RDF_SIB_ANY, proptype->name, (char *) cond->value, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
			}
		}
	}

	return triples;
}

/**
* @brief Checkes is there input item in list of individuals.
*
* @param char * uuid.
* @param list_head_t * individuals list.
*
* @return returns true if item founded in list and false otherwise.
*/
bool is_in_individuals_list(char *uuid, list_t *inds_list) {
	list_head_t *pos = NULL;

	if ((uuid == NULL) || (inds_list == NULL)) return false;

	if (list_empty(&inds_list->links)) return false;

	list_for_each(pos, &inds_list->links) {
		list_t *node = list_entry(pos, list_t, links);
		individual_t *ind = (individual_t *) node->data;

		if ((ind != NULL) && (ind->uuid != NULL) && (strcmp(ind->uuid, uuid) == 0))
			return true;
	}

	return false;
}


/**
* \fn ss_triple_t *triples_to_individuals (ss_triple_t *triples)
*
* \brief Converts triples to individuals.
*
* Converts properties without values and uuid with classtype to individual struct.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] ss_triple_t *triples. Pointer to the list of triples.
* \return list_t *. List of all individuals.
*/
list_t *triples_to_individuals (ss_triple_t *triples)
{
	list_t *inds = NULL;
	individual_t *ind2 = NULL;
	char *subject = NULL;
	char *predicate = NULL;
	char *object = NULL;
	ss_triple_t *copy_back = triples;

	reset_error();
	
	/* Create new list only if triples not NULL */
	reset_error();
	if (!triples) return NULL;
	
	inds = list_get_new_list();

	/* First create individual without properties (check only RDF TYPE triples) */
	while (triples)
	{
		subject = (char *) triples->subject;
		predicate = (char *) triples->predicate;
		object = (char *) triples->object;

		if (strcmp(predicate,RDF_TYPE) == 0)
		{
			/* If no such individual was created search for a class type and create new individual */
			const individual_t *ind = sslog_repo_get_individual_by_uuid(subject);
			if (!ind)
			{
				const class_t* ont_class = sslog_repo_get_class_by_classtype(object);
				if (ont_class)
				{
					ind2 = sslog_new_individual(ont_class);
					ind2->uuid = strdup(subject);
				}
				else
				{
					triples = triples -> next;
					continue;
				}
			}
			/* Add to list if it is not there */
			if (!is_in_individuals_list(subject, inds))
			{
				if (ind)
				{
					list_t *node = list_get_new_node((void *) ind);
					list_add(&node->links, &inds->links);
					ind = NULL;
				}
				else if (ind2)
				{
					list_t *node = list_get_new_node((void *) ind2);
					list_add(&node->links, &inds->links);
					ind2 = NULL;
				}
			}
		}

		triples = triples -> next;
	}

	/* Set properties for all individuals */
	triples = copy_back;
	while (triples) {
		subject = (char *) triples->subject;
		predicate = (char *) triples->predicate;
		object = (char *) triples->object;

		if (strcmp(predicate,RDF_TYPE) != 0) {
			const individual_t *ind = sslog_repo_get_individual_by_uuid(subject);
			if (!ind) {
				triples = triples -> next;
				continue;
			}
			else
			{
				const property_t * proptype = sslog_repo_get_property_by_name(predicate);
				if (proptype == NULL) {
					triples = triples -> next;
					continue;
				}

				if (proptype->type == OBJECTPROPERTY) {
				    // ind2 could be changed, so we need to cast "const individual_t *" to "individual_t *"
					ind2 = (individual_t *) sslog_repo_get_individual_by_uuid(object);
					/* If no such individual in OBJECT PROPERTY the try find it and create */
					if (!ind2) {
						ss_triple_t *request = NULL;
						ss_triple_t *first_triple = NULL;

						ss_add_triple(&request, object, RDF_TYPE, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

						if(ss_query(sslog_get_ss_info(), request, &first_triple) < 0) {
							set_error(ERROR_UNKNOWN);
							ss_delete_triples(request);
							triples = triples -> next;
							continue;
						}
						ss_delete_triples(request);
						if (first_triple == NULL) {
							set_error(ERROR_UNKNOWN);
							triples = triples -> next;
							continue;
						}

						const class_t* ont_class = sslog_repo_get_class_by_classtype(first_triple->object);
						ss_delete_triples(first_triple);
						if (ont_class) {
							ind2 = sslog_new_individual(ont_class);
							ind2->uuid = strdup(object);
						}
						else {
							set_error(ERROR_UNKNOWN);
							triples = triples -> next;
							continue;
						}
					}

					/* Remove old properties with same values to avoid duplication mismatch */
					sslog_remove_property_with_value((individual_t *) ind, (property_t *) proptype, ind2);
					sslog_add_property((void *) ind, (property_t *) proptype, ind2);
				}
				else if (proptype->type == DATATYPEPROPERTY) {
					/* Remove old properties with same values to avoid duplication mismatch */
					sslog_remove_property_with_value((individual_t *) ind, (property_t *) proptype, object);
					sslog_add_property((void *) ind, (property_t *) proptype, object);
				}
			}
		}

		triples = triples -> next;
	}

	return inds;
}

/**
* \fn individual_t *triples_to_individual_first (ss_triple_t *triples)
*
* \brief Converts triples to individual.
*
* Converts properties without values and uuid with classtype to individual struct.
* Function sets global SmartSlog KP Library's errno.
*
* \param[in] ss_triple_t *triples. Pointer to the list of triples.
* \return individual_t . First individual only.
*/
individual_t *triples_to_individual_first (ss_triple_t *triples)
{
	individual_t *ind_f = NULL;
	individual_t *ind2 = NULL;
	char *subject = NULL;
	char *predicate = NULL;
	char *object = NULL;
	ss_triple_t *copy_back = triples;

	reset_error();
	if (!triples) return NULL;

	/* First create individual without properties (check only RDF TYPE triples) */
	while (triples)
	{
		subject = (char *) triples->subject;
		predicate = (char *) triples->predicate;
		object = (char *) triples->object;

		if (strcmp(predicate,RDF_TYPE) == 0)
		{
			/* If no such individual was created search for a class type and create new individual */
			if (ind_f == NULL || ind_f->uuid == NULL)
			{
				const individual_t *ind = sslog_repo_get_individual_by_uuid(subject);
				if (!ind)
				{
					const class_t* ont_class = sslog_repo_get_class_by_classtype(object);
					if (ont_class)
					{
						ind2 = sslog_new_individual(ont_class);
						ind2->uuid = strdup(subject);
					}
					else
					{
						triples = triples -> next;
						continue;
					}
				}
				if (ind)
				{
				    // ind_f could be changed, so we need to cast "const individual_t *" to "individual_t *"
					ind_f = (individual_t *) ind;
				}
				else if (ind2)
				{
					ind_f = ind2;
				}
				break;
			}
			else break;
		}

		triples = triples -> next;
	}

	if ((ind_f == NULL) || (ind_f->uuid == NULL)) return NULL;

	/* Set properties for all individuals */
	triples = copy_back;
	while (triples)
	{
		subject = (char *) triples->subject;
		predicate = (char *) triples->predicate;
		object = (char *) triples->object;

		if ((strcmp(predicate,RDF_TYPE) != 0) && (strcmp(ind_f->uuid, subject) == 0))
		{
			const property_t * proptype = sslog_repo_get_property_by_name(predicate);
			if (proptype == NULL) {
				triples = triples -> next;
				continue;
			}

			if (proptype->type == OBJECTPROPERTY) {
			    // ind2 could be changed, so we need to cast "const individual_t *" to "individual_t *"
				ind2 = (individual_t *) sslog_repo_get_individual_by_uuid(object);
				/* If no such individual in OBJECT PROPERTY the try find it and create */
				if (!ind2) {
					ss_triple_t *request = NULL;
					ss_triple_t *first_triple = NULL;

					ss_add_triple(&request, object, RDF_TYPE, SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);

					if(ss_query(sslog_get_ss_info(), request, &first_triple) < 0) {
						set_error(ERROR_UNKNOWN);
						ss_delete_triples(request);
						triples = triples -> next;
						continue;
					}
					ss_delete_triples(request);
					if (first_triple == NULL) {
						set_error(ERROR_UNKNOWN);
						triples = triples -> next;
						continue;
					}

					const class_t* ont_class = sslog_repo_get_class_by_classtype(first_triple->object);
					ss_delete_triples(first_triple);
					if (ont_class) {
						ind2 = sslog_new_individual(ont_class);
						ind2->uuid = strdup(object);
					}
					else {
						set_error(ERROR_UNKNOWN);
						triples = triples -> next;
						continue;
					}
				}

				/* Remove old properties with same values to avoid duplication mismatch */
				sslog_remove_property_with_value(ind_f, (property_t *) proptype, ind2);
				sslog_add_property((void *) ind_f, (property_t *) proptype, ind2);
			}
			else if (proptype->type == DATATYPEPROPERTY) {
				/* Remove old properties with same values to avoid duplication mismatch */
				sslog_remove_property_with_value(ind_f, (property_t *) proptype, object);
				sslog_add_property((void *) ind_f, (property_t *) proptype, object);
			}

		}

		triples = triples -> next;
	}

	return ind_f;
}


/**
 * @brief Convert individual to triplets using given list properties.
 *
 * Checks properties, if they can be set for given individuals it convert it to
 * triples, without set object (use SS_RDF_SIB_ANY - any value).
 * It not convert object properties.
 *
 * @param ind individual.
 * @param properties list of properties.
 * It can be NULL - all properties that be set for individual are converted to triplets.
 *
 * @return 0 on success or not otherwise.
 */
ss_triple_t* individual_to_triples_by_properties_any(individual_t *ind, list_t *properties)
{
    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_MED) \
            ("\n%s individual_to_triples_by_properties_any START\n", KPLIB_DEBUG_KPIINTER_PREFIX);

    if (verify_individual_prop_list(ind, properties) != ERROR_NO) {
        KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_MED) \
                ("\n%s individual_to_triples_by_properties_any END: prop_list error\n", KPLIB_DEBUG_KPIINTER_PREFIX);

        return NULL;
    }

    list_t *convert_props =
            (properties == NULL) ? ind->parent_class->properties : properties;

    ss_triple_t *triples = NULL;
    list_head_t *list_walker = NULL;

    list_for_each (list_walker, &convert_props->links) {
         list_t *node = list_entry(list_walker, list_t, links);
         property_t *property = (property_t *) node->data;

        if (property->type == DATATYPEPROPERTY) {
            ss_add_triple(&triples, ind->uuid, property->name,
                    SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
        } else if (property->type == OBJECTPROPERTY) {
            ss_add_triple(&triples, ind->uuid, property->name,
                    SS_RDF_SIB_ANY, SS_RDF_TYPE_URI, SS_RDF_TYPE_URI);
        }
    }
    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_MED) \
            ("\n%s individual_to_triples_by_properties_any END\n", KPLIB_DEBUG_KPIINTER_PREFIX);
    return triples;
}


/** @endcond */

#endif	/* _KPI_INTERFACE_C_ */
