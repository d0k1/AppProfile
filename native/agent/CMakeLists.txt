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
FIND_PACKAGE( Boost 1.55 COMPONENTS program_options system REQUIRED )

find_package(JNI REQUIRED)
include_directories("src/java_crw_demo")
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})
include_directories( ${Boost_INCLUDE_DIR} )

set(SOURCE_FILES
    ${SRC}/agent.cpp ${SRC}/agentoptions.cpp ${SRC}/utils.cpp ${SRC}/agentruntime.cpp ${SRC}/javaclassinfo.cpp ${SRC}/javamethodinfo.cpp ${SRC}/javaclassesinfo.cpp ${SRC}/javathreadsinfo.cpp ${SRC}/javathreadinfo.cpp ${SRC}/simplecallcounterprofiler.cpp ${SRC}/abstracttracingprofiler.cpp src/java_crw_demo/java_crw_demo.c)


##########################################################
# Compiler Options

set(GLOBAL_WARNINGS "-W -Wformat-security -Wno-char-subscripts -Wno-parentheses")
set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wno-write-strings -Wno-unused -Wl,--no-undefined")
#set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wno-sign-compare -Wno-strict-overflow")
#set(GLOBAL_WARNINGS "${GLOBAL_WARNINGS} -Wnon-virtual-dtor -Woverloaded-virtual")

set(GLOBAL_COPTS "-fno-strict-aliasing -fPIC -fno-omit-frame-pointer -fdiagnostics-show-option  -g3")
set(GLOBAL_COPTS "${GLOBAL_COPTS} -funsigned-char -fno-asynchronous-unwind-tables -msse2 -D__STDC_FORMAT_MACROS")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}  -pthread -std=c++14")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${GLOBAL_WARNINGS} ${GLOBAL_COPTS}")

##########################################################
# Build
set(STATIC_LIBS "libgcc libstdc++")
set(CMAKE_EXE_LINKER_FLAGS "-static ${STATIC_LIBS}") 
add_library(${OUTPUT} SHARED ${SOURCE_FILES})
target_link_libraries(${OUTPUT} ${JNI_LIBRARIES} ${Boost_LIBRARIES} c stdc++ dl)
