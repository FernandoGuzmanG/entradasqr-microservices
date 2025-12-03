package com.microservice.eventos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.eventos.model.Evento;
import com.microservice.eventos.model.Evento.EstadoEvento;
import com.microservice.eventos.service.EventoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Inicializa solo el EventoController y sus dependencias (las moficadas con @MockBean)
@WebMvcTest(EventoController.class)
public class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Se utiliza para convertir objetos Java a JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Inyecta una versión mockeada de EventoService
    @MockBean
    private EventoService eventoService;

    private Evento eventoPrueba;
    private Long ownerId = 100L;

    @BeforeEach
    void setUp() {
        eventoPrueba = Evento.builder()
                .idEvento(1L)
                .ownerId(ownerId)
                .nombre("Festival de Verano")
                .estado(EstadoEvento.Publicado)
                .fecha(LocalDate.now().plusMonths(1))
                .horaTermino(LocalTime.of(23, 0))
                .build();
    }

    @Test
    void crearEvento_debeRetornar201_yEventoCreado() throws Exception {
        // Arrange
        // Simulamos el comportamiento del service al crear un evento
        when(eventoService.crearEvento(any(Evento.class), eq(ownerId))).thenReturn(eventoPrueba);

        // Act & Assert
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Enviamos el ID del Owner en la cabecera
                        .header("X-User-ID", ownerId)
                        .content(objectMapper.writeValueAsString(eventoPrueba)))
                .andExpect(status().isCreated()) // Esperamos 201 Created
                .andExpect(jsonPath("$.idEvento").value(1L))
                .andExpect(jsonPath("$.estado").value("Publicado"));

        // Verificamos que el método del service fue llamado una vez
        verify(eventoService, times(1)).crearEvento(any(Evento.class), eq(ownerId));
    }

    @Test
    void cancelarEvento_exitoso_debeRetornar200() throws Exception {
        // Arrange
        Evento eventoCancelado = Evento.builder().idEvento(1L).estado(EstadoEvento.Cancelado).build();

        // Simulamos que el service cancela y retorna el evento actualizado
        when(eventoService.cancelarEvento(eq(1L), eq(ownerId))).thenReturn(eventoCancelado);

        // Act & Assert
        mockMvc.perform(put("/api/eventos/1/cancelar")
                        .header("X-User-ID", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(jsonPath("$.estado").value("Cancelado"));

        // Verificamos la interacción con el service
        verify(eventoService, times(1)).cancelarEvento(eq(1L), eq(ownerId));
    }

    @Test
    void cancelarEvento_permisosDenegados_debeRetornar403() throws Exception {
        // Arrange
        Long otroUsuarioId = 999L;
        // Simulamos que el service lanza una excepción de seguridad
        when(eventoService.cancelarEvento(eq(1L), eq(otroUsuarioId)))
                .thenThrow(new SecurityException("Usuario no autorizado para cancelar."));

        // Act & Assert
        mockMvc.perform(put("/api/eventos/1/cancelar")
                        .header("X-User-ID", otroUsuarioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Esperamos 403 Forbidden

        // Verificamos la interacción con el service
        verify(eventoService, times(1)).cancelarEvento(eq(1L), eq(otroUsuarioId));
    }
}