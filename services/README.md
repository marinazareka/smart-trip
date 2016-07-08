# smart-trip
Процессоры знаний (KP) для сервиса планирования маршрутов

* geo_db_kp --- KP представления сервиса DBpedia. Использует библиотеки common и geo_common. Логика работы KP описана в библиотеке geo_common.
* geo_names_kp --- KP представления сервиса geonames. Использует библиотеки common и geo_common. Логика работы KP описана в библиотеке geo_common.
* geo_wm_kp --- KP представления сервиса WikiMapia. Использует библиотеки common и geo_common. Логика работы KP описана в библиотеке geo_common.
* time_plan_kp --- KP вычисления временного плана маршрута. Использует результаты работы transport_kp.
* time_review_kp --- KP определения необходимых задержек во время маршрута.
* transport_kp --- KP построения маршрута для заданного набора точек. Содержит Java модуль для работы с библиотекой GraphHopper и C библиотеку-интерфейс с интеллектуальным пространством.
* ranking_distance_kp --- KP ранжирования результатов поисковых запросов.

## Сборка
### Зависимости
* libsmartslog0-dev 0.6.0 (https://build.opensuse.org/package/show/home:seekerk/smartslog)
* smartslog-codegen 0.5 (https://build.opensuse.org/package/show/home:seekerk/smartslog-codegen)

### Сгенерировать онтологию
```bash
cd ontology
./generate-ontology.h
```

### Собрать С KP
```bash
mkdir build
cmake ..
make
```

### Собрать Java KP
```bash
cd transport_kp/TransportKP
./gradlew shadowJar
```

## Настройка
Все KP используют настройки из файла config.ini.
Ожидаемые местоположения файла:
* /etc/smart-trip/
* директория запуска KP.
