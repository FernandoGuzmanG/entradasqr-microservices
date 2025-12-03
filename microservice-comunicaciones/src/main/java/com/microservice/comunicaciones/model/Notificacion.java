package com.microservice.comunicaciones.model;

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
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @Column(nullable = false)
    private Long idInvitado; // FK al servicio de Ticketing

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    @Builder.Default
    private String tipoComunicacion = "EMAIL";

    @Column(nullable = false)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String cuerpoMensaje; // Opcional: registrar el contenido final

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estadoEnvio;

    private LocalDateTime fechaEnvio;

    public enum EstadoEnvio {
        PENDIENTE,
        ENVIADO,
        FALLIDO,
        REINTENTANDO
    }
}