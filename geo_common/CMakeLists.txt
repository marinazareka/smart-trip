cmake_minimum_required(VERSION 2.6.4)

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_library(geo_common geo_common.c loader.c cJSON.c)

target_link_libraries(geo_common ontology commons ${smartslog_LIBRARIES})
