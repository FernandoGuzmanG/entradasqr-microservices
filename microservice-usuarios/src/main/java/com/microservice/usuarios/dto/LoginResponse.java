package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta de autenticación que contiene el token JWT.")
public class LoginResponse {
    
    @Schema(description = "Token JWT de acceso para la sesión.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVC...")
    private final String token;
}