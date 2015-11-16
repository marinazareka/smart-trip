cmake_minimum_required(VERSION 3.0)
project(travelling-smart-tourist)


add_subdirectory(ontology)
include_directories(ontology/gen)

add_subdirectory(common)
include_directories(common)

add_subdirectory(user_kp)
add_subdirectory(geo_kp)
add_subdirectory(transport-kp-native)
