cmake_minimum_required(VERSION 3.0)
project(travelling-smart-tourist)

SET(CMAKE_C_FLAGS "-std=gnu99 -g")

add_subdirectory(ontology)
include_directories(ontology/gen)

add_subdirectory(common)
include_directories(common)

add_subdirectory(user_kp)
add_subdirectory(geo_kp)
add_subdirectory(transport-kp-native)
