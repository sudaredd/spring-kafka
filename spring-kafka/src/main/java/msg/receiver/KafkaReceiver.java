package msg.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import msg.model.TwitterUser;

public class KafkaReceiver {

	 private static final Logger logger = LoggerFactory
	            .getLogger(KafkaReceiver.class);

	 int soh = 0x01;

	 @Autowired
	 private TwitterUserRepository twitterUserRepository;
	 
	    @KafkaListener(topics = "darsan")
	    public void receiveMsg(String message) {
	        logger.info("received message in derasan ='{}'", message);
	    	String []msg = message.split("");
	    	TwitterUser twitterUser = TwitterUser.newInstance(msg);
	    	twitterUserRepository.save(twitterUser);
	    	logger.info("saved tweet to Mongo db:"+twitterUser);
	    }

}
