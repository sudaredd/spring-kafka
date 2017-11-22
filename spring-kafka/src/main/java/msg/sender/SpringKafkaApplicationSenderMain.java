package msg.sender;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

@EnableAutoConfiguration
@SpringBootApplication
public class SpringKafkaApplicationSenderMain implements CommandLineRunner {

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

			sendFixMsg(1);
			sendTweets("#java8");
			//sendTweets("#java9");
			
	    	applicationContext.close();

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
