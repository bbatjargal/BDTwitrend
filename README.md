```
GitHub: https://github.com/bbatjargal/BDTwitrend
```

# BDTwitrend
The class project of Big Data Technology (CS523) course in March 2018 at MUM.

## Describe

```
Project Parts Statements
Part 1. Create your own project for Spark Streaming.
Part 2. Create your own project using Spark SQL and Hbase/Hive together.
Part 3. In any of the parts 1 and 2 above, show the proper use of any of the data visualization tools like Tableau, Jupyter, Plotly, etc.
Part 4. Do some research and create a simple demo project for any one of the following tools: Presto, Impala, Phoenix, Storm, Kafka
```

I tried to combine all parts into a single project. My idea is that find out trending hash tags from Twitter data.

Main operations are that reading stream data from Twitter and saving it to HBase database using Kafka and Spark Stream.  Reading from HBase database using Spark, proceed some simple analysis using SparkSQL. Finally, illustrating regular charts and showing real time charts in a dashboard.

## Environment

All the development work is done on Ubuntu 17.10. I use Hadoop, YARN, Zookeeper, Kafka, Spark, Spark Streaming, Spark SQL, HBase, Zeppelin and D3.js. All applications are running under pseudo mode. The main program is written by Scala and Java.

### Documentations I used
- http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html
- https://hbase.apache.org/book.html#quickstart
- https://kafka.apache.org/quickstart
- https://spark.apache.org/downloads.html
- http://zeppelin.apache.org/docs/0.7.3/install/install.html
- https://bl.ocks.org/mbostock/3883245
- http://twitter4j.org/
- http://www.scala-lang.org/

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
- ScalaIDE - 4.7.0

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
You need export Scala and Java projects as a jar file using maven. Also, creating datatables and importing a Zeppelin json file into your Zeppelin Notebook.

- Export Projects as jars
```
$ cd BDTwitrend/
$ mvn clean install

rename your jar file to BDTwitrendConsumer.jar
```

```
$ cd BDTwitrendProducer/
$ mvn clean install

rename your jar file to BDTwitrendProducer.jar
```

- Create datatables on HBase database

There are two table 'tblTweet' and 'tblHashTag'.

```
$ hbase-shell
hbase> create 'tblTweet', 'Tweet', 'Geo'
hbase> create 'tblHashTag', 'Info'

```


- Import a json file from ApacheZeppelin directory into your Zeppelin Notebook

In order to run, we have to add the following Interpreters.

```
- md
- Angular
- HBase with the following dependencies:
  - /usr/local/hbase/lib/hbase-client-1.2.6.jar 	
  - /usr/local/hbase/lib/hbase-protocol-1.2.6.jar 	
  - /usr/local/hbase/lib/hbase-common-1.2.6.jar

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

### Get data

Run a Kafka producer using 8.BDTwitrendProducer.sh script in Commands directory. First, we need define twitter consumer key, secret, access token and access token secret. Read more information from http://docs.inboundnow.com/guide/create-twitter-application/.

```
$ spark-submit --class "edu.mum.bdt.java.BDTwitrendProducer" --master local[2] BDTwitrendProducer.jar <twitter-consumer-key> <twitter-consumer-secret> <twitter-access-token> <twitter-access-token-secret>  topic-bdtwitrend

or simply edit "8.BDTwitrendProducer.sh" bash file and run.

$ ./"8.BDTwitrendProducer.sh"

BDTwitrendProducer.jar is required in a directory where the base runs
```
This script will use twitter streaming API to get real-time tweets. It will feed the tweets into Kafka consumers.


### The main consumer

Saving the tweet into HBase database

```
$  ./"9.BDTwitrendConsumer.sh"

BDTwitrendConsumer.jar is required in a directory where the base runs

```
When it's running, it gets data from a Kafka producer and parse and save date into HBase database.


### Visualization

I use Apache Zeppelin notebook to visualize the data. There are two parts: Real time and regular charts which we have to run manually

- Regular charts

At first, a section named "4. Load data into table (Spark/HBase/DataFrames)" is needed to run. After finished, feel free to run sections which are below "4. Load data into table (Spark/HBase/DataFrames)", such as "Number of Tweets and Users", "Top 10 topics", "Most active 10 users" etc.

- Real time charts

At first, a section named "2. Kafka Consumer for real-time dashboard" is needed to run. After finished, run a section named "1. Real-Time Dashboard". Then you can see some real time charts such as "Number of Tweets by received date", "Number of Tweets/Users" and "Latest 10 tweets".


### Verify data
Using hbase-shell, we can see where data stored. Here are some commands we can use.

```
hbase> list
hbase> scan 'tblTweet', { LIMIT => 10 }
hbase> count 'tblTweet'

```

```
hbase> list
hbase> scan 'tblHashTag', { LIMIT => 10 }
hbase> count 'tblHashTag'

```
