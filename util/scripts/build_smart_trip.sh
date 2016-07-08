#!/bin/bash

function die {
    exit 1
}

if [[ $# -ne 1 ]]; then
    echo "Usage $0 <smart trip directory>"
    exit 1
fi

cd $1

pushd ontology
mkdir gen
smartsloggen.sh -k disabled -m disabled -n ontology -o gen etourism.owl
sed 's/#include <smartslog\/generic.h>/#include <smartslog.h>/' -i gen/ontology.h
popd

mkdir build
pushd build
cmake ..
make
popd

pushd TransportKP
mkdir -p src/main/resources/linux-x86-64
cp ../build/transport-kp-native/libtransport_kp.so src/main/resources/linux-x86-64/libtransport_kp.so
./gradlew shadowJar
popd
