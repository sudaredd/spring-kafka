package msg.sender;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@EnableAutoConfiguration
@SpringBootApplication
public class SpringKafkaApplicationSenderMain implements CommandLineRunner {

	private static final Logger looger = LoggerFactory
            .getLogger(SpringKafkaApplicationSenderMain.class);
	
	 @Autowired
	 private KafkaSender kafkaSender;
	 
	 @Autowired
	 private ConfigurableApplicationContext applicationContext;
	 
	 @Autowired
	 private Environment env;
		
	@Bean
	public Twitter twitter() {
		String appId = env.getProperty("social.twitter.app-id");
		String appSecret = env.getProperty("social.twitter.app-secret");
		return new TwitterTemplate(appId, appSecret);
	}
	 
	@Autowired
	private Twitter twitter;
	
	@Autowired
	private TwitterStream twitterStream;
	 
	    public static void main(String[] args)  {
	/*		new SpringApplicationBuilder(SpringKafkaApplicationSenderMain.class)
		//	.web(WebApplicationType.NONE)
			.run(args);*/
			SpringApplication.run(SpringKafkaApplicationSenderMain.class);
	    }
	    
		private void sendFixMsg(int size) {
			String fixMsg = 	"8=FIX.4.29=17535=D49=SENDER56=TARGET34=24850=frg52=20100702-11:12:4211=BS0100035492400021=3100=J55=ILA SJ48=YY7722=5167=CS207=J54=160=20100702-11:12:4238=50040=115=ZAR59=010=230";
			ExecutorService executor = Executors.newFixedThreadPool(4);
	    	for(int i=0; i<size;i++) {
	    		executor.execute(()->writeToKafka(fixMsg));
	    	}
	    	executor.shutdown();
	    	try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
		}

		private void writeToKafka(String msg) {
			kafkaSender.sendMessage("darsan", msg);
		}

		@Override
		public void run(String... args) throws Exception {
			
			readTwitterStream();

			/*sendFixMsg(1);
			sendTweets("#java8");*/
			//sendTweets("#java9");
			
	   // 	applicationContext.close();

		}
		  private void readTwitterStream() {

			  StatusListener statusListener =   new StatusListener() {
					@Override
					public void onException(Exception ex) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStatus(Status status) {
						String message = "time :"+status.getCreatedAt() + ",user:"+ status.getUser().getName() + ",text:"+status.getText();
						looger.info(message);
						writeToKafka(message);
					}
					
					@Override
					public void onStallWarning(StallWarning warning) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onScrubGeo(long userId, long upToStatusId) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
						// TODO Auto-generated method stub
						
					}
				};
				
				twitterStream.addListener(statusListener);
				twitterStream.filter("#java","#java8","#Black Friday","#Ashes cricket","#python","#Donald Trump");
		}

		public List<Tweet> helloTwitter(String hashTag) {
		    	return twitter.searchOperations().search(hashTag,20).getTweets();
		    }

		private void sendTweets(String hashTag) {
			List<Tweet> tweets = helloTwitter(hashTag);
			tweets.stream().map(t->t.getFromUser() + ":" + t.getText())
			.forEach(this::writeToKafka);
			
		}
}
