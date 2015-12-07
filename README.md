# smart-trip
SmartM3 KPs for tourist route planning

## Сборка
### Зависимости
* JDK 8
* scew (http://www.nongnu.org/scew/)
* ckpi 0.31alpha (http://sourceforge.net/projects/smartslog/files/ANSI-C_KPI/)
* smartslog_dapi (Instruction.zip)

### Сгенерировать онтологию
```bash
cd ontology
mkdir gen
java -jar SmartSlogCodeGen_v0.5.jar -k disabled -m disabled -n ontology -o gen etourism.owl
sed 's/#include <smartslog\/generic.h>/#include <smartslog.h>/' -i gen/ontology.h
```

### Собрать С-проект
```bash
mkdir build
cmake ..
make
```

### Собрать Java-kp
```bash
cd TransportKP
mkdir -p src/main/resources/linux-x86-64
cp ../build/transport-kp-native/libtransport_kp.so src/main/resources/linux-x86-64
./gradlew shadowJar
```

### Запуск TransportKP
```bash
cd TransportKP/build/libs
java -jar TransportKP-all.jar -f ~/osm/RU-KR.osm.pbf -t /tmp/gh -a 127.0.0.1 -p 10010 -n X
```

Параметры:
* f - файл карты в формате .osm.pbf
* t - рабочая директория для graphhopper'а
* a - ip-адрес SIB'а
* p - порт SIB'а
* n - id smartspace'а

### Работа с TransportKP

Запрос передается в следующем виде:
* Координаты точек хранятся в объектах Location (свойства lat и long)
* Объект Point ссылается на объект Location через свойство hasLocation
* Запрос передается в виде объекта Schedule
* Schedule ссылается на Route через свойство hasRoute
* Route содержит набор точек, для которых требуется найти маршрут (свойство hasPoint)

Результат запроса:
* Отрезки пути сохраняются в виде объектов Movement со свойствами isStartPoint и isEndPoint
* Movement'ы добавляются к Route в виде свойства hasMovement
* Все Movement'ы кроме первого, имеют свойство hasNextMovement, указывающее на следующий отрезок в маршруте
* Первый Movement добавляется к Route в виде свойства hasStartMovement
