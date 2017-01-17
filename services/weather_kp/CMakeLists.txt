cmake_minimum_required(VERSION 2.6.4)

project(weather_kp)

include(FindPkgConfig)
include(FindCURL)

find_package(CURL REQUIRED)
pkg_search_module(smartslog REQUIRED smartslog)

pkg_search_module(libxml-2.0 REQUIRED libxml-2.0)

include_directories(${smartslog_INCLUDE_DIRS} ${libxml-2.0_INCLUDE_DIRS})
include_directories(${CURL_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS} ${libxml-2.0_LIBRARY_DIRS})
link_directories(${CURL_LIBRARY_DIRS})

if (NOT TARGET ontology)
add_subdirectory(../../ontology ${CMAKE_CURRENT_BINARY_DIR}/ontology)
include_directories(../../ontology/gen)
endif (NOT TARGET ontology)

if (NOT TARGET commons)
add_subdirectory(../../libraries/common ${CMAKE_CURRENT_BINARY_DIR}/common)
include_directories(../../libraries/common)
endif (NOT TARGET commons)

add_executable(weather_kp 
    main.c)

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/weather_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

target_link_libraries(weather_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES} ${libxml-2.0_LIBRARIES})

### install systemd file
if(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
install(FILES weather-kp.service
    DESTINATION ${SYSTEMD_CONFIGURATIONS_FILES_DIR})
endif(NOT DEFINED UPSTART_CONFIGURATIONS_FILES_DIR)
