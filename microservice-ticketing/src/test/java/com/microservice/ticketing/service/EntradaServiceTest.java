package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.dto.CheckinResponse;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.model.EntradaEmitida;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.repository.EntradaEmitidaRepository;
import com.microservice.ticketing.repository.InvitadoRepository;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito; // IMPORTACIÓN AÑADIDA

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntradaServiceTest {

    @Mock
    private EntradaEmitidaRepository entradaEmitidaRepository;
    @Mock
    private InvitadoRepository invitadoRepository;
    @Mock
    private TipoEntradaRepository tipoEntradaRepository;
    @Mock
    private EventoClient eventoClient;

    @InjectMocks
    private EntradaService entradaService; // Clase bajo prueba

    // --- Datos de Prueba ---
    private final Long EVENTO_ID = 10L;
    private final Long TIPO_ENTRADA_ID = 100L;
    private final Long INVITADO_ID = 500L;
    private final Long STAFF_ID_OWNER = 1L;
    private final Long STAFF_ID_PERMITTED = 2L;
    private final Long STAFF_ID_UNAUTHORIZED = 3L;
    private final String VALID_QR = "TKT-VALID-12345";
    private final String INVALID_QR = "TKT-INVALID-00000";

    private EntradaEmitida entradaNoUsada;
    private EntradaEmitida entradaUsada;
    private TipoEntrada tipoEntrada;
    private Invitado invitado;
    private EventoOwnerDTO eventoOwnerDTO;

    @BeforeEach
    void setUp() {
        // Configuración de Modelos
        tipoEntrada = TipoEntrada.builder()
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .idEvento(EVENTO_ID)
                .nombre("General")
                .build();

        invitado = Invitado.builder()
                .idInvitado(INVITADO_ID)
                .nombreCompleto("Juan Test")
                .correo("juan@test.com")
                .build();

        entradaNoUsada = EntradaEmitida.builder()
                .idEntrada(1L)
                .codigoQR(VALID_QR)
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .idInvitado(INVITADO_ID)
                .estadoUso(EntradaEmitida.EstadoUso.NO_UTILIZADA)
                .build();

        entradaUsada = EntradaEmitida.builder()
                .idEntrada(2L)
                .codigoQR(VALID_QR)
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .idInvitado(INVITADO_ID)
                .estadoUso(EntradaEmitida.EstadoUso.UTILIZADA)
                .fechaUso(LocalDateTime.now().minusHours(1))
                .build();

        // DTO de Información del Owner del Evento
        eventoOwnerDTO = new EventoOwnerDTO(STAFF_ID_OWNER, "Evento de Prueba");
        
        // Configuraciones generales para que la respuesta auxiliar buildResponse funcione
        // Se usa Mockito.lenient() para que estos mocks no fallen en tests donde no son llamados.
        Mockito.lenient().when(invitadoRepository.findById(anyLong())).thenReturn(Optional.of(invitado));
        Mockito.lenient().when(tipoEntradaRepository.findById(anyLong())).thenReturn(Optional.of(tipoEntrada));
    }

    // --- TESTS DE CASOS DE ÉXITO ---

    @Test
    void testValidarYUsarEntrada_AccesoConcedido_StaffOwner() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        
        // Simular que el repositorio guarda la entidad actualizada
        when(entradaEmitidaRepository.save(any(EntradaEmitida.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mock necesario para evitar errores de stubbing si se llama
        Mockito.lenient().when(eventoClient.staffTienePermiso(any(), any(), any())).thenReturn(true);


        // Act
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_OWNER, VALID_QR);

        // Assert
        assertEquals("ACCESO CONCEDIDO.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        assertNotNull(response.getFechaUso());
        
        // Verificación de la actualización en DB
        verify(entradaEmitidaRepository, times(1)).save(entradaNoUsada);
        // CORRECCIÓN: La llamada a staffTienePermiso siempre ocurre en el servicio,
        // por lo que solo verificamos que fue llamado, no usamos 'never()'.
        verify(eventoClient, times(1)).staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_OWNER), eq("escanear_entrada"));
    }

    @Test
    void testValidarYUsarEntrada_AccesoConcedido_StaffPermitido() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        
        // Simular que el staff tiene el permiso específico
        when(eventoClient.staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_PERMITTED), eq("escanear_entrada"))).thenReturn(true);
        when(entradaEmitidaRepository.save(any(EntradaEmitida.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, VALID_QR);

        // Assert
        assertEquals("ACCESO CONCEDIDO.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        
        // Verificación de la actualización en DB
        verify(entradaEmitidaRepository, times(1)).save(entradaNoUsada);
        // Verificación de la llamada al cliente Feign para chequear permiso
        verify(eventoClient, times(1)).staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_PERMITTED), eq("escanear_entrada"));
    }

    // --- TESTS DE CASOS DE FALLO (LÓGICA DE NEGOCIO) ---

    @Test
    void testValidarYUsarEntrada_Fallo_QRNoValido() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(INVALID_QR)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, INVALID_QR)
        );

        assertEquals("QR no válido o no encontrado.", exception.getMessage());
        verify(entradaEmitidaRepository, never()).save(any());
    }

    @Test
    void testValidarYUsarEntrada_Fallo_EntradaYaUtilizada() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        
        // El permiso se debe chequear para llegar al paso 4
        Mockito.lenient().when(eventoClient.staffTienePermiso(any(), any(), any())).thenReturn(true); 

        // Act
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, VALID_QR);

        // Assert
        assertEquals("Entrada ya utilizada. Acceso denegado.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        verify(entradaEmitidaRepository, never()).save(any()); // No se debe llamar a save
    }

    // --- TESTS DE CASOS DE FALLO (PERMISOS Y PROPIEDAD) ---

    @Test
    void testValidarYUsarEntrada_Fallo_StaffNoAutorizado() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        
        // Simular que el staff NO es el Owner y NO tiene el permiso
        when(eventoClient.staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_UNAUTHORIZED), eq("escanear_entrada"))).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_UNAUTHORIZED, VALID_QR)
        );

        assertEquals("Acceso Denegado. El Staff no tiene permisos para escanear en este evento.", exception.getMessage());
        verify(entradaEmitidaRepository, never()).save(any());
        verify(eventoClient, times(1)).staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_UNAUTHORIZED), eq("escanear_entrada"));
    }

    @Test
    void testValidarYUsarEntrada_Fallo_InconsistenciaTipoEntrada() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        // Simula que el TipoEntrada asociado no existe (error de integridad)
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_OWNER, VALID_QR)
        );

        assertEquals("Error interno: Tipo de entrada no asociado a la entrada.", exception.getMessage());
        verify(eventoClient, never()).getEventoOwnerById(any());
        verify(entradaEmitidaRepository, never()).save(any());
    }
}