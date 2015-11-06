cmake_minimum_required(VERSION 3.0)

project(geo_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_executable(geo_kp 
    main.c)

target_link_libraries(geo_kp commons ontology ${smartslog_LIBRARIES})
