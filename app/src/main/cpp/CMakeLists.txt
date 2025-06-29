
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("archandler")

#bzip2
add_subdirectory(third_party/bzip2)
set(BZIP2_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/third_party/bzip2")
set(BZIP2_LIBRARIES bzip2)
# xz&lama
set(BUILD_SHARED_LIBS OFF)
set(ENABLE_DECODER_LZMA2 ON)
set(ENABLE_DECODER_XZ ON)
set(XZ_BUILD_DECODERS ON)
set(XZ_BUILD_LZMADEC ON)
set(BUILD_TESTING OFF)
set(XZ_BUILD_MINICHECK OFF)
set(XZ_BUILD_EXAMPLES OFF)
set(XZ_BUILD_TOOLS OFF)
add_subdirectory(third_party/xz)
#zlib
add_subdirectory(third_party/zlib)
#libarchive
set(ENABLE_ZLIB ON)
set(ENABLE_BZip2 ON)
set(ENABLE_LZMA ON)
set(ENABLE_ZSTD OFF)
set(ENABLE_TAR ON)
set(ENABLE_CPIO ON)
set(ENABLE_CAT OFF)
set(ENABLE_TEST OFF)
set(ENABLE_EXAMPLES OFF)
add_subdirectory(third_party/libarchive)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native_lib.cpp)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        archive_static
        # List libraries link to the target library
        android
        log)
