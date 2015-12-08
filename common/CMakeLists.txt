cmake_minimum_required(VERSION 3.0)

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)
pkg_search_module(glib REQUIRED glib-2.0)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS} ${glib_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS} ${glib_LIBRARY_DIRS})

add_library(commons 
    common.c common.h st_point.c)

target_link_libraries(commons ontology ${smartslog_LIBRARIES} ${glib_LIBRARIES})
