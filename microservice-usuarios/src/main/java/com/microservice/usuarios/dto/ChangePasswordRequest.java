package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud para el cambio de contraseña de un usuario.")
public class ChangePasswordRequest {

    @Schema(description = "La contraseña actual del usuario para verificación.", 
            requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "password123")
    private String currentPassword;

    @Schema(description = "La nueva contraseña deseada (8-16 caracteres).", 
            requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "newPassword456")
    private String newPassword;

    @Schema(description = "Repetición de la nueva contraseña para confirmación.", 
            requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "newPassword456")
    private String confirmationPassword;
}