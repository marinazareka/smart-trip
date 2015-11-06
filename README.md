# smart-trip
SmartM3 KPs for tourist route planning
## Сборка
###Сгенерировать онтологию
```bash
cd ontology
mkdir gen
java -jar SmartSlogCodeGen_v0.5.jar -k disabled -m disabled -n ontology -o gen ontology.owl
sed 's/#include <smartslog\/generic.h>/#include <smartslog.h>/' -i gen/ontology.h
```
###Собрать С-проект
mkdir build
cmake ..
make

####Собрать Java-kp
TODO:
