cmake_minimum_required(VERSION 2.6.4)

project(transport_kp)

include(FindPkgConfig)

pkg_search_module(smartslog REQUIRED smartslog)

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

add_library(transport_kp SHARED
    main.c)

target_link_libraries(transport_kp commons ontology ${smartslog_LIBRARIES})

install(TARGETS transport_kp
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ GROUP_WRITE GROUP_EXECUTE WORLD_READ WORLD_EXECUTE )

set_target_properties(transport_kp
  PROPERTIES VERSION ${GENERIC_LIB_VERSION} SOVERSION ${GENERIC_LIB_SOVERSION}
)
