cmake_minimum_required(VERSION 2.6.4)

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)
pkg_search_module(glib REQUIRED glib-2.0)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS} ${glib_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS} ${glib_LIBRARY_DIRS})

add_library(commons SHARED
    common.c common.h st_point.c)

target_link_libraries(commons ontology ${smartslog_LIBRARIES} ${glib_LIBRARIES})

install(TARGETS commons
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ GROUP_WRITE GROUP_EXECUTE WORLD_READ WORLD_EXECUTE )

set_target_properties(commons
  PROPERTIES VERSION ${GENERIC_LIB_VERSION} SOVERSION ${GENERIC_LIB_SOVERSION}
)
