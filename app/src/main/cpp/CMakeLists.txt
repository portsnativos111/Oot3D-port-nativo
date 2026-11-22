cmake_minimum_required(VERSION 3.18.1)
project("oot3d_native")

add_library(oot3d_native SHARED native-lib.cpp)

find_library(log-lib log)
find_library(gles-lib GLESv3)

target_link_libraries(oot3d_native ${log-lib} ${gles-lib})
