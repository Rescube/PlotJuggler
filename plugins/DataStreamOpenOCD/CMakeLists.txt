cmake_minimum_required(VERSION 3.16)

include_directories(../)

add_definitions(${QT_DEFINITIONS})
add_definitions(-DQT_PLUGIN)

QT5_WRAP_UI ( UI_SRC  datastream_openocd.ui  )

SET( SRC datastream_openocd.cpp )

add_library(DataStreamOpenOCD SHARED ${SRC} ${UI_SRC}  )

target_link_libraries(DataStreamOpenOCD
    ${Qt5Widgets_LIBRARIES}
    plotjuggler_plugin_base
    )


if(COMPILING_WITH_CATKIN)
    install(TARGETS DataStreamOpenOCD
        ARCHIVE DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION}
        LIBRARY DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION}
        RUNTIME DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION} )
elseif(COMPILING_WITH_AMENT)
    install(TARGETS DataStreamOpenOCD
            DESTINATION  lib/${PROJECT_NAME} )
else()
    install(TARGETS DataStreamOpenOCD DESTINATION bin  )
endif()


