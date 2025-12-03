package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para la autenticación de usuarios.")
public class LoginRequest {

    @Schema(description = "Correo electrónico para el login.", example = "juan.perez@email.com", required = true)
    private String correo;

    @Schema(description = "Contraseña en texto plano.", required = true)
    private String password;
}