##########################################################
# CMake Configuration

cmake_minimum_required(VERSION 2.8 FATAL_ERROR)

project(focusit)

#enable_testing()

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "build")

include(CheckLibraryExists)
include(CheckSymbolExists)
include(CheckFunctionExists)

##########################################################
# Paths
set(SRC "src/main/cpp")
#set(SRC_TEST "src/test/cpp")
set(BIN "build")
set(OUTPUT "bond")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${BIN})

#set(CMAKE_VERBOSE_MAKEFILE "ON")
##########################################################
# Libraries & Code

find_package(PkgConfig REQUIRED)

set(Boost_USE_STATIC_LIBS ON)
set(Boost_USE_MULTITHREADED ON)
unset(Boost_INCLUDEDIR CACHE)
unset(Boost_LIBRARYDIR CACHE)

set(Boost_NO_SYSTEM_PATHS TRUE) 

if (Boost_NO_SYSTEM_PATHS)
  set(BOOST_ROOT "./boost1.57")
  set(BOOST_INCLUDEDIR "${BOOST_ROOT}/boost")
  set(BOOST_LIBRARYDIR "${BOOST_ROOT}/stage/lib")
endif (Boost_NO_SYSTEM_PATHS)

FIND_PACKAGE( Boost 1.55 COMPONENTS log log_setup thread date_time filesystem system program_options REQUIRED )

find_package(JNI REQUIRED)
include_directories("src/java_crw_demo")
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})
include_directories( ${Boost_INCLUDE_DIR} )

set(SOURCE_FILES
    ${SRC}/agent.cpp ${SRC}/stacktracer.cpp ${SRC}/tickscounter.cpp ${SRC}/agentoptions.cpp ${SRC}/utils.cpp ${SRC}/agentruntime.cpp ${SRC}/javaclassinfo.cpp ${SRC}/javamethodinfo.cpp ${SRC}/javaclassesinfo.cpp ${SRC}/javathreadsinfo.cpp ${SRC}/javathreadinfo.cpp ${SRC}/simplecallcounterprofiler.cpp ${SRC}/abstracttracingprofiler.cpp src/java_crw_demo/java_crw_demo.c ${SRC}/threadcallstackprofiler.cpp)


##########################################################
# Compiler Options

set(GLOBAL_WARNINGS "-W -Wformat-security -Wno-char-subscripts -Wno-parentheses")
set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wno-write-strings -Wno-unused -Wl,--no-undefined")

set(GLOBAL_COPTS "-fno-strict-aliasing -fPIC -fno-omit-frame-pointer -fdiagnostics-show-option -O3")
set(GLOBAL_COPTS "${GLOBAL_COPTS} -funsigned-char -fno-asynchronous-unwind-tables -msse2 -D__STDC_FORMAT_MACROS")

set(CMAKE_CXX_FLAGS "-static-libgcc -static-libstdc++ ${CMAKE_CXX_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}  -pthread -std=c++14")
set(CMAKE_C_FLAGS "-static-libgcc -static-libstdc++ ${CMAKE_C_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}")

set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS}")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS}")

##########################################################
# Build
add_library(${OUTPUT} SHARED ${SOURCE_FILES})
target_link_libraries(${OUTPUT} ${JNI_LIBRARIES} ${Boost_LIBRARIES})
