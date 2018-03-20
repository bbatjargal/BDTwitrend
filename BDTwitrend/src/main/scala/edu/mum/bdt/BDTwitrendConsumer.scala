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

    if (args.length != 3) {
      System.err.println(s"""
        |Usage: BDTwitrendConsumer <brokers> <topics> <checkpoint-address>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topics> is a list of one or more kafka topics to consume from
        |  <checkpoint-address> is a check point directory for performing stateful operations
        |
        """.stripMargin)
      System.exit(1)
    }
    
        
    val Array(brokers, topics, checkpoint_address) = args
    
    // Create context with 6 second batch interval
    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount").setMaster("local[2]").set("spark.executor.memory","1g");
    val ssc = new StreamingContext(sparkConf, Seconds(6))
    
    //val sc = ssc.sparkContext
    //sc.setLogLevel("ERROR")
    
    try{
      // Create direct kafka stream with brokers and topics
      val topicsSet = topics.split(",").toSet
      
      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
      
      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, topicsSet)
      
      //HBASE
      val updateFunc = (values: Seq[Int], state: Option[Int]) => { 
        val currentCount = values.sum       
        val previousCount = state.getOrElse(0)         
        val updatedSum = currentCount+previousCount         
        Some(updatedSum)       
      }
    
      //Defining a check point directory for performing stateful operations 
      ssc.checkpoint(checkpoint_address) 

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
    val hTable = new HTable(hConf, "tblTweet") 
    
     
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