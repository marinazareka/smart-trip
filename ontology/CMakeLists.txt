cmake_minimum_required(VERSION 2.6.4)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include(FindPkgConfig)
pkg_search_module(smartslog REQUIRED smartslog)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

include_directories(${smartslog_INCLUDE_DIRS})
link_directories(${smartslog_LIBRARY_DIRS})

if(NOT DEFINED CMAKE_INSTALL_LIBDIR)
  if (CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
    set(CMAKE_INSTALL_LIBDIR "\${prefix}/lib64")
  else (CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
    set(CMAKE_INSTALL_LIBDIR "\${prefix}/lib")
  endif(CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
endif(NOT DEFINED CMAKE_INSTALL_LIBDIR)

add_library(ontology SHARED
    gen/ontology.c gen/ontology.h)

target_link_libraries(ontology ${smartslog_LIBRARIES})

install(TARGETS ontology
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ GROUP_WRITE GROUP_EXECUTE WORLD_READ WORLD_EXECUTE )

set_target_properties(ontology
  PROPERTIES VERSION ${GENERIC_LIB_VERSION} SOVERSION ${GENERIC_LIB_SOVERSION}
)
