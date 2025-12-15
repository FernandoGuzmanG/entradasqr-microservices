package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud para el cambio de contraseña de un usuario.")
public class ChangePasswordRequest {

    @Schema(description = "La contraseña actual del usuario para verificación.", required = true, example = "password123")
    private String currentPassword;

    @Schema(description = "La nueva contraseña deseada (8-16 caracteres).", required = true, example = "newPassword456")
    private String newPassword;

    @Schema(description = "Repetición de la nueva contraseña para confirmación.", required = true, example = "newPassword456")
    private String confirmationPassword;
}