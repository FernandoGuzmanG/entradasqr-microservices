package com.microservice.comunicaciones.service;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import com.microservice.comunicaciones.model.Notificacion;
import com.microservice.comunicaciones.model.Notificacion.EstadoEnvio;
import com.microservice.comunicaciones.repository.NotificacionRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    // Se asume que EmailService ya tiene su propia capa de pruebas y se mockea aquí
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacionService notificacionService;

    // --- Datos de Prueba ---
    private final Long INVITADO_ID = 500L;
    private final String CORREO_TEST = "test@example.com";
    private EnvioEntradasRequest request;

    @BeforeEach
    void setUp() {
        // Configuramos la solicitud de entrada para la prueba
        request = new EnvioEntradasRequest();
        request.setIdInvitado(INVITADO_ID);
        request.setCorreoDestino(CORREO_TEST);
        request.setNombreEvento("Evento de Prueba");
        request.setNombreTipoEntrada("VIP");
        request.setNombreInvitado("Juan Test");
        
        // El DTO de tickets debe existir aunque esté vacío para evitar NullPointerException
        EnvioEntradasRequest.TicketData ticket = new EnvioEntradasRequest.TicketData();
        ticket.setCodigoQR("TKT123");
        ticket.setEstadoUso("NO_UTILIZADA");
        request.setTickets(Collections.singletonList(ticket));

        // Mock genérico para la generación del contenido HTML (simula que es exitoso)
        when(emailService.buildEmailContent(any(), any(), any(), any())).thenReturn("<html>...content...</html>");
        
        // Mock: Simular que el repositorio retorna la Notificacion con ID después de guardar
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion saved = invocation.getArgument(0);
            if (saved.getIdNotificacion() == null) {
                 saved.setIdNotificacion(1L); // Asignar un ID simulado
            }
            return saved;
        });
    }

    // ----------------------------------------------------------------------------------
    // Test de Éxito (Envío completo)
    // ----------------------------------------------------------------------------------

    @Test
    void testProcesarEnvioEntradas_Exito() throws Exception {
        // Arrange
        // Simular que el envío de correo es exitoso (no lanza excepción)
        doNothing().when(emailService).enviarEntradas(any(EnvioEntradasRequest.class));

        // Captor para verificar el argumento de la SEGUNDA llamada a save (el estado final)
        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);

        // Act
        notificacionService.procesarEnvioEntradas(request);

        // Assert
        // 1. Verificar que se intentó guardar la notificación inicial (PENDIENTE) y la final (ENVIADO)
        verify(notificacionRepository, times(2)).save(notificacionCaptor.capture()); 
        
        // 2. Verificar que se llamó al servicio de envío de correo
        verify(emailService, times(1)).enviarEntradas(request);
        
        // 3. Capturar el objeto de la ÚLTIMA llamada a save (que debe ser el estado final)
        List<Notificacion> allSaves = notificacionCaptor.getAllValues();
        Notificacion notificacionFinal = allSaves.get(allSaves.size() - 1); // Último objeto guardado

        assertEquals(EstadoEnvio.ENVIADO, notificacionFinal.getEstadoEnvio(), 
                     "El estado final de la notificación debe ser ENVIADO.");
    }

    // ----------------------------------------------------------------------------------
    // Test de Fallo (MessagingException)
    // ----------------------------------------------------------------------------------

    @Test
    void testProcesarEnvioEntradas_Fallo_MessagingException() throws Exception {
        // Arrange
        // Simular que el envío de correo falla con la excepción esperada
        doThrow(new MessagingException("Error SMTP")).when(emailService).enviarEntradas(any(EnvioEntradasRequest.class));

        // Captor para verificar el argumento de la SEGUNDA llamada a save (el estado final)
        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);

        // Act & Assert
        // Debe relanzar como RuntimeException para que Feign lo capture
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            notificacionService.procesarEnvioEntradas(request)
        );

        assertTrue(exception.getMessage().contains("Fallo al enviar el correo: Error SMTP"), 
                   "El mensaje de la excepción debe indicar el fallo de envío.");
        
        // 1. Verificar que se intentó guardar la notificación (PENDIENTE y FALLIDO)
        verify(notificacionRepository, times(2)).save(notificacionCaptor.capture()); 
        
        // 2. Verificar que el estado final guardado es FALLIDO
        List<Notificacion> allSaves = notificacionCaptor.getAllValues();
        Notificacion notificacionFinal = allSaves.get(allSaves.size() - 1); // Último objeto guardado

        assertEquals(EstadoEnvio.FALLIDO, notificacionFinal.getEstadoEnvio(), 
                     "El estado final de la notificación debe ser FALLIDO.");
    }
}