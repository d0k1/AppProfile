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
set(OUTPUT "lagent")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${BIN})

##########################################################
# Libraries & Code

find_package(PkgConfig REQUIRED)

find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})

# include of gcc 4.8 headers specifically to work around
# https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=729933
# equivalent in ubuntu 13.10
include_directories("/usr/include/i386-linux-gnu/c++/4.8/")

set(SOURCE_FILES
    ${SRC}/agent.cpp)


##########################################################
# Compiler Options

set(GLOBAL_WARNINGS "-Wall -Wformat-security -Wno-char-subscripts")

set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wno-sign-compare -Wno-strict-overflow -Wwrite-strings")
set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wnon-virtual-dtor -Woverloaded-virtual")

set(GLOBAL_COPTS "-fdiagnostics-show-option -fno-omit-frame-pointer -fno-strict-aliasing")
set(GLOBAL_COPTS "${GLOBAL_COPTS} -funsigned-char -fno-asynchronous-unwind-tables -msse2 -g -D__STDC_FORMAT_MACROS -O2")

    message("-- using GCC")
    set(GLOBAL_COPTS "${GLOBAL_COPTS} -mfpmath=sse -fno-tree-loop-distribute-patterns")
    set(GLOBAL_WARNINGS, "${GLOBAL_WARNINGS} -Wframe-larger-than=16384 -Wno-unused-but-set-variable")
    set(GLOBAL_WARNINGS, "${GLOBAL_WARNINGS} -Wunused-but-set-parameter -Wvla -Wno-conversion-null")
    set(GLOBAL_WARNINGS, "${GLOBAL_WARNINGS} -Wno-unknown-pragmas -Wno-builtin-macro-redefined -Wl,-fatal_warnings")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS} -pthread -std=c++14")

##########################################################
# Build

add_library(${OUTPUT} SHARED ${SOURCE_FILES})
target_link_libraries(${OUTPUT} ${JNI_LIBRARIES} dl)

