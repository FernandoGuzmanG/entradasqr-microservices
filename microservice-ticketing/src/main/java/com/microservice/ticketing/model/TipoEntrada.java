package com.microservice.ticketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipos_entrada")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa la configuración de un tipo de ticket para un evento, incluyendo stock, precio y período de venta.")
public class TipoEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Clave primaria del tipo de entrada.", example = "1")
    private Long idTipoEntrada;

    @Schema(description = "Identificador del evento al que pertenece este tipo de entrada.", example = "101")
    private Long idEvento;

    @Schema(description = "Nombre visible del tipo de entrada (e.g., 'Entrada VIP - Primera Fase').", example = "Entrada General - Fase 1")
    private String nombre;

    @Schema(description = "Descripción detallada del ticket.")
    private String descripcion;

    @Schema(description = "Costo de la entrada. Usa BigDecimal para precisión monetaria.", example = "50.00")
    private BigDecimal precio;

    @Schema(description = "Stock máximo disponible para este tipo de entrada.", example = "500")
    private Integer cantidadTotal;

    @Schema(description = "Contador de entradas ya generadas o reservadas. Usado para control de stock.", example = "150")
    private Integer cantidadEmitida;

    @Schema(description = "Fecha y hora en que se activa la venta.", example = "2024-10-01T09:00:00")
    private LocalDateTime fechaInicioVenta;

    @Schema(description = "Fecha y hora en que finaliza la venta.", example = "2024-10-31T23:59:59")
    private LocalDateTime fechaFinVenta;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del tipo de entrada (ACTIVO, AGOTADO, INACTIVO).", example = "ACTIVO")
    private EstadoTipoEntrada estado;

    public enum EstadoTipoEntrada {
        ACTIVO,
        AGOTADO,
        INACTIVO
    }
}