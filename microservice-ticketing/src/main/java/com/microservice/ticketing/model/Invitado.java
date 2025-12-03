package com.microservice.ticketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa una orden o asignación de entradas a una persona, la cual genera uno o varios tickets.")
public class Invitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Clave primaria de la orden de invitado/compra.", example = "50")
    private Long idInvitado;

    @Schema(description = "Nombre completo del receptor o comprador de las entradas.", example = "Juan Pérez")
    private String nombreCompleto;

    @Schema(description = "Email del receptor, utilizado para el envío del código QR o códigos.", example = "juan.perez@example.com")
    private String correo;

    @Schema(description = "Identificador del tipo de entrada asociado a esta orden.", example = "1")
    private Long idTipoEntrada;

    @Schema(description = "Número de entradas solicitadas/asignadas bajo esta orden.", example = "2")
    private Integer cantidad;

    @Schema(description = "Timestamp de cuando se registró la orden/invitación.", example = "2024-10-20T15:30:00")
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado del proceso de comunicación (envío del QR al correo). PENDIENTE, ENVIADO, ERROR_ENVIO.", example = "PENDIENTE")
    private EstadoEnvio estadoEnvio;

    public enum EstadoEnvio {
        PENDIENTE,
        ENVIADO,
        ERROR_ENVIO
    }
}