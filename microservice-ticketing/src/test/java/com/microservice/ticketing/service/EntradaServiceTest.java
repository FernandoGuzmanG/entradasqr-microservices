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
    private EntradaService entradaService;

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
        tipoEntrada = new TipoEntrada();
        tipoEntrada.setIdTipoEntrada(TIPO_ENTRADA_ID);
        tipoEntrada.setIdEvento(EVENTO_ID);
        tipoEntrada.setNombre("General");

        invitado = new Invitado();
        invitado.setIdInvitado(INVITADO_ID);
        invitado.setNombreCompleto("Juan Test");
        invitado.setCorreo("juan@test.com");

        entradaNoUsada = new EntradaEmitida();
        entradaNoUsada.setIdEntrada(1L);
        entradaNoUsada.setCodigoQR(VALID_QR);
        entradaNoUsada.setIdTipoEntrada(TIPO_ENTRADA_ID);
        entradaNoUsada.setIdInvitado(INVITADO_ID);
        entradaNoUsada.setEstadoUso(EntradaEmitida.EstadoUso.NO_UTILIZADA);

        entradaUsada = new EntradaEmitida();
        entradaUsada.setIdEntrada(2L);
        entradaUsada.setCodigoQR(VALID_QR);
        entradaUsada.setIdTipoEntrada(TIPO_ENTRADA_ID);
        entradaUsada.setIdInvitado(INVITADO_ID);
        entradaUsada.setEstadoUso(EntradaEmitida.EstadoUso.UTILIZADA);
        entradaUsada.setFechaUso(LocalDateTime.now().minusHours(1));

        // DTO de Información del Evento
        eventoOwnerDTO = new EventoOwnerDTO(STAFF_ID_OWNER, "Evento de Prueba");
    }

    // --- TESTS DE CASOS DE ÉXITO ---

    @Test
    void testValidarYUsarEntrada_AccesoConcedido_StaffOwner() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        // El Owner no necesita el permiso específico
        when(eventoClient.staffTienePermiso(any(), any(), any())).thenReturn(false);
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitado));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(entradaEmitidaRepository.save(any(EntradaEmitida.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecución
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_OWNER, VALID_QR);

        // Verificación
        assertEquals("ACCESO CONCEDIDO.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        assertNotNull(response.getFechaUso());
        verify(entradaEmitidaRepository, times(1)).save(any(EntradaEmitida.class));
    }

    @Test
    void testValidarYUsarEntrada_AccesoConcedido_StaffPermitido() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        // El staff tiene el permiso específico
        when(eventoClient.staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_PERMITTED), eq("escanear_entrada"))).thenReturn(true);
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitado));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(entradaEmitidaRepository.save(any(EntradaEmitida.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecución
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, VALID_QR);

        // Verificación
        assertEquals("ACCESO CONCEDIDO.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        verify(entradaEmitidaRepository, times(1)).save(any(EntradaEmitida.class));
    }

    // --- TESTS DE CASOS DE FALLO (LÓGICA DE NEGOCIO) ---

    @Test
    void testValidarYUsarEntrada_Fallo_QRNoValido() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(INVALID_QR)).thenReturn(Optional.empty());

        // Ejecución y Verificación
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, INVALID_QR)
        );

        assertEquals("QR no válido o no encontrado.", exception.getMessage());
        verify(entradaEmitidaRepository, never()).save(any());
    }

    @Test
    void testValidarYUsarEntrada_Fallo_EntradaYaUtilizada() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        when(eventoClient.staffTienePermiso(any(), any(), any())).thenReturn(true); // Permisos dados para pasar la validación
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitado));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));


        // Ejecución
        CheckinResponse response = entradaService.validarYUsarEntrada(STAFF_ID_PERMITTED, VALID_QR);

        // Verificación
        assertEquals("Entrada ya utilizada. Acceso denegado.", response.getMensaje());
        assertEquals(EntradaEmitida.EstadoUso.UTILIZADA.name(), response.getEstadoUso());
        verify(entradaEmitidaRepository, never()).save(any()); // No se debe llamar a save
    }

    // --- TESTS DE CASOS DE FALLO (PERMISOS) ---

    @Test
    void testValidarYUsarEntrada_Fallo_StaffNoAutorizado() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        // El staff NO es el Owner y NO tiene el permiso
        when(eventoClient.staffTienePermiso(eq(EVENTO_ID), eq(STAFF_ID_UNAUTHORIZED), eq("escanear_entrada"))).thenReturn(false);

        // Ejecución y Verificación
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_UNAUTHORIZED, VALID_QR)
        );

        assertEquals("Acceso Denegado. El Staff no tiene permisos para escanear en este evento.", exception.getMessage());
        verify(entradaEmitidaRepository, never()).save(any());
    }

    // --- TESTS DE CASOS DE FALLO (INCONSISTENCIA DE DATOS) ---

    @Test
    void testValidarYUsarEntrada_Fallo_TipoEntradaFaltante() {
        // Mocks
        when(entradaEmitidaRepository.findByCodigoQR(VALID_QR)).thenReturn(Optional.of(entradaNoUsada));
        // Simula que el TipoEntrada asociado no existe (error de integridad)
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.empty());

        // Ejecución y Verificación
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                entradaService.validarYUsarEntrada(STAFF_ID_OWNER, VALID_QR)
        );

        assertEquals("Error interno: Tipo de entrada no asociado a la entrada.", exception.getMessage());
        verify(eventoClient, never()).getEventoOwnerById(any());
        verify(entradaEmitidaRepository, never()).save(any());
    }
}