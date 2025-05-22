package msg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Generic Spring Boot application entry point.
 * For specific sender functionality, run {@code msg.sender.SpringKafkaApplicationSenderMain} as the main class.
 * For specific receiver functionality, run {@code msg.receiver.SpringKafkaApplicationReceiverMain} as the main class.
 */
@SpringBootApplication
public class SpringKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaApplication.class, args);
	}
}
