cmake_minimum_required(VERSION 3.22.1)
project("guitarfx")

# Download Oboe library
include(FetchContent)
FetchContent_Declare(
        oboe
        GIT_REPOSITORY https://github.com/google/oboe.git
        GIT_TAG 1.8.0
)
set(OBOE_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(OBOE_BUILD_TESTS OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(oboe)

add_library(
        guitarfx
        SHARED
        AudioEngine.cpp
)

find_library(log-lib log)

target_link_libraries(
        guitarfx
        oboe
        ${log-lib}
)
