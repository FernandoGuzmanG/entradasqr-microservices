package com.microservice.eventos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Eventos",
                version = "1.0",
                description = "API para la gestión del ciclo de vida de Eventos (creación, actualización, cancelación), y la administración de Staff y permisos.",
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