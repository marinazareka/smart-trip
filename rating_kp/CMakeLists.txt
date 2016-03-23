cmake_minimum_required(VERSION 3.0)

project(rating_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})

add_executable(rating_kp 
    main.c)

target_link_libraries(rating_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})
