package com.microservice.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Resumen de estadísticas y próximos eventos para el Dashboard del usuario.")
public class DashboardResponse {

    @Schema(description = "Cantidad de eventos donde el usuario es el Owner.")
    private long cantidadEventosPropios;

    @Schema(description = "Cantidad de eventos donde el usuario es Staff activo (invitación aceptada).")
    private long cantidadEventosStaff;

    @Schema(description = "Cantidad de invitaciones de Staff pendientes de respuesta.")
    private long cantidadInvitacionesPendientes;

    @Schema(description = "Lista de los próximos eventos (Owner o Staff) ordenados por fecha.")
    private List<EventoResponse> proximosEventos;
}