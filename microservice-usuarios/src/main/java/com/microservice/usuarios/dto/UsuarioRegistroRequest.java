package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO de entrada para el registro de nuevos usuarios.")
public class UsuarioRegistroRequest {

    @Schema(description = "Rol Único Tributario (identificador nacional).", example = "12345678-9", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rut;

    @Schema(description = "Correo electrónico del usuario.", example = "nuevo.usuario@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;

    @Schema(description = "Contraseña en texto plano para el registro.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Nombres de pila del usuario.", example = "Ana Sofía", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombres;

    @Schema(description = "Apellidos de pila del usuario.", example = "Gómez Salas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto.", example = "+56912345678")
    private String telefono;
}