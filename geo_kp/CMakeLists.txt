cmake_minimum_required(VERSION 3.0)

project(geo_kp)

include(FindPkgConfig)
include(FindCURL)

find_package(CURL REQUIRED)
pkg_search_module(smartslog REQUIRED smartslog)

find_library(M_LIB m)

include_directories(${smartslog_INCLUDE_DIRS})
include_directories(${CURL_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})
link_directories(${CURL_LIBRARY_DIRS})

add_executable(geo_kp 
    main.c loader.c test-loader.c wm-loader.c cJSON.c dbpedia-loader.c)

target_link_libraries(geo_kp ${M_LIB})
target_link_libraries(geo_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})
