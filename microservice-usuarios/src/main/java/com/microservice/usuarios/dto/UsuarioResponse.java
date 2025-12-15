package com.microservice.usuarios.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "DTO de respuesta del perfil de usuario (información pública y de gestión).")
public class UsuarioResponse {
    
    @Schema(description = "Identificador único del usuario.", example = "101")
    private Long id;    
    
    @Schema(description = "Rol Único Tributario.", example = "12345678-9")
    private String rut;
    
    @Schema(description = "Correo electrónico del usuario.", example = "usuario@ejemplo.com")
    private String correo;
    
    @Schema(description = "Nombres de pila.", example = "Juan Pablo")
    private String nombres;
    
    @Schema(description = "Apellidos.", example = "Perez Soto")
    private String apellidos;
    
    @Schema(description = "Número de teléfono de contacto.", example = "+56998765432")
    private String telefono;
    
    @Schema(description = "Fecha y hora del registro del usuario.", example = "2023-11-20T14:30:00")
    private LocalDateTime fechaRegistro;

}