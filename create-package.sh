PACKAGENAME=smart-trip
VERSION=0.1.3

rm -rf ../$PACKAGENAME-$VERSION
mkdir ../$PACKAGENAME-$VERSION
cp -r * ../$PACKAGENAME-$VERSION
cd ../$PACKAGENAME-$VERSION
rm -rf build nbproject ontology/gen/* SmartTripAndroid services/transport_kp/TransportKP tests
cd ..
tar -cJf $PACKAGENAME\_$VERSION.orig.tar.xz $PACKAGENAME-$VERSION
cd $PACKAGENAME-$VERSION
dpkg-buildpackage -S
