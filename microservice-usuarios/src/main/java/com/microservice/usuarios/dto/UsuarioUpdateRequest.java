package com.microservice.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO de entrada para actualizar el perfil del usuario. Los campos nulos no se actualizan.")
public class UsuarioUpdateRequest {

    @Schema(description = "Nombres de pila del usuario (opcional para actualizar).", example = "Juan Pablo")
    private String nombres;

    @Schema(description = "Apellidos de pila del usuario (opcional para actualizar).", example = "Rodríguez")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto (opcional para actualizar).", example = "+56998765432")
    private String telefono;

}