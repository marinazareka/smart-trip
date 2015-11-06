cmake_minimum_required(VERSION 3.0)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_library(ontology 
    gen/ontology.c gen/ontology.h)

target_link_libraries(ontology ${smartslog_LIBRARIES})
