package com.microservice.eventos.service;

import com.microservice.eventos.client.UsuarioClient;
import com.microservice.eventos.dto.UsuarioDto;
import com.microservice.eventos.model.Evento;
import com.microservice.eventos.model.Evento.EstadoEvento;
import com.microservice.eventos.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Permite usar anotaciones de Mockito, como @Mock y @InjectMocks
@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    // Mockea las dependencias de la clase que estamos probando
    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioClient usuarioFeignClient;

    // Inyecta los mocks en la instancia real de EventoService
    @InjectMocks
    private EventoService eventoService;

    private Evento eventoPrueba;
    private Long ownerId = 100L;
    private UsuarioDto ownerDto;

    @BeforeEach
    void setUp() {
        // Inicialización de datos de prueba
        eventoPrueba = Evento.builder()
                .idEvento(1L)
                .ownerId(ownerId)
                .nombre("Test Event")
                .estado(EstadoEvento.Publicado)
                .fecha(LocalDate.now().plusDays(5))
                .horaTermino(LocalTime.of(18, 0))
                .build();

        ownerDto = new UsuarioDto();
        ownerDto.setIdUsuario(ownerId);
        ownerDto.setEstado("Activo"); // Usuario activo
    }

    @Test
    void crearEvento_exitoso_yEstadoPublicado() {
        // Arrange
        // Simulamos que el cliente Feign retorna el DTO del Owner
        when(usuarioFeignClient.getUsuarioById(ownerId)).thenReturn(ownerDto);
        // Simulamos que el repositorio guarda y retorna el evento
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoPrueba);

        // Act
        Evento resultado = eventoService.crearEvento(eventoPrueba, ownerId);

        // Assert
        // Verificamos que se llamó al Feign Client
        verify(usuarioFeignClient, times(1)).getUsuarioById(ownerId);
        // Verificamos que se llamó al método save del repositorio
        verify(eventoRepository, times(1)).save(eventoPrueba);
        // Verificamos que el evento retornado tiene el estado correcto
        assertEquals(EstadoEvento.Publicado, resultado.getEstado(), "El estado debe ser Publicado");
    }

    @Test
    void cancelarEvento_porOwner_debeCambiarEstadoACancelado() {
        // Arrange
        // Simulamos que encontramos el evento y que el Owner es el correcto
        when(eventoRepository.findById(eventoPrueba.getIdEvento())).thenReturn(Optional.of(eventoPrueba));

        // El repositorio simula guardar la entidad actualizada
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoPrueba);

        // Act
        Evento eventoCancelado = eventoService.cancelarEvento(eventoPrueba.getIdEvento(), ownerId);

        // Assert
        // Verificamos que se actualizó el estado a CANCELADO antes de guardar
        assertEquals(EstadoEvento.Cancelado, eventoCancelado.getEstado(), "El estado debe ser Cancelado");
        // Verificamos que se llamó a save()
        verify(eventoRepository, times(1)).save(eventoPrueba);
    }

    @Test
    void cancelarEvento_porUsuarioNoOwner_debeLanzarSecurityException() {
        // Arrange
        Long otroUsuarioId = 999L;
        when(eventoRepository.findById(eventoPrueba.getIdEvento())).thenReturn(Optional.of(eventoPrueba));

        // Act & Assert
        // Verificamos que al intentar cancelar con un ID diferente al Owner, se lanza SecurityException
        assertThrows(SecurityException.class, () -> {
            eventoService.cancelarEvento(eventoPrueba.getIdEvento(), otroUsuarioId);
        });

        // Verificamos que save() nunca se llamó
        verify(eventoRepository, never()).save(any(Evento.class));
    }
}