package edu.mum.bdt.java;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/** 
 * Arguments: <comsumerKey> <consumerSecret> <accessToken> <accessTokenSecret> <topic-name>
 * <comsumerKey>	- Twitter consumer key 
 * <consumerSecret>  	- Twitter consumer secret
 * <accessToken>	- Twitter access token
 * <accessTokenSecret>	- Twitter access token secret
 * <topic-name>		- The kafka topic to subscribe to
 */

public class BDTwitrendProducer {
	public static void main(String[] args) throws Exception {
		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);

		if (args.length < 4) {
			System.out.println(
					"Usage: BDTwitrendProducer <twitter-consumer-key> <twitter-consumer-secret> <twitter-access-token> <twitter-access-token-secret> <topic-name>");
			return;
		}
		//Logger rootLogger = Logger.getRootLogger();
		//rootLogger.setLevel(Level.ERROR);

//Logger.getLogger("org").setLevel(Level.OFF);
//Logger.getLogger("akka").setLevel(Level.OFF);

		String consumerKey = args[0].toString();
		String consumerSecret = args[1].toString();
		String accessToken = args[2].toString();
		String accessTokenSecret = args[3].toString();
		String topicName = args[4].toString();
		//String[] arguments = args.clone();
		//String[] keyWords = Arrays.copyOfRange(arguments, 5, arguments.length);

		// Set twitter oAuth tokens in the configuration
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);

		// Create twitterstream using the configuration
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				queue.offer(status);
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId);
			}

			//@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			//@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		twitterStream.addListener(listener);

		// Filter keywords
		//FilterQuery query = new FilterQuery().track(new String[] {""});
		//twitterStream.filter(query);
		
		twitterStream.sample("en");

		// Thread.sleep(5000);

		// Add Kafka producer config settings
		
		Properties props = new Properties();
		props.put("metadata.broker.list", "localhost:9092");
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);

		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);

		// poll for new tweets in the queue. If new tweets are added, send them
		// to the topic
		StringBuilder sb;
		String delimiter = "º¿";
		
		Locale dateLocale = Locale.US;
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", dateLocale);
		
		while (true) {
			
			Status ret = queue.poll();
			
			sb = new StringBuilder();			
			
			//ret.
			if (ret == null) {
				Thread.sleep(100);
				// i++;
			} else {
				StringJoiner hashTags = new StringJoiner(" ");
				for (HashtagEntity hashtage : ret.getHashtagEntities()) {
					hashTags.add(hashtage.getText());
				}

				sb.append(hashTags.toString()).append(delimiter);
				sb.append(ret.getText()).append(delimiter);
				if(ret.getGeoLocation() != null) {
					sb.append(ret.getGeoLocation().getLatitude()).append(delimiter);
					sb.append(ret.getGeoLocation().getLongitude()).append(delimiter);
				}else {
					sb.append("").append(delimiter);
					sb.append("").append(delimiter);
				}
				if(ret.getUser() != null) {
					sb.append(outFormat.format(ret.getUser().getCreatedAt())).append(delimiter);
					sb.append(ret.getUser().getName());
				}else {
					sb.append("").append(delimiter).append(delimiter);
					sb.append("").append(delimiter);
				}
				

				System.out.println("Data: " + sb.toString());
				producer.send(new ProducerRecord<String, String>(topicName, UUID.randomUUID().toString(), sb.toString() ));
				
				//new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())
			}
		}
		// producer.close();
		// Thread.sleep(500);
		// twitterStream.shutdown();
	}

}