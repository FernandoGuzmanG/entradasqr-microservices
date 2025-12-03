package com.microservice.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MicroserviceUsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceUsuariosApplication.class, args);
	}

}
