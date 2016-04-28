PACKAGENAME=transport-kp
VERSION=0.1.1

rm -rf ../../$PACKAGENAME-$VERSION
mkdir ../../$PACKAGENAME-$VERSION
cp -r * ../../$PACKAGENAME-$VERSION
cd ../../$PACKAGENAME-$VERSION
./gradlew shadowJar
cd ..
tar -cJf $PACKAGENAME\_$VERSION.orig.tar.xz $PACKAGENAME-$VERSION
cd $PACKAGENAME-$VERSION
dpkg-buildpackage -S
