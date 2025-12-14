package com.microservice.usuarios.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "DTO para mostrar errores con código y mensaje.")
public class ErrorResponse {
    @Schema(description = "Código de estado HTTP (ej: 400, 404, 409)")
    private final int status;
    @Schema(description = "Razón del estado HTTP (ej: BAD_REQUEST, NOT_FOUND)")
    private final String error;
    @Schema(description = "Mensaje detallado de la excepción (e.g., \"El correo ya está registrado.\")")
    private final String message;
    @Schema(description = "URI de la solicitud fallida")
    private final String path;
    @Schema(description = "Momento en que ocurrió el error")
    private final LocalDateTime timestamp;
}