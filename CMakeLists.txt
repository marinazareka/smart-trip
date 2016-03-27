cmake_minimum_required(VERSION 3.0)
project(travelling-smart-tourist)

SET(CMAKE_C_FLAGS "-std=gnu99 -g")

### check system directories
if(NOT DEFINED CMAKE_INSTALL_LIBDIR)
  if (CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
    set(CMAKE_INSTALL_LIBDIR "\${prefix}/lib64")
  else (CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
    set(CMAKE_INSTALL_LIBDIR "\${prefix}/lib")
  endif(CMAKE_SYSTEM_PROCESSOR EQUAL "x86_64")
endif(NOT DEFINED CMAKE_INSTALL_LIBDIR)

if(NOT DEFINED INCLUDE_INSTALL_DIR)
  set(INCLUDE_INSTALL_DIR "\${prefix}/include")
endif(NOT DEFINED INCLUDE_INSTALL_DIR)

if(NOT DEFINED CMAKE_INSTALL_BINDIR)
  set(CMAKE_INSTALL_BINDIR "\${prefix}/bin")
endif(NOT DEFINED CMAKE_INSTALL_BINDIR)

if(NOT DEFINED CMAKE_INSTALL_SYSCONFDIR)
  set(CMAKE_INSTALL_SYSCONFDIR "\${prefix}/../etc")
endif(NOT DEFINED CMAKE_INSTALL_SYSCONFDIR)

### install config file
install(FILES config.ini
    DESTINATION ${CMAKE_INSTALL_SYSCONFDIR}/smart-trip
    COMPONENT config)

### include subprojects
add_subdirectory(ontology)
include_directories(ontology/gen)

add_subdirectory(common)
include_directories(common)

add_subdirectory(geo_common)
include_directories(geo_common)

add_subdirectory(user_kp)
add_subdirectory(geo_wm_kp)
add_subdirectory(geo_db_kp)
add_subdirectory(transport-kp-native)
add_subdirectory(clean_kp)
add_subdirectory(time_plan_kp)
add_subdirectory(test/test_user_kp)
add_subdirectory(time_review_kp)
