# Makefile

ONTOLOGY_SOURCE := ontology/etourism.c
ONTOLOGY_HEADER := ontology/etourism.h
ONTOLOGY_OBJECT := ontology/etourism.o

pkg_packages := scew smartslog ckpi

PKG_CFLAGS  := $(shell pkg-config --cflags $(pkg_packages))
PKG_LDFLAGS := $(shell pkg-config --libs $(pkg_packages))

INCLUDES := 
LIBS := 

CC    := gcc

DEBUG := -g
EXTRA_FLAGS := 
EXTRA_OPTION :=
		  
CFLAGS := -c $(DEBUG) $(INCLUDES) $(PKG_CFLAGS) $(EXTRA_FLAGS)  
LFLAGS := $(DEBUG) $(PKG_LDFLAGS) $(LIBS)

PROGRAM_NAME := dbpedia_sparql

targets = main

o_files :=  ./dbpedia_sparql.o \
		    $(ONTOLOGY_OBJECT) 

.PHONY: all
all : $(targets)

main: $(o_files)
	$(CC) $(o_files) -o $(PROGRAM_NAME) $(EXTRA_OPTION) $(LFLAGS)

.c.o:
	$(CC) $(EXTRA_OPTION) $(CFLAGS) -o $@ $<


.PHONY: clean
clean:
	rm -f *.o
	rm -f $(PROGRAM_NAME)

