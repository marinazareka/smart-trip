/**
 * @file classes.c
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

#ifdef KPI_LOW
        #include <kpilow/kpi_low.h>
#else                 
        #include <ckpi/ckpi.h>
#endif        

#include <string.h>
#include "classes.h"
#include "structures.h"
#include "ss_func.h" // KPLIB_UUID_MAX_LEN
#include "utils/check_func.h"
#include "utils/kp_error.h"
#include "utils/util_func.h"
#include "utils/list.h"


#ifndef _CLASSES_C
#define	_CLASSES_C

/**
 * @brief Repository for classes.
 * All classes must be inserted to repository.
 * It's used for search and other check things.
 */
static list_t *g_classes_repository = NULL;

/**
 * @brief Repository for individuals.
 * All individuals inserted in repository, then it created by @see new_individual.
 * You do not need add created individual again by hand.
 */
static list_t *g_individuals_repository = NULL;

/**
 * @brief Repository for properties. 
 * All properties must be inserted to repository. 
 */
static list_t *g_properties_repository = NULL;



/******************************************************************************/
/*************************** Static functions list ****************************/
static list_t *get_global_repository_by_rtti(int rtti);
static void add_individual_to_repository(individual_t *individual);
static void add_individual_to_class(class_t *ont_class, individual_t *individual);



/******************************************************************************/
/**************************** External functions ******************************/
/// @cond EXTERNAL_FUNCTIONS
/**
 * @brief Create new individual.
 *
 * Create new individual without UUID.
 *
 * @param classtype type of class.
 *
 * @return individual of given class type on success or NULL otherwise.
 */
SSLOG_EXTERN individual_t* sslog_new_individual(const class_t *ont_class)
{
    reset_error();

    int error_code = verify_class(ont_class);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        return NULL;
    }

    individual_t *individual = (individual_t *) malloc(sizeof(individual_t));

    if (individual == NULL) {
        set_error(ERROR_OUT_OF_MEMORY);
        return NULL;
    }

    // Sets different fields of the new created individuals
    individual->rtti = RTTI_INDIVIDUAL;
    individual->parent_class = ont_class;
    individual->classtype = strdup(ont_class->classtype);
    individual->properties = NULL;
    individual->uuid = NULL; //sslog_generate_uuid();
    individual->subscribe_status = SBCR_STATUS_NO;

    error_code = verify_individual(individual);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        sslog_free_individual(individual);
        return NULL;
    }

    add_individual_to_class((class_t *) ont_class, individual);
    add_individual_to_repository(individual);

    return individual;
}


/**
 * @brief Returns a list of superclasses.
 *
 * @param class class.
 *
 * @return list of superclasses on success or NULL otherwise.
 */
SSLOG_EXTERN list_t* sslog_get_superclass_all(const class_t *ont_class)
{
    reset_error();

    int error_code = verify_class(ont_class);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        return NULL;
    }

    if (ont_class->superclasses == NULL) {
        return NULL;
    }
/*
    list_t superclasses = list_get_new_list();

    list_add_list(superclasses, ont_class->superclasses);


     list_head_t *list_walker = NULL;
     list_for_each (list_walker, &superclasses->links) {
        list_t *node = list_entry(list_walker, list_t, links);
        class_t *ont_class = (class_t *) node->data;

        if (ont_class == NULL) {
            continue;
        } else if (ont_class->superclasses != NULL) {
            list_add_list(superclasses, ont_class->superclasses);
        }

        if (list_count_nodes_with_data(superclasses, (void *) ont_class) > 1) {
            list_del_and_free_node(node, NULL);
        }

    }
*/
    return ont_class->superclasses;
}


/**
 * @brief Checks if class has superclass.
 *
 * @param class class for check.
 * @param superclass alleged superclass of class.
 *
 * @return 1 if the class is a subclass of superclass, 0 if no and -1 on error.
 */
SSLOG_EXTERN int sslog_is_subclass_of(const class_t *subclass, const class_t *superclass)
{
    reset_error();

    int error_code = verify_class(subclass);
    error_code = verify_class(superclass);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        return -1;
    }

    if (subclass->superclasses == NULL) {
        return 0;
    }

    // Compare classatypes
    list_head_t *pos = NULL;
    list_for_each(pos, &subclass->superclasses->links) {
         list_t *list = list_entry(pos, list_t, links);
         class_t *test_class = (class_t *)list->data;

         if (strcmp(test_class->classtype, superclass->classtype) == 0) {
            return 1;
        }
    }

    return 0;
}


/**
 * @brief Checks type of individual.
 *
 * @param individual object of some class for check.
 * @param ont_class allaged class of individual.
 *
 * @return returns 1 if the individual is object of given class, 0 if no and -1 on error.
 */
SSLOG_EXTERN int sslog_is_classtype_of(const individual_t *individual, const class_t *ont_class)
{
    int error_code = verify_class(ont_class);
	
    if (error_code != ERROR_NO) {
        set_error(error_code);
        return -1;
    }

    error_code = verify_individual(individual);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        return -1;
    }

    if (strcmp(ont_class->classtype, individual->classtype) == 0) {
        return 1;
    }

    return 0;
}


/**
 * @brief Create new property value structure.
 *
 * This function copy only value for data-property (i.e. char*), 
 * if property is object then value is pointer to given value (i.e. individual).
 *
 * @param prop property for value.
 * @param data data for value of property.
 *
 * @return new property value structure or NULL on error.
 */
SSLOG_EXTERN prop_val_t* sslog_new_value_struct(property_t *prop, const void *data)
{
    if (prop == NULL) {
        return NULL;
    }

    prop_val_t *value = (prop_val_t *) malloc(sizeof(prop_val_t));
    value->property = prop;
    
    // If we create a value for data-property then we make copy. 
    if (sslog_is_object_property(prop) == true) {
        value->prop_value = (void *) data;
    } else {
        value->prop_value = sslog_strndup((const char *) data, SS_OBJECT_MAX_LEN);
    }


    return value;
}


/**
 * @brief Count references to given individual.
 *
 * It gets other individuals and search references to individual from their
 * properties.
 *
 * @param individual individual for count.
 *
 * @return count of references.
 */
SSLOG_EXTERN int sslog_rep_count_individual_references(individual_t *individual)
{
    if (individual == NULL) {
        return 0;
    }

    list_t *repository = get_global_repository_by_rtti(RTTI_INDIVIDUAL);

    if (repository == NULL) {
        return 0;
    }

    int ind_counter = 0;

    /* Checking properties of every individual to point to the given individual.
       The given individual and individuals with no properties are ignored. */    
    list_head_t *ind_walker = NULL;
    list_for_each (ind_walker, &repository->links) {
         list_t *list = list_entry(ind_walker, list_t, links);
         individual_t *test_ind = (individual_t *) list->data;

         if (test_ind == individual
                 || list_is_null_or_empty(test_ind->properties) == 1) {
             continue;
         }

         list_head_t *prop_walker = NULL;
         list_for_each (prop_walker, &test_ind->properties->links) {
            list_t *list = list_entry(prop_walker, list_t, links);
            prop_val_t *test_propval = (prop_val_t *) list->data;

            if (test_propval == NULL) {
                continue;
            } else if (test_propval->prop_value == individual) {
                ++ind_counter;
            }
         }
    }

    return ind_counter;
}


/**
 * @brief Set UUID.
 *
 * Set new UUID for individuals. You need reinitialize individual to work with SS.
 *
 * @param ind individual.
 * @param uuid UUID.
 *
 * @return ERROR_NO on success or error code otherwise.
 */
SSLOG_EXTERN int sslog_set_individual_uuid(individual_t *ind, const char *uuid)
{
   int error_code = verify_individual(ind);

   if (error_code != ERROR_NO) {
        return set_error(error_code);
   }

   if (is_str_null_or_empty(uuid) == true) {
       return set_error(ERROR_INCORRECT_UUID);
   }

   if (ind->uuid != NULL) {
       free(ind->uuid);
   }

   ind->uuid = sslog_strndup(uuid, KPLIB_UUID_MAX_LEN);  

   return reset_error();
}


/**
 * @brief Add entity to repository.
 *
 * It set error and return it on failure.
 * You need to add individual, if you create it manually.
 * Function @see sslog_new_individual() add individual to repository automatically.
 *
 * @param entity individual, property or class.
 *
 * @return ERROR_NO on success or not otherwise.
 */
SSLOG_EXTERN int sslog_repo_add_entity(void *entity)
{
    int error_code = verify_entity(entity);

    if (error_code != ERROR_NO) {
        return set_error(error_code);
    }

    int rtti = sslog_get_rtti_type(entity);

    list_t *repository = get_global_repository_by_rtti(rtti);

    if (repository == NULL) {
        return set_error(ERROR_INCORRECT_ENTITY);
    }
    
    // Blocking to adding the entity to repository more than once.
    if (list_count_nodes_with_data(repository, entity) == 0) {
        list_add_data(entity, repository);
    }

    return reset_error();
}


/**
 * @brief Add class to class repository.
 *
 * It set error and return it on failure.
 *
 * @param ont_class class for insert.
 *
 * @return ERROR_NO on success or not otherwise.
 */
SSLOG_EXTERN int sslog_repo_add_class(class_t *ont_class)
{
    int error_code = verify_class(ont_class);

    if (error_code != ERROR_NO) {
        set_error(error_code);
        return error_code;
    }

    g_classes_repository = list_get_new_list_if_null(g_classes_repository);
    list_add_data(ont_class, g_classes_repository);

    reset_error();
    return ERROR_NO;
}


/**
 * @brief Add property to property repository.
 *
 * It set error and return it on failure.
 *
 * @param property for insert.
 *
 * @return ERROR_NO on success or not otherwise.
 */
SSLOG_EXTERN int sslog_repo_add_property(property_t *property)
{
    int error_code = verify_property(property);

    if (error_code != ERROR_NO) {
        return set_error(error_code);
    }

    g_properties_repository = list_get_new_list_if_null(g_properties_repository);
    list_add_data(property, g_properties_repository);

    return reset_error();
}


/**
 * @brief Get property from repository by given name.
 *
 * @param name property name.
 *
 * @return property from repository or NULL if it not found.
 */
SSLOG_EXTERN const property_t* sslog_repo_get_property_by_name(const char *name)
{
    list_t *repository = get_global_repository_by_rtti(RTTI_PROPERTY);

    if (is_str_null_or_empty(name) == true
            || list_is_null_or_empty(repository) == 1) {
        KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_BMED) \
                ("%s get_property_from_repository_by_name: parameter error or repository = NULL", \
                KPLIB_DEBUG_CLASSSES_PREFIX);

        return NULL;
    }

    // Gets properties and checks their names.
    list_head_t *list_walker = NULL;
    list_for_each (list_walker, &repository->links) {
         list_t *list = list_entry(list_walker, list_t, links);
         property_t *test_prop = (property_t *) list->data;

         if (strcmp(test_prop->name, name) == 0) {
            return test_prop;
        }
    }

    return NULL;
}


/**
 * @brief Get class from repository by given classtype.
 *
 * @param classtype name of classtype.
 *
 * @return class from repository or NULL if it not found.
 */
SSLOG_EXTERN const class_t* sslog_repo_get_class_by_classtype(const char *classtype)
{
    list_t *repository = get_global_repository_by_rtti(RTTI_CLASS);

    if (is_str_null_or_empty(classtype) == true
            || list_is_null_or_empty(repository) == 1) {
        return NULL;
    }

    // Gets classes and checks their classtypes.
    list_head_t *list_walker = NULL;
    list_for_each (list_walker, &repository->links) {
         list_t *list = list_entry(list_walker, list_t, links);
         class_t *test_class = (class_t *) list->data;

         if (strcmp(test_class->classtype, classtype) == 0) {
            return test_class;
        }
    }

    return NULL;
}


/**
 * @brief Get individuals from repository by given classtype.
 *
 * @param classtype name of classtype.
 *
 * @return individuals list or NULL if not found. Do not free list.
 */
SSLOG_EXTERN const list_t* sslog_repo_get_individual_by_classtype_all(const char *classtype)
{
    const class_t *ont_class = sslog_repo_get_class_by_classtype(classtype);

    if (ont_class == NULL) {
        return NULL;
    }

    return ont_class->instances;
}


/**
 * @brief Get individuals from repository by given uuid.
 *
 * @param uuid individual's uuid.
 *
 * @return individual or NULL if it not found.
 */
SSLOG_EXTERN const individual_t* sslog_repo_get_individual_by_uuid(const char *uuid)
{
    list_t *repository = get_global_repository_by_rtti(RTTI_INDIVIDUAL);

    if (is_str_null_or_empty(uuid) == true
            || list_is_null_or_empty(repository) == 1) {
        return NULL;
    }
    
    // Checks individuals UUIDs. 
    list_head_t *list_walker = NULL;
    list_for_each (list_walker, &repository->links) {
         list_t *list = list_entry(list_walker, list_t, links);
         individual_t *test_ind = (individual_t *) list->data;

         if (test_ind->uuid == NULL) {
             continue;
         }

         if (strcmp(test_ind->uuid, uuid) == 0) {
             return test_ind;
         }
    }

    return NULL;
}


/**
 * @brief Remove entity from repository.
 *
 * @param entity individual, class or property.
 */
SSLOG_EXTERN void sslog_repo_remove_entity(void *entity)
{
    int rtti = sslog_get_rtti_type(entity);

    if (rtti <= RTTI_MIN_VALUE) {
        return;
    }

    list_t *repository = get_global_repository_by_rtti(rtti);

    list_del_and_free_nodes_with_data(repository, entity, NULL);
}


// FIXME: need get all classes, now only parent.
/**
 * @brief Gets all inheritance classes.
 *
 * @param individual individual for get parents.
 *
 * @return returns list with classes on success or NULL otherwise.
 */
SSLOG_EXTERN list_t* get_individual_inheritance_classes(individual_t *individual)
{
    if (verify_individual(individual) != ERROR_NO) {
        return NULL;
    }

    const class_t *parent = individual->parent_class;

    list_t *classes = list_get_new_list();

    list_add_data((void *)parent, classes);
    return classes;
}


void sslog_repo_clean_all()
{
    list_free_with_nodes(g_individuals_repository, (void (*)(void *)) sslog_free_individual);
    g_individuals_repository = NULL;

    list_free_with_nodes(g_classes_repository, (void (*)(void *)) sslog_free_class);
    g_classes_repository = NULL;

    list_free_with_nodes(g_properties_repository, (void (*)(void *)) sslog_free_property);
    g_properties_repository = NULL;
}
/// @endcond


/******************************************************************************/
/***************************** Static functions *******************************/
/**
 * @brief Gets a global repository by given RTTI.
 *
 * @param rtti Run-time type information.
 *
 * @return global repository.
 */
static list_t *get_global_repository_by_rtti(int rtti)
{
    switch (rtti) {
        case RTTI_INDIVIDUAL: {
            g_individuals_repository = list_get_new_list_if_null(g_individuals_repository);
            return g_individuals_repository;
            break;
        } case RTTI_PROPERTY: {
            g_properties_repository = list_get_new_list_if_null(g_properties_repository);
            return g_properties_repository;
            break;
        } case RTTI_CLASS: {
            g_classes_repository = list_get_new_list_if_null(g_classes_repository);
            return g_classes_repository;
            break;
        }
    }
    return NULL;
}


/**
 * @brief Add individual to repository.
 *
 * It used when new individual create.
 *
 * @param individual individual for insert.
 */
static void add_individual_to_repository(individual_t *individual)
{
    if (individual == NULL) {
        return;
    }

    list_t *repo = get_global_repository_by_rtti(RTTI_INDIVIDUAL);

    list_add_data(individual, repo);
}


/**
 * @brief Add individual to instances list for given class.
 *
 * @param ont_class class.
 * @param individual individual;
 */
static void add_individual_to_class(class_t *ont_class,
        individual_t *individual)
{
    if (ont_class == NULL || individual == NULL) {
        return;
    }

    ont_class->instances = list_get_new_list_if_null(ont_class->instances);

    list_add_data((void *) individual, ont_class->instances);
}


#endif	/* _CLASSES_C */
