package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "DTO utilizado para solicitar el envío de los códigos QR al microservicio de notificaciones/comunicaciones.")
public class EnvioEntradasRequest {

    @Schema(description = "ID del registro del invitado/orden que generó los tickets.", example = "50")
    private Long idInvitado;

    @Schema(description = "Correo electrónico de destino.", example = "destino@mail.com")
    private String correoDestino;

    @Schema(description = "Nombre del invitado (personalización del correo).", example = "Juan Pérez")
    private String nombreInvitado;

    @Schema(description = "Nombre del evento (obtenido desde microservice-eventos).", example = "Concierto Rock 2024")
    private String nombreEvento;

    @Schema(description = "ID del tipo de entrada.", example = "1")
    private Long idTipoEntrada;

    @Schema(description = "Nombre del tipo de entrada (personalización del correo).", example = "Entrada General - Fase 1")
    private String nombreTipoEntrada;

    @Schema(description = "Lista de tickets individuales generados (códigos QR).")
    private List<TicketData> tickets;

    @Data
    @Schema(description = "Detalle de cada ticket individual para el envío.")
    public static class TicketData {
        @Schema(description = "Código único del QR.", example = "TKT-EVT101-50-01")
        private String codigoQR;

        @Schema(description = "Estado de uso del ticket.", example = "NO_UTILIZADA")
        private String estadoUso;
    }
}