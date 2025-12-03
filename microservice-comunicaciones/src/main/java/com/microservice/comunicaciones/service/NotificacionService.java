package com.microservice.comunicaciones.service;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import com.microservice.comunicaciones.model.Notificacion;
import com.microservice.comunicaciones.model.Notificacion.EstadoEnvio;
import com.microservice.comunicaciones.repository.NotificacionRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;

    /**
     * Procesa una solicitud de envío de entradas, registrando el intento y actualizando el estado.
     */
    @Transactional
    public void procesarEnvioEntradas(EnvioEntradasRequest request) {

        // 1. CREAR REGISTRO INICIAL (PENDIENTE)
        Notificacion notificacion = Notificacion.builder()
                .idInvitado(request.getIdInvitado())
                .destinatario(request.getCorreoDestino())
                .asunto("Tus Entradas para: " + request.getNombreEvento())
                .estadoEnvio(EstadoEnvio.PENDIENTE)
                .fechaEnvio(LocalDateTime.now())
                // Opcional: Construir el cuerpo antes de registrar para auditoría
                .cuerpoMensaje(emailService.buildEmailContent(request.getNombreInvitado(),
                        request.getNombreEvento(),
                        request.getNombreTipoEntrada(),
                        request.getTickets()))
                .build();

        notificacion = notificacionRepository.save(notificacion);

        try {
            // 2. LLAMAR AL SERVICIO DE ENVÍO (SÍNCRONO A MAILPIT)
            emailService.enviarEntradas(request);

            // 3. ACTUALIZAR ESTADO (ÉXITO)
            notificacion.setEstadoEnvio(EstadoEnvio.ENVIADO);
            notificacion.setFechaEnvio(LocalDateTime.now());

        } catch (MessagingException e) {
            // 4. ACTUALIZAR ESTADO (FALLO)
            notificacion.setEstadoEnvio(EstadoEnvio.FALLIDO);
            notificacion.setFechaEnvio(LocalDateTime.now());

            // **CRÍTICO:** Imprimir el stack trace completo de la excepción original (MessagingException)
            System.err.println("--- ERROR DE MENSAJERÍA (MAILPIT) ---");
            e.printStackTrace();
            System.err.println("-------------------------------------");

            // Relanzar como RuntimeException para que Feign lo capture
            throw new RuntimeException("Fallo al enviar el correo: " + e.getMessage(), e);

        } finally {
            notificacionRepository.save(notificacion);
        }
    }
}