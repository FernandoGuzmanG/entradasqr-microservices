package com.microservice.usuarios.dto;

import com.microservice.usuarios.model.Usuario.EstadoUsuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponse {
    private Long idUsuario;
    private String rut;
    private String correo;
    private String nombres;
    private String apellidos;
    private String telefono;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro;
}