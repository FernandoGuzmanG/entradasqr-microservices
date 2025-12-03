package com.microservice.eventos.dto;

import com.microservice.eventos.model.Evento;
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
public class EventoResponse {

    private Long idEvento;
    private String nombre;
    private String categoria;
    private String descripcion;
    private String direccion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaCierrePuertas;
    private LocalTime horaTermino;
    private Integer capacidadMaxima;
    private String estado;
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