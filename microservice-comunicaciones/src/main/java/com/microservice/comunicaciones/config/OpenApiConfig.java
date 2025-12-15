package com.microservice.comunicaciones.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Comunicaciones",
                version = "1.0",
                description = "API para la gestión y envío de notificaciones transaccionales (correos electrónicos) a los usuarios/invitados.",
                contact = @Contact(
                        name = "Ticketing Staff App Team"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080/api/notificaciones", description = "Servidor Principal (Gateway)")
        }
)
public class OpenApiConfig {
}