package com.booking.movietickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MovieticketsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieticketsApplication.class, args);
	}

}
