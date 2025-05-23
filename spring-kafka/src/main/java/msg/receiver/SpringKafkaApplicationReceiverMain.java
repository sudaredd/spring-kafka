package msg.receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import msg.model.TwitterUser;

/**
 * Main entry point for running the Kafka message receiver application.
 * This application listens to a Kafka topic, processes incoming messages (tweets),
 * and saves them to a MongoDB database.
 * 
 * To run this receiver, execute the main method in this class.
 */
@EnableAutoConfiguration
@SpringBootApplication
public class SpringKafkaApplicationReceiverMain implements CommandLineRunner {

	 @Autowired
	 private KafkaReceiver kafkaReceiver;

	    public static void main(String[] args)  {
		ConfigurableApplicationContext applicationContext =	new SpringApplicationBuilder(SpringKafkaApplicationReceiverMain.class)
			.web(WebApplicationType.NONE).run(args);
		
	    }


		@Override
		public void run(String... args) throws Exception {

		}
}

interface TwitterUserRepository extends ReactiveMongoRepository<TwitterUser, String> {
	
}