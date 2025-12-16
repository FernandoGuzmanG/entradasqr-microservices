package com.microservice.eventos.dto;

import com.microservice.eventos.model.Evento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para los detalles de un Evento, utilizado en listados y dashboards.")
public class EventoResponse {

    @Schema(description = "Identificador único del evento.", example = "50")
    private Long idEvento;

    @Schema(description = "Nombre comercial del evento.", example = "Concierto de Verano")
    private String nombre;

    @Schema(description = "Categoría del evento.", example = "Música/Festival")
    private String categoria;

    @Schema(description = "Descripción detallada del evento.")
    private String descripcion;

    @Schema(description = "Dirección física del lugar del evento.", example = "Av. Principal 123, Santiago")
    private String direccion;

    @Schema(description = "Fecha en que se realiza el evento.", example = "2024-12-31")
    private LocalDate fecha;

    @Schema(description = "Hora de inicio del evento.", example = "20:00:00")
    private LocalTime horaInicio;

    @Schema(description = "Hora límite de ingreso al recinto.", example = "19:30:00")
    private LocalTime horaCierrePuertas;

    @Schema(description = "Hora de finalización estimada del evento.", example = "23:00:00")
    private LocalTime horaTermino;

    @Schema(description = "Número máximo de asistentes permitido.", example = "500")
    private Integer capacidadMaxima;

    @Schema(description = "Estado actual del evento (Publicado, Finalizado, Cancelado).", example = "Publicado")
    private String estado;

    @Schema(description = "Relación del usuario logueado con este evento.", example = "OWNER, STAFF, INVITADO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String relacionUsuario;

    public static EventoResponse fromEntity(Evento evento) {
        return EventoResponse.builder()
                .idEvento(evento.getIdEvento())
                .nombre(evento.getNombre())
                .categoria(evento.getCategoria())
                .descripcion(evento.getDescripcion())
                .direccion(evento.getDireccion())
                .fecha(evento.getFecha())
                .horaInicio(evento.getHoraInicio())
                .horaCierrePuertas(evento.getHoraCierrePuertas())
                .horaTermino(evento.getHoraTermino())
                .capacidadMaxima(evento.getCapacidadMaxima())
                .estado(evento.getEstado().name())
                .build();
    }
}