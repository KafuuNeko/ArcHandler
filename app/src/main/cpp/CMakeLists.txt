cmake_minimum_required(VERSION 3.22.1)

project("archandler")

# 使用静态链接
set(BUILD_SHARED_LIBS OFF)

# libiconv & libcharset
set(ICONV_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/third_party/libiconv/include" CACHE PATH "iconv include" FORCE)
set(LIBICONV_PATH "${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI}/libiconv.a" CACHE FILEPATH "iconv static lib" FORCE)
set(LIBCHARSET_PATH "${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI}/libcharset.a" CACHE FILEPATH "iconv static lib" FORCE)
set(LIBICONV_INCLUDE_DIR "${ICONV_INCLUDE_DIR}")
set(LIBICONV_LIBRARIES "${LIBICONV_PATH}" "${LIBCHARSET_PATH}")
if (NOT TARGET libiconv::libiconv)
    add_library(libiconv::libiconv STATIC IMPORTED)
    set_target_properties(libiconv::libiconv PROPERTIES
            IMPORTED_LOCATION "${LIBICONV_PATH}"
            INTERFACE_INCLUDE_DIRECTORIES "${ICONV_INCLUDE_DIR}"
    )
endif()
if (NOT TARGET libiconv::libcharset)
    add_library(libiconv::libcharset STATIC IMPORTED)
    set_target_properties(libiconv::libcharset PROPERTIES
            IMPORTED_LOCATION "${LIBCHARSET_PATH}"
            INTERFACE_INCLUDE_DIRECTORIES "${ICONV_INCLUDE_DIR}"
    )
endif()

# bzip2
add_subdirectory(third_party/bzip2)
set(BZIP2_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/third_party/bzip2")
set(BZIP2_LIBRARIES bzip2)

#lz4
add_subdirectory(third_party/lz4/build/cmake)
set(LZ4_FOUND TRUE CACHE BOOL "lz4 found by top-level" FORCE)
set(LZ4_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/third_party/lz4/lib")
set(LZ4_LIBRARY lz4)

# zstd
set(ZSTD_BUILD_TESTS OFF)
add_subdirectory(third_party/zstd/build/cmake)
set(ZSTD_FOUND TRUE CACHE BOOL "zstd found by top-level" FORCE)
set(ZSTD_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/third_party/zstd/lib")
set(ZSTD_LIBRARY "${CMAKE_BINARY_DIR}/third_party/zstd/build/cmake/lib/libzstd.a")

# xz&lama
set(ENABLE_DECODER_LZMA2 ON)
set(ENABLE_DECODER_XZ ON)
set(XZ_BUILD_DECODERS ON)
set(XZ_BUILD_LZMADEC ON)
set(BUILD_TESTING OFF)
set(XZ_BUILD_MINICHECK OFF)
set(XZ_BUILD_EXAMPLES OFF)
set(XZ_BUILD_TOOLS OFF)
add_subdirectory(third_party/xz)
# 设置LIBLZMA模块变量
set(_xz_lib "${CMAKE_BINARY_DIR}/third_party/xz/liblzma.a")
set(_xz_inc "${CMAKE_SOURCE_DIR}/third_party/xz/src/liblzma/api")
set(LIBLZMA_LIBRARY_DEBUG  "${_xz_lib}" CACHE FILEPATH "liblzma debug lib" FORCE)
set(LIBLZMA_LIBRARY_RELEASE "${_xz_lib}" CACHE FILEPATH "liblzma release lib" FORCE)
set(LIBLZMA_INCLUDE_DIR    "${_xz_inc}" CACHE PATH "liblzma includes" FORCE)
set(LIBLZMA_FOUND TRUE CACHE BOOL "liblzma found by top-level" FORCE)
set(LIBLZMA_LIBRARIES "${_xz_lib}" CACHE INTERNAL "liblzma library (forced)" FORCE)
# 创建一个 modern CMake IMPORTED target
if (NOT TARGET LibLZMA::LibLZMA)
    add_library(LibLZMA::LibLZMA STATIC IMPORTED)
    set_target_properties(LibLZMA::LibLZMA PROPERTIES
            IMPORTED_LOCATION "${_xz_lib}"
            INTERFACE_INCLUDE_DIRECTORIES "${_xz_inc}"
    )
    message(WARNING "Top-level: created IMPORTED target LibLZMA::LibLZMA -> ${_xz_lib}")
endif()

# zlib
add_subdirectory(third_party/zlib)

# libarchive
set(ENABLE_ICONV ON)
set(ENABLE_ZLIB ON)
set(ENABLE_BZip2 ON)
set(ENABLE_LZ4 ON)
set(ENABLE_LZMA ON)
set(ENABLE_ZSTD ON)
set(ENABLE_TAR ON)
set(ENABLE_CPIO ON)
set(ENABLE_CAT OFF)
set(ENABLE_TEST OFF)
set(ENABLE_EXAMPLES OFF)
add_subdirectory(third_party/libarchive)
target_include_directories(archive_static
        PRIVATE ${LIBLZMA_INCLUDE_DIR}
        PUBLIC third_party/libarchive
)

# native
add_library(${CMAKE_PROJECT_NAME} SHARED
        src/archive_builder.cc
        src/archive_extractor.cc
        src/native_lib.cc
)
target_link_libraries(${CMAKE_PROJECT_NAME}
        archive_static
        android
        log
)
target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC ${PROJECT_SOURCE_DIR})