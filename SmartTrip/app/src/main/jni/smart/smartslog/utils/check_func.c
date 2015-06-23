/**
 * @file check_func.c
 * @Author Alaxandr A. Lomov <lomov@cs.karelia.ru>
 * @date   11 January, 2010
 * @brief  Functions for different checks.
 *
 *
 * @section LICENSE
 *
 * PetrSU KP Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PetrSU KP Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PetrSU KP Library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 *
 *
 * @section DESCRIPTION
 *
 * Contains functions for different checks.
 * This file is part of PetrSU KP Library.
 */

//#include <smartslog/utils/kp_errors_define_decl.txt>

#include "check_func.h"


/******************************************************************************/
/***************************** External functions *****************************/
/// @cond EXTERNAL_FUNCTIONS

/**
 * @brief Checks string to null or empty value.
 *
 * @param string string for check.
 *
 * @return true if string is not null or empty, false otherwise.
 */
SSLOG_EXTERN bool is_str_null_or_empty(const char* string)
{
    if (string == NULL) {
        return true;
    }

    if (*string == '\0') {
        return true;
    }

    return false;
}
/// @endcond



/******************************************************************************/
/***************************** INTERNAL functions *****************************/
/// @cond INTERNAL_FUNCTIONS

/**
 * @brief Checks class structure.
 *
 * Checks: class struct not equals NULL; RTTI equals class; classtype set;
 *
 * @param ont_class class for check.
 *
 * @return ERROR_NO if checks not fails or error code otherwise.
 */
int verify_class(const class_t *ont_class)
{
    int error_code = ERROR_NO;

    if (ont_class == NULL) {
        error_code = ERROR_INCORRECT_CLASS;
    } else if (ont_class->rtti != RTTI_CLASS) {
        error_code = ERROR_INCORRECT_CLASS;
    } else if (is_str_null_or_empty(ont_class->classtype) == 1) {
        error_code = ERROR_INCORRECT_CLASSTYPE;
    }

    return error_code;
}

/**
 * @brief Checks individual structure.
 *
 * Checks: individual structure not equals NULL; RTTI equals individual;
 * class set; class type; classtype set and equals parent class type;
 *
 * @param individual individual for check.
 *
 * @return ERROR_NO if checks not fails or error code otherwise.
 */
int verify_individual(const individual_t *individual)
{
    if (individual == NULL) {
        KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_individual END: " \
                    "individual is null", KPLIB_DEBUG_CHECKS_PREFIX);
        return ERROR_INCORRECT_INDIVIDUAL;
    }

    int error_code = ERROR_NO;

    if (individual->rtti != RTTI_INDIVIDUAL) {
        error_code = ERROR_INCORRECT_RTTI;
    } else if (is_str_null_or_empty(individual->classtype) == 1) {
        error_code = ERROR_INCORRECT_CLASSTYPE;
    } else if (individual->parent_class == NULL) {
        error_code = ERROR_INCORRECT_INDIVIDUAL_PARENTCLASS;
    } else if (is_str_null_or_empty(individual->parent_class->classtype) == 1) {
        error_code = ERROR_INCORRECT_INDIVIDUAL_PARENTCLASS;
    } else if (strcmp(individual->parent_class->classtype,
            individual->classtype) != 0) {
        error_code = ERROR_INCORRECT_INDIVIDUAL_PARENTCLASS;
        //    } else if (is_str_null_or_empty(individual->uuid) == true) {
        //        error_code = ERROR_INCORRECT_UUID;
    }

    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_individual END: " \
            "UUID = %s, error_code = %i",        \
            KPLIB_DEBUG_CHECKS_PREFIX, individual->uuid, error_code);

    return error_code;
}

/**
 * @brief Checks entity(class, individual, property) structure.
 *
 * @param entity one of structures: class, individual, property.
 *
 * @return ERROR_NO if checks not fails or error code otherwise.
 */
int verify_entity(const void *entity)
{
    if (entity == NULL) {
        return ERROR_INCORRECT_ENTITY;
    }

    int rtti_type = sslog_get_rtti_type(entity);

    if ((rtti_type >= RTTI_MAX_VALUE) || (rtti_type <= RTTI_MIN_VALUE)) {
        return ERROR_INCORRECT_ENTITY;
    }

    int error_code = ERROR_NO;
    switch (rtti_type) {
        case RTTI_CLASS:
        {
            error_code = verify_class((class_t *) entity);
            break;
        }
        case RTTI_INDIVIDUAL:
        {
            error_code = verify_individual((individual_t *) entity);
            break;
        }
        case RTTI_PROPERTY:
        {
            error_code = verify_property((property_t *) entity);
            break;
        }
        case RTTI_SUBSCRIPTION:
        {
            error_code = verify_subscription(
                    (subscription_t *) entity);
        }
    }
    return error_code;
}

/**
 * @brief Checks property structure.
 *
 * Checks: class struct not equals NULL, RTTI, property name, type and about field.
 *
 * @param prop property for check.
 *
 * @return 0 if checks not fails or error code otherwise.
 */
int verify_property(const property_t *property)
{
    if (property == NULL) {
        KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_property END: " \
            "property is null", KPLIB_DEBUG_CHECKS_PREFIX);
        return ERROR_INCORRECT_PROPERTY;
    }

    if (sslog_get_rtti_type(property) != RTTI_PROPERTY) {
        KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_property END: " \
            "incorrect RTTI", KPLIB_DEBUG_CHECKS_PREFIX);
        return ERROR_INCORRECT_RTTI;
    }

    int error_code = ERROR_NO;

    if (is_str_null_or_empty(property->name) == true) {
        error_code = ERROR_INCORRECT_PROPERTY_NAME;
    } else if (is_str_null_or_empty(property->about) == true) {
        error_code = ERROR_INCORRECT_PROPERTY_ABOUT;
    } else if (property->type != OBJECTPROPERTY
            && property->type != DATATYPEPROPERTY) {
        error_code = ERROR_INCORRECT_PROPERTY_TYPE;
    }

    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_property END: " \
            "About = %s, error_code = %i",        \
            KPLIB_DEBUG_CHECKS_PREFIX, property->name, error_code);

    return error_code;
}


//INFO: No checks for container data.

/**
 * @brief Checks subscription container structure.
 *
 * Checks: class struct not equals NULL, RTTI and list of subscription data.
 *
 * @param container subscription container for check.
 *
 * @return 0 if checks not fails or error code otherwise.
 */
int verify_subscription(const subscription_t *subscription)
{
    int error_code = ERROR_NO;

    if (subscription == NULL) {
        return ERROR_NULL_ARGUMENT;
    }

    if (sslog_get_rtti_type(subscription) != RTTI_SUBSCRIPTION) {
        return ERROR_INCORRECT_SUBSCRIPTION;
    }

    return error_code;
}

/**
 * @brief Checks individual and property list.
 *
 * Checks: individual and try to find properties for given individual.
 *
 * @param individual individual for check.
 * @param prop_list properties list for chek for given individual.
 *
 * @return 0 if checks not fails or error code otherwise.
 */
int verify_individual_prop_list(const individual_t *individual, list_t *prop_list)
{
    int error_code = verify_individual(individual);

    if (error_code != ERROR_NO) {
        return error_code;
    }

    list_head_t *list_walker = NULL;

    list_for_each(list_walker, &prop_list->links)
    {
        list_t *node = list_entry(list_walker, list_t, links);
        property_t *test_prop = (property_t *) node->data;

        if (verify_property(test_prop) != ERROR_NO) {
            return ERROR_INCORRECT_PROPLIST;
        }

        if (sslog_get_property_type(individual->parent_class, test_prop->name) == NULL) {
            return ERROR_INCORRECT_PROPLIST;
        }

    }

    return error_code;
}

/**
 * @brief Checks pattern structure.
 *
 * Checks: pattern structure not equals NULL, RTTI.
 *
 * @param pattern pattern to check.
 *
 * @return ERROR_NO if checks not fails or error code otherwise.
 */
int verify_pattern(const pattern_t *pattern)
{
    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_pattern START",        \
            KPLIB_DEBUG_CHECKS_PREFIX);
    int error_code = ERROR_NO;

    if (pattern == NULL) {
        error_code = ERROR_INCORRECT_PATTERN;
    } else if (pattern->rtti != RTTI_PATTERN) {
        error_code = ERROR_INCORRECT_PATTERN;
    }

    KPLIB_DEBUG(KPLIB_DEBUG_LEVEL_LOW) ("\n%s verify_pattern END: error_code = %i",        \
            KPLIB_DEBUG_CHECKS_PREFIX, error_code);

    return error_code;
}

/**
 * @brief Checks subscription container structure.
 *
 * Checks: class struct not equals NULL, RTTI and list of subscription data.
 *
 * @param container subscription container for check.
 *
 * @return 0 if checks not fails or error code otherwise.
 */
int verify_subscription_changes(const subscription_changes_data_t *changes)
{
    if (changes == NULL) {
        return ERROR_NULL_ARGUMENT;
    }

    if (sslog_get_rtti_type(changes) != RTTI_SUBSCRIPTION_CHANGES) {
        return ERROR_INCORRECT_ENTITY;
    }

    return ERROR_NO;
}


/// @endcond
