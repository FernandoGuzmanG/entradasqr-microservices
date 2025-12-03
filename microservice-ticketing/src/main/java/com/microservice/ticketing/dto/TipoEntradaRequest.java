package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Solicitud para crear o actualizar un Tipo de Entrada.")
public class TipoEntradaRequest {

    @Schema(description = "ID del evento al que pertenece este tipo de entrada.", example = "101")
    private Long idEvento;

    @Schema(description = "Nombre del ticket (ej: 'Entrada VIP').", example = "Entrada General - Primera Fase")
    private String nombre;

    @Schema(description = "Descripción del tipo de ticket.", example = "Incluye acceso a área lounge y barra libre.")
    private String descripcion;

    @Schema(description = "Precio del ticket.", example = "75.50")
    private BigDecimal precio;

    @Schema(description = "Stock total disponible para este tipo de ticket.", example = "1000")
    private Integer cantidadTotal;

    @Schema(description = "Fecha y hora de inicio de venta.", example = "2024-10-01T00:00:00")
    private LocalDateTime fechaInicioVenta;

    @Schema(description = "Fecha y hora de fin de venta.", example = "2024-11-01T23:59:59")
    private LocalDateTime fechaFinVenta;
}