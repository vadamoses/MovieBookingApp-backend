package com.moviebookingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication(scanBasePackages = {"com.moviebookingapp"})
@OpenAPIDefinition(info = @Info(title = "Movie booking Application", version = "1.0",
description = "This application acts as the ticketting service of a theater."))
public class MoviebookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviebookingApplication.class, args);
	}

}
