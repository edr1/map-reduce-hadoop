# Componentes Hadoop durante WordCount en EMR

## Participación real

| Componente | Ubicación típica | Papel en WordCount |
|---|---|---|
| Driver/cliente | Primary | Configura el `Job` y lo envía a YARN. |
| ResourceManager | Primary | Administra recursos y asigna containers. |
| MapReduce ApplicationMaster | Container YARN | Coordina mapas, reducer, reintentos y progreso. |
| NodeManager | Cada Core | Inicia y supervisa los containers de tareas. |
| InputFormat/RecordReader | Containers Map | Convierte archivos/splits en registros para el Mapper. |
| Mapper | Cores | Emite `(palabra, 1)`. |
| Combiner | Cores | Suma parcialmente antes de enviar por red. Es optimización, no garantía semántica. |
| Partitioner | Salida Map | Decide a qué reducer se dirige cada clave. |
| Shuffle | Reducer/NodeManagers | El reducer obtiene particiones producidas por todos los mappers. |
| Sort/Group | Lado Reduce | Ordena y agrupa pares por palabra. |
| Reducer | Container YARN | Suma los valores de cada palabra. |
| OutputFormat | Container Reduce | Escribe `part-r-*` y `_SUCCESS`. |
| JobHistory Server | Primary | Conserva métricas y detalles de trabajos finalizados. |
| S3A | Todos los nodos que acceden a datos | Conector para leer entrada y escribir salida en S3. |

## Presentes, pero no utilizados por este comando

- **NameNode/DataNodes:** están activos porque HDFS pertenece al bundle Hadoop. El job puede usar almacenamiento temporal local/HDFS, pero los datos persistentes indicados con `s3://` permanecen en S3.
- **Oozie:** solo participaría si el WordCount fuese lanzado mediante un workflow Oozie.
- **Tez:** no interviene en un `hadoop jar` MapReduce. Participaría con Hive/Pig configurado para Tez.
- **Hue:** es una interfaz de usuario; no procesa los registros.

## Flujo resumido

1. El Driver consulta los archivos de entrada mediante S3A.
2. Hadoop crea input splits y el ApplicationMaster solicita containers.
3. Los NodeManagers ejecutan mappers en los Core.
4. Cada mapper emite pares intermedios; el Combiner puede agregarlos localmente.
5. Partitioner, Shuffle y Sort llevan y agrupan todas las claves iguales.
6. El Reducer suma los valores y OutputFormat escribe el resultado en S3.
7. JobHistory Server registra la ejecución para consultarla desde YARN Timeline Server.
