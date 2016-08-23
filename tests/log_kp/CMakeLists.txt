cmake_minimum_required(VERSION 2.6.4)

project(log_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})

link_directories(${smartslog_LIBRARY_DIRS})

add_subdirectory(../../ontology ${CMAKE_CURRENT_BINARY_DIR}/ontology)
include_directories(../../ontology/gen)

add_subdirectory(../../libraries/common ${CMAKE_CURRENT_BINARY_DIR}/common)
include_directories(../../libraries/common)



if(NOT DEFINED CMAKE_INSTALL_BINDIR)
  set(CMAKE_INSTALL_BINDIR "\${prefix}/bin")
endif(NOT DEFINED CMAKE_INSTALL_BINDIR)



add_executable(log_kp 
    main.c)

target_link_libraries(log_kp commons ontology ${smartslog_LIBRARIES} ${CURL_LIBRARIES})

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/log_kp 
    DESTINATION ${CMAKE_INSTALL_BINDIR})
