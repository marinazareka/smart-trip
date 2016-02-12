cmake_minimum_required(VERSION 3.0)
project(travelling-smart-tourist)

SET(CMAKE_C_FLAGS "-std=gnu99 -g")

add_subdirectory(ontology)
include_directories(ontology/gen)

add_subdirectory(common)
include_directories(common)

add_subdirectory(geo_common)
include_directories(geo_common)

add_subdirectory(user_kp)
add_subdirectory(geo_wm_kp)
add_subdirectory(geo_db_kp)
add_subdirectory(transport-kp-native)
add_subdirectory(clean_kp)
add_subdirectory(time_plan_kp)
add_subdirectory(test/test_user_kp)
