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

See more from Commands folder

### Preparation
You need export Scala and Java projects as a jar using maven. Also, import a Zeppelin json file into your Zeppelin Notebook.
```
$ cd BDTwitrend
$ mvn clean install

rename your jar file to BDTwitrendConsumer.fatjar
'''

```
$ cd BDTwitrendProducer
$ mvn clean install

rename your jar file to BDTwitrendProducer.fatjar
'''

- Import a json file from ApacheZeppelin folder into your Zeppelin Notebook
 

### Get the data

Under the project directory, run the getData.py script:
```
$ python Codes/getData.py
```
This script will use twitter real-time streaming API to get real-time tweets, the filter condition is set to "AI". It will proceed the tweets into kafka while writing to a local .json file.

If you're offline, you also can use a tweets file to generate data in Kafka. For example, we already have a .json file in Data directory as tweets_AI.json, use the order to push the data to Kafka:
```
$ cat Data/tweets_AI.json | $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic tweets
```
### The main programe
The main program sparkKafka.py need to initialize by spark:
```
$  $SPARK_HOME/spark-submit --packages org.apache.spark:spark-streaming-kafka-0–8_2.11:1.6.0 Codes/sparkKafka.py
```
When it's running, it gets data from kafka and parse data to count hashtags, you will see some simple result in the terminal window. Since the spark streaming batch interval is set to 10s, the interval analysis result is stored to hive for further analysis. In the mean time, we extract data from hive and proceed further analysis using spark SQL, then visualizing the final result by Plotly. In our main program, the spark SQL will analysis the history data, and update the data visualization every 10s with the latest data.

### Virtualization Results
DataVirtual\file_analysis_output
