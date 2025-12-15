package com.microservice.ticketing.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Ticketing",
                version = "1.0",
                description = "API para la gestión de tickets (creación, emisión, check-in) y administración de invitados y tipos de entrada.",
                contact = @Contact(
                        name = "Ticketing Staff App Team"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080/api", description = "Servidor Principal (Gateway)")
        }
)
public class OpenApiConfig {
}