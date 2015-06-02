#-------------------------------------------------
#
# Project created by QtCreator 2015-06-02T10:26:42
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = PQE
CONFIG   += console
CONFIG   -= app_bundle

CONFIG += link_pkgconfig
CONFIG += c++11

TEMPLATE = app

PKGCONFIG += scew smartslog ckpi

SOURCES += main.cpp \
    pqe.cpp \
    generatorsubscriptionhandler.cpp

SOURCES += ontology/ontology.c

HEADERS += \
    pqe.h \
    generatorsubscriptionhandler.h \
    cfunction.hpp

