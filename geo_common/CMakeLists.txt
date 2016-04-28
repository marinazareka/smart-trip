cmake_minimum_required(VERSION 2.6.4)

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

find_library(M_LIB m)

add_library(geo_common SHARED geo_common.c loader.c cJSON.c)

target_link_libraries(geo_common ontology commons ${smartslog_LIBRARIES})
target_link_libraries(geo_common ${M_LIB})

install(TARGETS geo_common
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ GROUP_WRITE GROUP_EXECUTE WORLD_READ WORLD_EXECUTE )

set_target_properties(geo_common
  PROPERTIES VERSION ${GENERIC_LIB_VERSION} SOVERSION ${GENERIC_LIB_SOVERSION}
)
