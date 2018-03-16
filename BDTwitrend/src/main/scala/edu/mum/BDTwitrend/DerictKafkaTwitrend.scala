package edu.mum.BDTwitrend
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions


import kafka.serializer.StringDecoder

import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf


import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{ HBaseAdmin, Put, HTable } 
import org.apache.hadoop.hbase.{ HBaseConfiguration, HTableDescriptor, HColumnDescriptor }

object DerictKafkaTwitrend {
  def main(args: Array[String]) = {

    if (args.length != 2) {
      System.err.println(s"""
        |Usage: DerictKafkaTwitrend <brokers> <topics>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topics> is a list of one or more kafka topics to consume from
        |
        """.stripMargin)
      System.exit(1)
    }
        
    val Array(brokers, topics) = args
    
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount").setMaster("local[2]").set("spark.executor.memory","1g");
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    
    //For hbase
    //val sc = new SparkContext(sparkConf)
    
    try{
      

      // Create direct kafka stream with brokers and topics
      val topicsSet = topics.split(",").toSet
      
      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
      
      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, topicsSet)
  
      // Get the lines, split them into words, count the words and print
      val lines = messages.map(_._2)
      val words = lines.flatMap(_.split(" "))
      val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
      wordCounts.print()
  
      
      //HBASE
      val updateFunc = (values: Seq[Int], state: Option[Int]) => { 
        val currentCount = values.foldLeft(0)(_ + _)         
        val previousCount = state.getOrElse(0)         
        val updatedSum = currentCount+previousCount         
        Some(updatedSum)       
      }
      
      
      val tableName = "WordCount"
      val columnFamily = "Word"
    
      //Defining a check point directory for performing stateful operations
 
      ssc.checkpoint("hdfs://localhost:9000/WordCount_checkpoint") 
      val cnt = wordCounts.map(x => (x, 1)).reduceByKey(_ + _).updateStateByKey(updateFunc) 
      def toHBase(row: (_, _)) { 
      
        val hConf = new HBaseConfiguration() 
        //hConf.set("hbase.zookeeper.quorum", "localhost:2182") 
        val tableName = "Streaming_wordcount" 
        val hTable = new HTable(hConf, tableName) 
        val tableDescription = new HTableDescriptor(tableName) 
        //tableDescription.addFamily(new HColumnDescriptor("Details".getBytes()))
         
        val thePut = new Put(Bytes.toBytes(row._1.toString())) 
        thePut.add(Bytes.toBytes("Word_count"), Bytes.toBytes("Occurances"), Bytes.toBytes(row._2.toString)) 
        hTable.put(thePut)
      }

      val Hbase_inset = cnt.foreachRDD(rdd => if (!rdd.isEmpty()) rdd.foreach(toHBase(_)))
      
      // Start the computation
      ssc.start()
      ssc.awaitTermination()
    
      
    } finally {
    }
    
  }
  
}

/*
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