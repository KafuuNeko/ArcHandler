cmake_minimum_required(VERSION 3.6)
project(bzip2 C)

add_library(bzip2 STATIC
        blocksort.c
        bzlib.c
        compress.c
        crctable.c
        decompress.c
        huffman.c
        randtable.c
)

target_include_directories(bzip2 PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}
)
