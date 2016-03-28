cmake_minimum_required(VERSION 2.6.4)

project(geo_kp)

include(FindPkgConfig)
include(FindCURL)

find_package(CURL REQUIRED)
pkg_search_module(smartslog REQUIRED smartslog)

find_library(M_LIB NAMES m)

include_directories(${smartslog_INCLUDE_DIRS})
include_directories(${CURL_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})
link_directories(${CURL_LIBRARY_DIRS})

add_executable(geo_wm_kp 
    main.c wm-loader.c)

target_link_libraries(geo_wm_kp ${M_LIB})
target_link_libraries(geo_wm_kp commons ontology geo_common ${smartslog_LIBRARIES} ${CURL_LIBRARIES})
target_link_libraries(geo_wm_kp m)

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/geo_wm_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

### install systemd file
install(FILES geo-wm-kp.service
    DESTINATION ${CMAKE_INSTALL_SYSCONFDIR}/systemd/system)
