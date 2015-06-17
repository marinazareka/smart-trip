#-------------------------------------------------
#
# Project created by QtCreator 2015-05-27T15:08:10
#
#-------------------------------------------------

QT       += core
QT       -= gui

TARGET = restaurant-user-kp

CONFIG   += console
CONFIG   -= app_bundle

CONFIG += link_pkgconfig
CONFIG += c++11

TEMPLATE = app

PKGCONFIG += scew smartslog ckpi

SOURCES += main.cpp
SOURCES += ontology/ontology.c

