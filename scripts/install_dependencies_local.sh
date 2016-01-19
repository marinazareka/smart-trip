#!/bin/bash

function die {
    exit 1
}

if [[ $# -ne 1 ]]; then
    echo "Usage $0 <install directory>" 
    exit 1
fi

INSTALL_DIR="$1"
mkdir -p "${INSTALL_DIR}"

mkdir -p build

if [[ ! -f SmartSlog_dapi_0.6.0_src.tar.gz ]]; then
    echo "Place file SmartSlog_dapi_0.6.0_src.tar.gz in current directory"
    exit 1
fi

if [[ ! -f SmartSlogCodeGen_v0.5.jar ]]; then
    echo "Place file SmartSlogCodeGen_v0.5.jar in current directory"
    exit 1
fi

cp SmartSlogCodeGen_v0.5.jar ${INSTALL_DIR}/

if [[ ! -f scew-1.1.7.tar.gz ]]; then
    wget http://savannah.nongnu.org/download/scew/scew-1.1.7.tar.gz
fi


if [[ ! -f ANSI-C_KPI_v0.32alpha.tar.gz ]]; then
    wget http://sourceforge.net/projects/smartslog/files/ANSI-C_KPI/ANSI-C_KPI_v0.32alpha.tar.gz
fi

cd build

tar -xf ../scew-1.1.7.tar.gz
tar -xf ../ANSI-C_KPI_v0.32alpha.tar.gz
tar -xf ../SmartSlog_dapi_0.6.0_src.tar.gz

cat << EOF >> "${INSTALL_DIR}/env.sh"
export PKG_CONFIG_PATH="${INSTALL_DIR}/lib/pkgconfig:$PKG_CONFIG_PATH"
export LD_LIBRARY_PATH="${INSTALL_DIR}/lib:$LD_LIBRARY_PATH"
EOF

source "${INSTALL_DIR}/env.sh"

pushd scew-1.1.7
./configure --prefix="${INSTALL_DIR}" && make && make install || die "Error installing scew"
popd

pushd ANSI-C_KPI_v0.32alpha
./configure --prefix="${INSTALL_DIR}" && make && make install || die "Error installing ckpi"
popd

pushd SmartSlog_dapi_0.6.0_src
patch -p1 < ../../smartslog_cmake.patch
cmake -DCMAKE_INSTALL_PREFIX:PATH="${INSTALL_DIR}" -DWITH_DEMOS=0 && make && make install || die "Error installing smartslog"
popd
