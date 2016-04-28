cmake_minimum_required(VERSION 2.6.4)

project(geo_names)

include(FindPkgConfig)
include(FindCURL)

find_package(CURL REQUIRED)
pkg_search_module(smartslog REQUIRED smartslog)
find_package(LibXml2 REQUIRED)

find_library(M_LIB m)

include_directories(${smartslog_INCLUDE_DIRS})
include_directories(${LIBXML2_INCLUDE_DIR})
include_directories(${CURL_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})
link_directories(${LIBXML2_LIBRARIES})
link_directories(${CURL_LIBRARY_DIRS})

add_executable(geo_names_kp 
    main.c geonames-loader.c)

target_link_libraries(geo_names_kp ${M_LIB})
target_link_libraries(geo_names_kp commons ontology geo_common ${smartslog_LIBRARIES} ${LIBXML2_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/geo_names_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

### install systemd file
if(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
install(FILES geo-names-kp.service
    DESTINATION ${SYSTEMD_CONFIGURATIONS_FILES_DIR})
endif(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
