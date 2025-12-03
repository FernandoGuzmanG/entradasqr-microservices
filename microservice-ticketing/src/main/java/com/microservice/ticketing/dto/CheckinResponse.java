package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Respuesta detallada tras el escaneo de un código QR en el check-in.")
public class CheckinResponse {

    @Schema(description = "Mensaje de resultado del check-in (ej: 'ACCESO CONCEDIDO.', 'Entrada ya utilizada.').", example = "ACCESO CONCEDIDO.")
    private String mensaje;

    @Schema(description = "El código QR escaneado.", example = "TKT-EVT101-50-01")
    private String codigoQR;

    @Schema(description = "Estado de uso del ticket después de la validación.", example = "UTILIZADA")
    private String estadoUso;

    @Schema(description = "Timestamp del momento del uso/escaneo.", example = "2024-11-05T18:00:00")
    private LocalDateTime fechaUso;

    @Schema(description = "Nombre completo del invitado asociado al ticket.", example = "Juan Pérez")
    private String nombreInvitado;

    @Schema(description = "Correo del invitado.", example = "juan.perez@example.com")
    private String correo;

    @Schema(description = "Nombre del tipo de entrada (ej: 'Entrada VIP').", example = "Entrada General - Fase 1")
    private String nombreTipoEntrada;
}