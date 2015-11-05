# Сборка

1. Генерация онтологии
```
cd ontology
java -jar SmartSlogCodeGen_v0.42alpha_with_dependencies.jar -k disabled -m disabled -n ontology -o gen ontology.owl
```

2. Генерация jni-биндингов
```
cd SmartTrip
./gradlew runSwig
```