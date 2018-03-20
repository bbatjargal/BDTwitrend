package edu.mum.bdt
import org.apache.spark.SparkConf
import org.apache.log4j.{Level, Logger}

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions


import org.apache.spark.sql.execution.datasources.hbase._
import org.apache.spark.sql._
import org.apache.spark.sql.SparkSession


import java.util.UUID
import kafka.serializer.StringDecoder

import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf


import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{ HBaseAdmin, Put, HTable } 
import org.apache.hadoop.hbase.{ HBaseConfiguration, HTableDescriptor, HColumnDescriptor }

object BDTwitrendConsumer {
  def main(args: Array[String]) = {

    if (args.length != 2) {
      System.err.println(s"""
        |Usage: BDTwitrendConsumer <brokers> <topics>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topics> is a list of one or more kafka topics to consume from
        |
        """.stripMargin)
      System.exit(1)
    }
    
        
    val Array(brokers, topics) = args
    
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount").setMaster("local[2]").set("spark.executor.memory","1g");
    val ssc = new StreamingContext(sparkConf, Seconds(6))
    
    //val sc = ssc.sparkContext
    //sc.setLogLevel("ERROR")

    //For hbase
    //val sc = new SparkContext(sparkConf)
    
    try{
      /*val spark = SparkSession
                    .builder()
                    .appName("Spark SQL basic example")
                    .config("spark.some.config.option", "some-value")
                    .getOrCreate()
                    
      import spark.implicits._ */

      
      //var hashTagCounts = loadHashTagFromHBase(spark)

      // Create direct kafka stream with brokers and topics
      val topicsSet = topics.split(",").toSet
      
      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
      
      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, topicsSet)
  
      // Get the lines, split them into words, count the words and print
      //val words = messages.map(_._2).flatMap(_.split(" "))
      //val wordCounts = messages.map(x => (x, 1)).reduceByKey(_ + _)
      
      //wordCounts.print()
  
      //hashTagCounts.
      
      //HBASE
      val updateFunc = (values: Seq[Int], state: Option[Int]) => { 
        val currentCount = values.sum       
        val previousCount = state.getOrElse(0)         
        val updatedSum = currentCount+previousCount         
        Some(updatedSum)       
      }
      
      
      val columnFamily = "Word"
    
      //Defining a check point directory for performing stateful operations
 
      ssc.checkpoint("hdfs://localhost:9000/WordCount_checkpoint") 
      //val cnt = wordCounts.updateStateByKey(updateFunc)  //.map(x => (x, 1)).reduceByKey(_ + _)


      val Hbase_inset = messages.foreachRDD(rdd => if (!rdd.isEmpty()) rdd.foreach(toHBase(_)))
      
      // Start the computation
      ssc.start()
    

      ssc.awaitTermination()
    
      
    } finally {
    }
    
  }
  
  def toHBase(row: (_, _)) { 

    val delimiter= "º¿"
    val hConf = new HBaseConfiguration() 
    //hConf.set("hbase.zookeeper.quorum", "localhost:2181") 
    val hTable = new HTable(hConf, "tblTweet") 
    
    //hTable.getScanner(x$1)
    //val tableDescription = new HTableDescriptor(tableName) 
    //tableDescription.addFamily(new HColumnDescriptor("Details".getBytes()))
     
    val data = row._2.toString()
    val datas = data.split(delimiter)
    
    val hastags = datas(0)
    val text = datas(1)
    val latitude = datas(2)
    val longitude = datas(3)
    val createdAt = datas(4)
    val user = datas(5)
    val receivedAt = datas(6)
				
    val thePut = new Put(Bytes.toBytes(row._1.toString())) 
    thePut.add(Bytes.toBytes("Tweet"), Bytes.toBytes("Text"), Bytes.toBytes(text)) 
    thePut.add(Bytes.toBytes("Tweet"), Bytes.toBytes("Hastags"), Bytes.toBytes(hastags)) 
    thePut.add(Bytes.toBytes("Tweet"), Bytes.toBytes("Created"), Bytes.toBytes(createdAt)) 
    thePut.add(Bytes.toBytes("Tweet"), Bytes.toBytes("User"), Bytes.toBytes(user)) 
    thePut.add(Bytes.toBytes("Tweet"), Bytes.toBytes("ReceivedAt"), Bytes.toBytes(receivedAt)) 
    thePut.add(Bytes.toBytes("Geo"), Bytes.toBytes("Latitude"), Bytes.toBytes(latitude)) 
    thePut.add(Bytes.toBytes("Geo"), Bytes.toBytes("Longitude"), Bytes.toBytes(longitude)) 
    
    
    hTable.put(thePut)
    
    
    val arrTags = hastags.split(" ")
    
    val tblHashTag = new HTable(hConf, "tblHashTag") 
    for( tag <- arrTags){
      if(tag != "" && tag != "null") {
        val putHashTag = new Put(Bytes.toBytes(UUID.randomUUID().toString())) 
        putHashTag.add(Bytes.toBytes("Info"), Bytes.toBytes("HashTag"), Bytes.toBytes(tag)) 
        putHashTag.add(Bytes.toBytes("Info"), Bytes.toBytes("Created"), Bytes.toBytes(createdAt))  
        putHashTag.add(Bytes.toBytes("Info"), Bytes.toBytes("ReceivedAt"), Bytes.toBytes(receivedAt))  
        tblHashTag.put(putHashTag)      
      }
    }
    
  }
  
}

/*
 * 
 * 
  
  def loadHashTagFromHBase(sqlContext: SparkSession) : DataFrame = {

    def catalog = s"""{
      |"table":{"namespace":"default", "name":"tblTweetHashTag"},
      |"rowkey":"key",
      |"columns":{
        |"col0":{"cf":"rowkey", "col":"key", "type":"string"},
        |"col1":{"cf":"HashTagCount", "col":"HashTag", "type":"string"},
        |"col2":{"cf":"HashTagCount", "col":"Count", "type":"string"}
      |}
    |}""".stripMargin
    
    def withCatalog(cat: String): DataFrame = {
      sqlContext
      .read
      .options(Map(HBaseTableCatalog.tableCatalog->cat))
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .load()
    }
    
    val df = withCatalog(catalog)
    //val s = df.select("col1", "col2")
    df.registerTempTable("tblTweetHashTag")
    //return 
    return sqlContext.sql("select col0, col2 from tblTweetHashTag") //.show
    //return s
    
  }
     val Array(input, output) = args
    
    //Start the Spark context
    val conf = new SparkConf()
      .setAppName("WordCount")
      .setMaster("local")
    val sc = new SparkContext(conf)

    //Read some example file to a test RDD
    val test = sc.textFile(input)

    test.flatMap { line => //for each line
      line.split(" ") //split the line in word by word.
    }
      .map { word => //for each word
        (word, 1) //Return a key/value tuple, with the word as key and 1 as value
      }
      .reduceByKey(_ + _) //Sum all of the value with same key
      .saveAsTextFile(output) //Save to a text file

    //Stop the Spark context
    sc.stop
  
 * */