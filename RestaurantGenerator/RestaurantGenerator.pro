#-------------------------------------------------
#
# Project created by QtCreator 2015-05-28T13:14:14
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = RestaurantGenerator

CONFIG   += console
CONFIG   -= app_bundle

CONFIG += link_pkgconfig
CONFIG += c++11

TEMPLATE = app

PKGCONFIG += scew smartslog ckpi CAPTGenerator

SOURCES += main.cpp \
    restorauntgenerator.cpp

SOURCES += ontology/ontology.c

HEADERS += \
    restorauntgenerator.h
