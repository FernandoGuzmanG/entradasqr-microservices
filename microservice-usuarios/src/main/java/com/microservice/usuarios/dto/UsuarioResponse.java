package com.microservice.usuarios.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponse {
    private String rut;
    private String correo;
    private String nombres;
    private String apellidos;
    private String telefono;
    private LocalDateTime fechaRegistro;
}