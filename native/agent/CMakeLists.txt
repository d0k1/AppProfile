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
set(OUTPUT "agent")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${BIN})

#set(CMAKE_VERBOSE_MAKEFILE "ON")
##########################################################
# Libraries & Code

find_package(PkgConfig REQUIRED)

find_package(JNI REQUIRED)
include_directories("src/java_crw_demo src/agent_util")
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})

# include of gcc 4.8 headers specifically to work around
# https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=729933
# equivalent in ubuntu 13.10
#include_directories("/usr/include/i386-linux-gnu/c++/4.8/")

set(SOURCE_FILES
    ${SRC}/agent.cpp src/agent_util/agent_util.c src/java_crw_demo/java_crw_demo.c)


##########################################################
# Compiler Options

set(GLOBAL_COPTS "-fno-strict-aliasing -fPIC -fno-omit-frame-pointer -W -Wall  -Wno-unused -Wno-parentheses -g3 ")
#set(GLOBAL_COPTS "${GLOBAL_COPTS} -msse2 -g3 -D__STDC_FORMAT_MACROS")
#set(GLOBAL_WARNINGS, "${GLOBAL_WARNINGS} -W -Wall  -Wno-unused -Wno-parentheses -DDEBUG")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS} -pthread -std=c++14")

##########################################################
# Build
set(STATIC_LIBS "libgcc libstdc++")
set(CMAKE_EXE_LINKER_FLAGS "-static ${STATIC_LIBS}") 
add_library(${OUTPUT} SHARED ${SOURCE_FILES})
target_link_libraries(${OUTPUT} ${JNI_LIBRARIES} c dl)
