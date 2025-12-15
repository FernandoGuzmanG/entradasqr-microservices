package com.microservice.comunicaciones.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa el registro histórico y estado de una notificación (correo) enviada.")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la notificación.", example = "10")
    private Long idNotificacion;

    @Column(nullable = false)
    @Schema(description = "ID del invitado asociado en el servicio de Ticketing.", example = "505", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idInvitado; // FK al servicio de Ticketing

    @Column(nullable = false)
    @Schema(description = "Dirección de correo electrónico del destinatario.", example = "juan.perez@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String destinatario;

    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Tipo de canal de comunicación utilizado.", example = "EMAIL", defaultValue = "EMAIL")
    private String tipoComunicacion = "EMAIL";

    @Column(nullable = false)
    @Schema(description = "Asunto del mensaje enviado.", example = "Tus entradas para el Concierto", requiredMode = Schema.RequiredMode.REQUIRED)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Cuerpo completo del mensaje (HTML o Texto) para auditoría.", example = "<html><body>Hola...</body></html>")
    private String cuerpoMensaje; // Opcional: registrar el contenido final

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del proceso de envío.", example = "ENVIADO")
    private EstadoEnvio estadoEnvio;

    @Schema(description = "Fecha y hora exacta en que se procesó el envío.", example = "2024-12-01T15:30:00")
    private LocalDateTime fechaEnvio;

    @Schema(description = "Enumeración de los posibles estados de una notificación.")
    public enum EstadoEnvio {
        PENDIENTE,
        ENVIADO,
        FALLIDO,
        REINTENTANDO
    }
}