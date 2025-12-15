package com.microservice.usuarios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Usuarios",
                version = "1.0",
                description = "API para la gestión de autenticación (Login), registro y perfiles de usuario.",
                contact = @Contact(
                        name = "Ticketing Staff App Team"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080/api/usuarios", description = "Servidor Principal (Gateway)")
        }
)
public class OpenApiConfig {
}