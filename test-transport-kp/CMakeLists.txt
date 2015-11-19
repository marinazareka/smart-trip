cmake_minimum_required(VERSION 3.0)

project(test_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_executable(test_kp 
    main.c)

target_link_libraries(test_kp commons ontology transport_kp ${smartslog_LIBRARIES})
