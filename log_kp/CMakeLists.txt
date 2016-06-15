cmake_minimum_required(VERSION 2.6.4)

project(log_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})

add_executable(log_kp 
    main.c)

target_link_libraries(log_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/log_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})
