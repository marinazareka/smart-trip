cmake_minimum_required(VERSION 2.6.4)

project(transport_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_library(transport_kp SHARED
    main.c)

target_link_libraries(transport_kp commons ontology ${smartslog_LIBRARIES})
