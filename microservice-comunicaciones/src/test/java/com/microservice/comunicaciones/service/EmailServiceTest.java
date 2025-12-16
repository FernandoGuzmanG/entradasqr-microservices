package com.microservice.comunicaciones.service;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    // Mock del componente de Spring para enviar correos
    @Mock
    private JavaMailSender mailSender;

    // Mock para MimeMessage
    @Mock
    private MimeMessage mimeMessage;
    
    // Usamos @Spy para EmailService para poder simular generateQRCodeImage
    @InjectMocks
    @Spy
    private EmailService emailService;

    // --- Datos de Prueba ---
    private EnvioEntradasRequest request;
    private final String CORREO_DESTINO = "invitado@example.com";
    private final String NOMBRE_EVENTO = "Conferencia Anual";
    private final String CODIGO_QR_1 = "TKT-12345";
    private final byte[] DUMMY_QR_BYTES = new byte[]{1, 2, 3, 4};

    @BeforeEach
    void setUp() {
        // --- 1. Configurar Solicitud de Entrada (Request) ---
        EnvioEntradasRequest.TicketData ticket1 = new EnvioEntradasRequest.TicketData();
        ticket1.setCodigoQR(CODIGO_QR_1);
        ticket1.setEstadoUso("NO_UTILIZADA");

        request = new EnvioEntradasRequest();
        request.setCorreoDestino(CORREO_DESTINO);
        request.setNombreEvento(NOMBRE_EVENTO);
        request.setNombreInvitado("John Doe");
        request.setNombreTipoEntrada("VIP");
        request.setTickets(Collections.singletonList(ticket1));
        
        // --- 2. Mockear JavaMailSender ---
        // Usamos lenient para evitar UnnecessaryStubbingException en tests que no usan MimeMessage
        Mockito.lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // --- 3. Spy del método generateQRCodeImage ---
        // Usamos lenient para evitar UnnecessaryStubbingException si el método no es llamado
        try {
            // Nota: Se asume que el método generateQRCodeImage fue cambiado a 'protected' o 'package private' en EmailService.java.
            Mockito.lenient().doReturn(DUMMY_QR_BYTES).when(emailService).generateQRCodeImage(anyString(), anyInt(), anyInt());
        } catch (Exception e) {
            fail("Fallo al mockear generateQRCodeImage", e);
        }
        
        // Mocking: Configuramos el MimeMessage para que devuelva un valor cuando se le pida el Subject/Recipients
        // aunque esta verificación es mejor hacerla a nivel de MimeMessageHelper o en la verificación final.
    }

    // ----------------------------------------------------------------------------------
    // Test de Envío (enviarEntradas)
    // ----------------------------------------------------------------------------------

    @Test
    void testBuildEmailContent_VerificaHTML() {
        // Arrange
        List<EnvioEntradasRequest.TicketData> tickets = request.getTickets();

        // Act
        String htmlContent = emailService.buildEmailContent("Juan Test", NOMBRE_EVENTO, "VIP", tickets);

        // Assert
        assertNotNull(htmlContent);
        assertTrue(htmlContent.contains("Juan Test"), "Debe contener el nombre del invitado.");
        assertTrue(htmlContent.contains(NOMBRE_EVENTO), "Debe contener el nombre del evento.");
        assertTrue(htmlContent.contains("Entrada Tipo: VIP"), "Debe contener el tipo de entrada.");
        assertTrue(htmlContent.contains("Código de Acceso: <b>" + CODIGO_QR_1), "Debe contener el código QR.");
        assertTrue(htmlContent.contains("<html>"), "Debe ser HTML válido.");
    }
}