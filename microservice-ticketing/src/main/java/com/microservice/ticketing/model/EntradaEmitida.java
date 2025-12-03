package com.microservice.ticketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "entradas_emitidas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa un ticket único e individual, listo para ser escaneado en el check-in.")
public class EntradaEmitida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Clave primaria del ticket individual.", example = "1001")
    private Long idEntrada;

    @Schema(description = "Identificador de la orden de invitado/compra que generó este ticket.", example = "50")
    private Long idInvitado;

    @Schema(description = "Identificador del tipo de entrada (relación redundante).", example = "1")
    private Long idTipoEntrada;

    @Column(unique = true, nullable = false)
    @Schema(description = "Código único que se convierte en el QR escaneable.", example = "TKT-EVT101-50-01")
    private String codigoQR;

    @Schema(description = "Momento exacto en que se generó este código QR.", example = "2024-10-20T15:35:00")
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del ticket en la puerta del evento. NO_UTILIZADA, UTILIZADA, ANULADA.", example = "NO_UTILIZADA")
    private EstadoUso estadoUso;

    @Schema(description = "Timestamp del check-in (momento del escaneo). Solo si estadoUso es UTILIZADA.", example = "2024-11-05T18:00:00")
    private LocalDateTime fechaUso;

    public enum EstadoUso {
        NO_UTILIZADA,
        UTILIZADA,
        ANULADA
    }
}