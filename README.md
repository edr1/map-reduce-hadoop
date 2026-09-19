# WordCount de frutas — código propio

Proyecto Java/Maven compatible con Hadoop 3.4.2 y Amazon EMR 7.14.0.

## Componentes implementados

- `FruitMapper`: normaliza a minúsculas, elimina puntuación y emite `(palabra, 1)`.
- `SumReducer`: suma todos los valores de una palabra.
- `SumReducer` como Combiner: reduce tráfico antes del Shuffle.
- `WordCountJob`: configura y envía el trabajo a YARN.

## Compilar en el nodo Primary

```bash
cd hadoop-wordcount-propio
mvn clean package
```

El JAR se genera en:

```text
target/wordcount-frutas-1.0.0.jar
```

## Ejecutar en EMR con S3

La ruta de salida no debe existir previamente.

```bash
hadoop jar target/wordcount-frutas-1.0.0.jar \
  s3://erick-wordcount-893804123750/input \
  s3://erick-wordcount-893804123750/output-propio
```

## Leer el resultado

```bash
hdfs dfs -cat "s3://erick-wordcount-893804123750/output-propio/part-r-*"
```

## Volver a ejecutar

Primero elimina exclusivamente la salida anterior:

```bash
hdfs dfs -rm -r s3://erick-wordcount-893804123750/output-propio
```

Luego repite el comando `hadoop jar`.

## Resultado esperado

```text
apple       11
banana       9
grape        6
mango        6
orange       6
papaya       4
pear         4
pineapple    4
```
