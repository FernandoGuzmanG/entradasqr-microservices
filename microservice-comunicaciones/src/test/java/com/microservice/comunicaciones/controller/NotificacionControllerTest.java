package com.microservice.comunicaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import com.microservice.comunicaciones.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
public class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService notificacionService;

    private EnvioEntradasRequest request;
    private EnvioEntradasRequest.TicketData validTicket;

    @BeforeEach
    void setUp() {
        // Ticket de prueba válido
        validTicket = new EnvioEntradasRequest.TicketData();
        validTicket.setCodigoQR("TKT-VALIDO-1");
        validTicket.setEstadoUso("PENDIENTE");

        // Request de prueba válido
        request = new EnvioEntradasRequest();
        request.setIdInvitado(1L);
        request.setCorreoDestino("test@mail.com");
        request.setNombreEvento("Evento Test");
        request.setNombreInvitado("Inv Test");
        request.setNombreTipoEntrada("General");
        request.setTickets(Collections.singletonList(validTicket));
    }

    // ----------------------------------------------------------------------------------
    // Test de Éxito (200 OK)
    // ----------------------------------------------------------------------------------

    @Test
    void enviarEntradas_exitoso_debeRetornar200() throws Exception {
        // Arrange
        // Simular que el servicio procesa la solicitud sin errores
        doNothing().when(notificacionService).procesarEnvioEntradas(any(EnvioEntradasRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/notificaciones/enviar-entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(content().string("Solicitud de envío procesada con éxito."));

        // Verificar que el método del service fue llamado una vez
        verify(notificacionService, times(1)).procesarEnvioEntradas(any(EnvioEntradasRequest.class));
    }

    // ----------------------------------------------------------------------------------
    // Test de Fallo (400 Bad Request)
    // ----------------------------------------------------------------------------------

    @Test
    void enviarEntradas_ticketsVacios_debeRetornar400() throws Exception {
        // Arrange
        // Configurar la solicitud con tickets vacíos
        request.setTickets(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/api/notificaciones/enviar-entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Esperamos 400 Bad Request
                .andExpect(content().string("No se proporcionaron entradas para enviar."));

        // Verificar que el servicio NUNCA fue llamado
        verify(notificacionService, never()).procesarEnvioEntradas(any());
    }

    @Test
    void enviarEntradas_ticketsNulos_debeRetornar400() throws Exception {
        // Arrange
        // Configurar la solicitud con tickets nulos
        request.setTickets(null);

        // Act & Assert
        mockMvc.perform(post("/api/notificaciones/enviar-entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Esperamos 400 Bad Request
                .andExpect(content().string("No se proporcionaron entradas para enviar."));

        // Verificar que el servicio NUNCA fue llamado
        verify(notificacionService, never()).procesarEnvioEntradas(any());
    }

    // ----------------------------------------------------------------------------------
    // Test de Fallo (500 Internal Server Error)
    // ----------------------------------------------------------------------------------

    @Test
    void enviarEntradas_falloInterno_debeRetornar500() throws Exception {
        // Arrange
        final String errorMessage = "Error de conexión con el servidor SMTP.";
        
        // Simular que el servicio lanza una excepción de runtime (que captura el catch)
        doThrow(new RuntimeException(errorMessage))
                .when(notificacionService).procesarEnvioEntradas(any(EnvioEntradasRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/notificaciones/enviar-entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()) // Esperamos 500 Internal Server Error
                .andExpect(content().string("Fallo en el envío: " + errorMessage));

        // Verificar que el servicio fue llamado una vez
        verify(notificacionService, times(1)).procesarEnvioEntradas(any(EnvioEntradasRequest.class));
    }
}