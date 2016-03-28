cmake_minimum_required(VERSION 3.0)

project(time_plan_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})

add_executable(time_plan_kp 
    main.c)

target_link_libraries(time_plan_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/time_plan_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})

### install systemd file
install(FILES time-plan-kp.service
    DESTINATION ${CMAKE_INSTALL_SYSCONFDIR}/systemd/system)
