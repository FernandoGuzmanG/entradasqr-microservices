package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO de entrada para el registro de nuevos usuarios.")
public class UsuarioRegistroRequest {

    @Schema(description = "Rol Único Tributario (identificador nacional).", example = "12345678-9", required = true)
    private String rut;

    @Schema(description = "Correo electrónico del usuario.", example = "nuevo.usuario@email.com", required = true)
    private String correo;

    @Schema(description = "Contraseña en texto plano para el registro.", required = true)
    private String password;

    @Schema(description = "Nombres de pila del usuario.", example = "Ana Sofía", required = true)
    private String nombres;

    @Schema(description = "Apellidos de pila del usuario.", example = "Gómez Salas", required = true)
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto.", example = "+56912345678")
    private String telefono;
}