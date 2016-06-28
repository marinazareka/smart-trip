cmake_minimum_required(VERSION 2.6.4)

project(user_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_subdirectory(../../ontology ${CMAKE_CURRENT_BINARY_DIR}/ontology)
include_directories(../../ontology/gen)

add_subdirectory(../../common ${CMAKE_CURRENT_BINARY_DIR}/common)
include_directories(../../common)


add_executable(user_kp 
    main.c)

target_link_libraries(user_kp commons ontology ${smartslog_LIBRARIES})
