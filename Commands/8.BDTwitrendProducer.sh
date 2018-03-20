#!/bin/bash
spark-submit --class "edu.mum.bdt.java.BDTwitrendProducer" --master local[2] BDTwitrendProducer.jar <twitter-consumer-key> <twitter-consumer-secret> <twitter-access-token> <twitter-access-token-secret>  topic-bdtwitrend
