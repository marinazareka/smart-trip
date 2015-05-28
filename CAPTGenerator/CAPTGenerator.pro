#-------------------------------------------------
#
# Project created by QtCreator 2015-05-28T11:29:01
#
#-------------------------------------------------

QT       -= gui

CONFIG += link_pkgconfig
CONFIG += c++11

CONFIG += create_prl no_install_prl
CONFIG += create_pc

PKGCONFIG += scew smartslog ckpi


TARGET = CAPTGenerator

TEMPLATE = lib

DEFINES += CAPTGENERATOR_LIBRARY

SOURCES += captgenerator.cpp
SOURCES += ontology/ontology.c



HEADERS += captgenerator.h \
        captgenerator_global.h \
        ontology/ontology.h


headers.files = captgenerator.h captgenerator_global.h

QMAKE_PKGCONFIG_NAME = capt-generator
QMAKE_PKGCONFIG_DESCRIPTION = CAPTGenerator
QMAKE_PKGCONFIG_LIBDIR = $$target.path
QMAKE_PKGCONFIG_INCDIR = $$target.path
QMAKE_PKGCONFIG_DESTDIR = pkgconfig

unix {
    isEmpty(PREFIX) {
        PREFIX = /usr/local
    }

    target.path = $$PREFIX/lib
    headers.path = $$PREFIX/include

    INSTALLS += target headers
}
