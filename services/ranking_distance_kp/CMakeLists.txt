cmake_minimum_required(VERSION 2.6.4)

project(ranking_distance_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})

add_executable(ranking_distance_kp 
    main.c)

target_link_libraries(ranking_distance_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/ranking_distance_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

### install systemd file
if(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
install(FILES ranking-distance-kp.service
    DESTINATION ${SYSTEMD_CONFIGURATIONS_FILES_DIR})
endif(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
