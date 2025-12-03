package com.microservice.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MicroserviceTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceTicketingApplication.class, args);
	}

}
