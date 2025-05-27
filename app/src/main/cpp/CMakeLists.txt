
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.

cmake_minimum_required(VERSION 3.4.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("gotronome")


# Pull in parselib
set (PARSELIB_DIR ../../../../../oboe/samples/parselib)
#message("PARSELIB_DIR = " + ${PARSELIB_DIR})

# Pull in iolib
set (IOLIB_DIR ../../../../../oboe/samples/iolib)
#message("IOLIB_DIR = " + ${IOLIB_DIR})

# Set the path to the Oboe library directory
set (OBOE_DIR ../../../../../oboe)
#message("OBOE_DIR = " + ${OBOE_DIR})

add_subdirectory(${OBOE_DIR} ./build)

# include folders
include_directories(
        ${OBOE_DIR}/include
        ${CMAKE_CURRENT_LIST_DIR}
)

#include(${PARSELIB_DIR}/src/main/cpp/CMakeLists.txt)
#include(${IOLIB_DIR}/src/main/cpp/CMakeLists.txt)

# Include the WavLib headers and shared sample code
#include_directories(
#        ${PARSELIB_DIR}/src/main/cpp
#        ${IOLIB_DIR}/src/main/cpp)

# App specific sources
set (APP_SOURCES
        MetronomeEngine.cpp
        jni_bridge.cpp
)

# Build the drumthumper (native) library
add_library(gotronome SHARED
        ${APP_SOURCES}
)

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Enable optimization flags: if having problems with source level debugging,
# disable -Ofast ( and debug ), re-enable after done debugging.
target_compile_options(gotronome PRIVATE -Wall -Werror "$<$<CONFIG:RELEASE>:-Ofast>")

target_link_libraries( # Specifies the target library.
        gotronome

        -Wl,--whole-archive
#        iolib
#        parselib
        -Wl,--no-whole-archive

        oboe

        # Links the target library to the log library
        # included in the NDK.
        log
        ${log-lib})
target_link_options(gotronome PRIVATE "-Wl,-z,max-page-size=16384")

