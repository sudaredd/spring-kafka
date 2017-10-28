package msg.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaReceiver {

	 private static final Logger LOGGER = LoggerFactory
	            .getLogger(KafkaReceiver.class);

	    @KafkaListener(topics = "darsan")
	    public void receiveMsg(String message) {
	        LOGGER.info("received message in derasan ='{}'", message);
	    }

}
