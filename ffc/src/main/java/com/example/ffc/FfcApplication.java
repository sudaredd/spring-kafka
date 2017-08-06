package com.example.ffc;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class FfcApplication {

	@Bean
	WebClient webClient() {
		return WebClient.create();
	}
	@Bean
	CommandLineRunner demo(WebClient client) {
		return args-> {
			client.get().uri("http://localhost:8080/movies/")
			.exchange()
			.subscribe(cr-> cr.bodyToFlux(Movie.class)
					.filter(m->m.getTitle().equals("Lord of rinds"))
					.subscribe(silennce->
					 			client.get().uri("http://localhost:8080/movies/"+silennce.getId()+"/events")
					 			.exchange()
					 			.subscribe(c->c.bodyToFlux(MovieEvent.class)
					 					.subscribe(System.out::println))));
		};
	}
	public static void main(String[] args) {
		SpringApplication.run(FfcApplication.class, args);
	}
}
@NoArgsConstructor
@AllArgsConstructor
@Data
class Movie {
	private String id;
	private String title;

}

@NoArgsConstructor
@AllArgsConstructor
@Data
class MovieEvent {
	Movie movie;
	Date now;
}
