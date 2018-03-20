# BDTwitrend
The class project of Big Data Technology (CS523) course in March 2018 at MUM.

## Describe
Reading stream data from Twitter and saving it to HBase database using Kafka and Spark Stream. Reading from HBase database using Spark, proceed some simple analysis using SparkSQL. Finally, illustrating charts and showing simple a Real-Time dashboard.

## Environment

All the development work is done on Ubuntu 17.10. I use Hadoop, YARN, Zookeeper, Kafka, Spark, Spark Streaming, Spark SQL, HBase, Zeppelin and D3.js. All applications are running under pseudo mode. The main program is written by Scala and Java.

### Documentations I used
- http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html
- https://hbase.apache.org/book.html#quickstart
- https://kafka.apache.org/quickstart
- https://spark.apache.org/downloads.html
- http://zeppelin.apache.org/docs/0.7.3/install/install.html
- https://bl.ocks.org/mbostock/3883245

### Requirments
- Ubuntu 17.10
- Hadoop 3.0.0
- Hbase 1.2.6
- Spark 2.1.0
- Kafka 1.0.1
- Spark-streaming-kafka_2.10
- Scala 2.12 - 2.10
- Twitter4j - 4.0.4
- D3JS - v3
- Zeppelin - 0.7.3
- Hortonworks-SHC 1.1.2-2.2-s_2.11-SNAPSHOT

## How it works
Firstly, you need to be sure all the components are correctly installed and started.

- Hadoop (dfs)
- YARN
- HBase
- Zookeeper for Kafka
- Kafka server
- Zeppelin

See more from Commands directory

### Preparation
You need export Scala and Java projects as a jar file using maven. Also, import a Zeppelin json file into your Zeppelin Notebook.

- Export Projects as jars
```
$ cd BDTwitrend
$ mvn clean install

rename your jar file to BDTwitrendConsumer.jar
```

```
$ cd BDTwitrendProducer
$ mvn clean install

rename your jar file to BDTwitrendProducer.jar
```

- Import a json file from ApacheZeppelin directory into your Zeppelin Notebook
In order to run we have to add the following Interpreters
```
- md
- Angular
- HBase
- spark with the following dependencies:
  - /usr/local/hbase/lib/hbase-client-1.2.6.jar 	
  - /usr/local/hbase/lib/hbase-common-1.2.6.jar 	
  - /usr/local/hbase/lib/hbase-protocol-1.2.6.jar 	
  - /usr/local/hbase/lib/hbase-server-1.2.6.jar 	
  - /usr/local/hbase/lib/metrics-core-2.2.0.jar 	
  - /usr/local/hbase/lib/shc-core-1.1.2-2.2-s_2.11-SNAPSHOT.jar 	
  - org.apache.spark:spark-streaming-kafka-0-10_2.11:2.1.0 	
  - org.apache.kafka:kafka-clients:0.10.1.1 	
  - org.apache.kafka:kafka_2.11:0.10.1.1 	
  - org.apache.spark:spark-sql-kafka-0-10_2.11:2.1.0 	
  - /usr/local/thirdpartylibs/spark-highcharts-0.6.5.jar 	
  - /usr/local/thirdpartylibs/lift-json_2.11-2.6.3.jar
```

zeppelin
hbase dependencies
/usr/local/hbase/lib/hbase-client-1.2.6.jar
/usr/local/hbase/lib/hbase-protocol-1.2.6.jar
/usr/local/hbase/lib/hbase-common-1.2.6.jar

spark dependencies

/usr/local/hbase/lib/hbase-client-1.2.6.jar
/usr/local/hbase/lib/hbase-common-1.2.6.jar
/usr/local/hbase/lib/hbase-protocol-1.2.6.jar
/usr/local/hbase/lib/hbase-server-1.2.6.jar
/usr/local/hbase/lib/metrics-core-2.2.0.jar
/usr/local/hbase/lib/shc-core-1.1.2-2.2-s_2.11-SNAPSHOT.jar
org.apache.spark:spark-streaming-kafka-0-10_2.11:2.1.0
org.apache.kafka:kafka-clients:0.10.1.1
org.apache.kafka:kafka_2.11:0.10.1.1  

### Get the data

Run a Kafka producer using 8.BDTwitrendProducer.sh script in Commands directory:
```
$ ./"8.BDTwitrendProducer.sh"

BDTwitrendProducer.jar is required in a directory where the base runs
```
This script will use twitter streaming API to get real-time tweets. It will feed the tweets into Kafka consumers.

### The main Consumer programs

- Saving the tweet into HBase database
```
$  ./"9.BDTwitrendConsumer.sh"

BDTwitrendConsumer.jar is required in a directory where the base runs

```
When it's running, it gets data from a Kafka producer and parse and save date into HBase database.


#### Vizualization Results

I use Apache Zeppelin notebook to visualize the data. There are two parts: Real time and regular charts which we have to run manually

- Regular charts

At first, When it's running, it gets data from a Kafka producer and parse and save date into HBase database.

- Real time charts
