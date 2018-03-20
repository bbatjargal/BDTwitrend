#!/bin/bash
spark-submit --class "edu.mum.bdt.BDTwitrendConsumer" --master local[2] BDTwitrendConsumer.jar localhost:9092 topic-bdtwitrend
