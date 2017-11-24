package msg.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@Configuration
public class TwitterConfig {

	 @Autowired
	 private Environment env;
		
	
	@Bean
	TwitterStream twitterStream() {
		 ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		 String appId = env.getProperty("social.twitter.app-id");
		 String appSecret = env.getProperty("social.twitter.app-secret");
		 String oathToken = env.getProperty("social.twitter.oath.token");
		 String oathTokenSecret = env.getProperty("social.twitter.oath-secret");
			
         configurationBuilder.setOAuthConsumerKey(appId)
                 .setOAuthConsumerSecret(appSecret)
                 .setOAuthAccessToken(oathToken)
                 .setOAuthAccessTokenSecret(oathTokenSecret);
    	return new TwitterStreamFactory(configurationBuilder.build()).getInstance();
	}
}
