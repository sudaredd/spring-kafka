package msg.springkafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringKafkaApplication {

	public static void main(String[] args) {
		System.out.println("kafka ex");
		SpringApplication.run(SpringKafkaApplication.class, args);
	}
}
