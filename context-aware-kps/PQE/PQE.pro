#-------------------------------------------------
#
# Project created by QtCreator 2015-06-02T10:26:42
#
#-------------------------------------------------

QT       += core
QT       += network

QT       -= gui

TARGET = PQE

CONFIG   += console
CONFIG   -= app_bundle

CONFIG += link_pkgconfig
CONFIG += c++11

TEMPLATE = app

PKGCONFIG += scew smartslog ckpi

INCLUDEPATH += ../CAPTGenerator
LIBS += -L $${OUT_PWD}/../CAPTGenerator -lCAPTGenerator

SOURCES += main.cpp \
    pqe.cpp \
    generatorsubscriptionhandler.cpp \
    captgeneratordesc.cpp \
    pendingrequest.cpp \
    processedrequest.cpp \
    placemark.cpp \
    gets.cpp

SOURCES += ontology/ontology.c

HEADERS += \
    pqe.h \
    generatorsubscriptionhandler.h \
    captgeneratordesc.h \
    pendingrequest.h \
    processedrequest.h \
    placemark.h \
    gets.h

