cmake_minimum_required(VERSION 3.0)

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_library(commons 
    common.c common.h)

target_link_libraries(commons ontology ${smartslog_LIBRARIES})
