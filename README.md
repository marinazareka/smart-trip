# smart-trip
SmartM3 KPs for tourist route planning
## Сборка
###Зависимости
JDK 8
scew (http://www.nongnu.org/scew/)
ckpi 0.31alpha (http://sourceforge.net/projects/smartslog/files/ANSI-C_KPI/)
smartslog_dapi (Instruction.zip)

###Сгенерировать онтологию
```bash
cd ontology
mkdir gen
java -jar SmartSlogCodeGen_v0.5.jar -k disabled -m disabled -n ontology -o gen etourism.owl
sed 's/#include <smartslog\/generic.h>/#include <smartslog.h>/' -i gen/ontology.h
```
###Собрать С-проект
```bash
mkdir build
cmake ..
make
```

####Собрать Java-kp
```bash
cd TransportKP
mkdir -p src/main/resources/linux-x86-64
cp ../build/transport-kp-native/libtransport_kp.so src/main/resources/linux-x86-64
./gradlew shadowJar
```
TransportKP-all.jar в каталоге build/libs
