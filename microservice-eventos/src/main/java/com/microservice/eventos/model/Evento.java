package com.microservice.eventos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información detallada de un evento organizado por un Owner.")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del evento.", example = "50")
    private Long idEvento;

    @Column(name = "owner_id", nullable = false)
    @Schema(description = "ID del usuario que creó y administra el evento (Owner).", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ownerId;

    @Column(nullable = false)
    @Schema(description = "Nombre comercial del evento.", example = "Concierto de Verano", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Categoría del evento.", example = "Música/Festival")
    private String categoria;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descripción detallada del evento.")
    private String descripcion;

    @Schema(description = "Dirección física del lugar del evento.", example = "Av. Principal 123, Santiago")
    private String direccion;

    @Column(nullable = false)
    @Schema(description = "Fecha en que se realiza el evento.", example = "2024-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    @Schema(description = "Hora de inicio del evento.", example = "20:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaInicio;

    @Column(name = "hora_cierre_puertas")
    @Schema(description = "Hora límite de ingreso al recinto.")
    private LocalTime horaCierrePuertas;

    @Column(name = "hora_termino")
    @Schema(description = "Hora de finalización estimada del evento.")
    private LocalTime horaTermino;

    @Column(name = "capacidad_maxima")
    @Schema(description = "Número máximo de asistentes permitido.")
    private Integer capacidadMaxima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado actual del evento (Publicado, Cancelado, etc.).", requiredMode = Schema.RequiredMode.REQUIRED)
    private EstadoEvento estado;

    @Schema(description = "Posibles estados en los que puede estar un Evento.")
    public enum EstadoEvento {
        Publicado, Finalizado, Cancelado
    }
}