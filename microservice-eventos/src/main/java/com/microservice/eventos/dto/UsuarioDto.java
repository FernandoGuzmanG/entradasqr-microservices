package com.microservice.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para transferir información básica del usuario (usado por Feign Client).")
public class UsuarioDto {
    @Schema(description = "Identificador único del usuario.", example = "101")
    private Long idUsuario;

    @Schema(description = "Rol Único Tributario.")
    private String rut;

    @Schema(description = "Correo electrónico del usuario.")
    private String correo;

    @Schema(description = "Nombres de pila.")
    private String nombres;

    @Schema(description = "Apellidos.")
    private String apellidos;

    @Schema(description = "Estado del usuario (Activo, Inactivo).", example = "Activo")
    private String estado;
}