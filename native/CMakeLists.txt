cmake_minimum_required(VERSION 3.22)
project(ffkit CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -ffunction-sections -fdata-sections -Os")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections -Wl,--strip-all")

add_library(ffkit SHARED
    esp.cpp
    aim.cpp
    features.cpp
    worker.cpp
    mask.cpp
    entry.cpp
)

target_link_libraries(ffkit log android)
