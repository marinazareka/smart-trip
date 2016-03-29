cmake_minimum_required(VERSION 2.6.4)

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

add_executable(geo_db_kp 
    main.c dbpedia-loader.c)

target_link_libraries(geo_db_kp ${M_LIB})
target_link_libraries(geo_db_kp commons ontology geo_common ${smartslog_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/geo_db_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

### install systemd file
install(FILES geo-db-kp.service
    DESTINATION ${SYSTEMD_CONFIGURATIONS_FILES_DIR})
