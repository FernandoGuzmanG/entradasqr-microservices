package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.client.NotificacionClient;
import com.microservice.ticketing.dto.EnvioEntradasRequest;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.dto.InvitadoBulkRequest;
import com.microservice.ticketing.dto.InvitadoRequest;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitadoServiceTest {

    @Mock
    private InvitadoRepository invitadoRepository;
    @Mock
    private EntradaEmitidaRepository entradaEmitidaRepository;
    @Mock
    private TipoEntradaRepository tipoEntradaRepository;

    // CORRECCIÓN: TipoEntradaService debe ser un Mock, no un @Spy/@InjectMocks
    @Mock
    private TipoEntradaService tipoEntradaService;

    @Mock
    private NotificacionClient notificacionClient;
    @Mock
    private EventoClient eventoClient;

    // Clase bajo prueba, donde se inyectarán todos los Mocks
    @InjectMocks
    private InvitadoService invitadoService;

    // --- Datos de Prueba Fijos ---
    private final Long OWNER_ID = 1L;
    private final Long STAFF_ID = 2L;
    private final Long UNAUTHORIZED_ID = 3L;
    private final Long TIPO_ENTRADA_ID = 100L;
    private final Long EVENTO_ID = 10L;
    private final Long INVITADO_ID = 500L;
    // Contador para generar IDs únicos en los tests
    private final AtomicLong uniqueIdCounter = new AtomicLong(INVITADO_ID);


    private InvitadoRequest invitadoRequest;
    private Invitado invitadoPendiente;
    private Invitado invitadoEnviado;
    private TipoEntrada tipoEntrada;
    private EventoOwnerDTO eventoOwnerDTO;

    @BeforeEach
    void setUp() {
        // Inicialización de DTOs y Modelos
        invitadoRequest = new InvitadoRequest(TIPO_ENTRADA_ID, "Juan Pérez", "juan@test.com", 2);

        tipoEntrada = new TipoEntrada();
        tipoEntrada.setIdTipoEntrada(TIPO_ENTRADA_ID);
        tipoEntrada.setIdEvento(EVENTO_ID);
        tipoEntrada.setCantidadTotal(100);
        tipoEntrada.setCantidadEmitida(10);
        tipoEntrada.setNombre("General");

        invitadoPendiente = Invitado.builder()
                .idInvitado(INVITADO_ID)
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .nombreCompleto("Juan Pérez")
                .correo("juan@test.com")
                .cantidad(2)
                .estadoEnvio(Invitado.EstadoEnvio.PENDIENTE)
                .build();

        invitadoEnviado = Invitado.builder()
                .idInvitado(INVITADO_ID + 1)
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .nombreCompleto("Maria Lopez")
                .correo("maria@test.com")
                .cantidad(3)
                .estadoEnvio(Invitado.EstadoEnvio.ENVIADO)
                .build();

        eventoOwnerDTO = new EventoOwnerDTO(OWNER_ID, "Conferencia Tech");

        // Configuración de Mocks genéricos (usando lenient para evitar UnnecessaryStubbingException)
        Mockito.lenient().doReturn(tipoEntrada).when(tipoEntradaService).findById(TIPO_ENTRADA_ID);
        Mockito.lenient().when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);

        // Simular validación de propiedad exitosa (para Owner)
        Mockito.lenient().doNothing().when(tipoEntradaService).validarPropiedadEvento(any(), eq(OWNER_ID));
    }

    /**
     * Helper para simular el éxito o fallo de la validación de permisos de Staff/Owner.
     */
    private void simularPermisoStaff(Long usuarioId, boolean tienePermiso) {
        if (!tienePermiso) {
            // Simular fallo de permiso
            doThrow(new SecurityException("Acceso denegado.")).when(tipoEntradaService)
                    .validarPermisoStaff(eq(TIPO_ENTRADA_ID), eq(usuarioId), any());
        } else {
            // Simular éxito de permiso
            doNothing().when(tipoEntradaService)
                    .validarPermisoStaff(eq(TIPO_ENTRADA_ID), eq(usuarioId), any());
        }
    }

    // ----------------------------------------------------------------------------------
    // Tests de Creación (POST /api/invitados)
    // ----------------------------------------------------------------------------------

    @Test
    void testCrearInvitado_Exito() {
        simularPermisoStaff(STAFF_ID, true);
        when(invitadoRepository.save(any(Invitado.class))).thenAnswer(i -> {
            Invitado saved = i.getArgument(0);
            saved.setIdInvitado(uniqueIdCounter.incrementAndGet()); // Genera un ID Long válido
            return saved;
        });

        Invitado result = invitadoService.crearInvitado(invitadoRequest, STAFF_ID);

        assertNotNull(result);
        assertEquals(2, result.getCantidad());
        assertEquals(Invitado.EstadoEnvio.PENDIENTE, result.getEstadoEnvio());
        verify(invitadoRepository, times(1)).save(any(Invitado.class));
    }

    @Test
    void testCrearInvitado_Fallo_CantidadInvalida() {
        invitadoRequest = new InvitadoRequest(TIPO_ENTRADA_ID, "Juan Pérez", "juan@test.com", 0);
        simularPermisoStaff(OWNER_ID, true);

        assertThrows(RuntimeException.class, () ->
                invitadoService.crearInvitado(invitadoRequest, OWNER_ID)
        );
        verify(invitadoRepository, never()).save(any());
    }

    @Test
    void testCrearInvitado_Fallo_SinPermisos() {
        simularPermisoStaff(UNAUTHORIZED_ID, false);

        assertThrows(SecurityException.class, () ->
                invitadoService.crearInvitado(invitadoRequest, UNAUTHORIZED_ID)
        );
        verify(invitadoRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Modificación de Datos (PUT /api/invitados/{id})
    // ----------------------------------------------------------------------------------

    @Test
    void testModificarInvitado_Exito() {
        InvitadoRequest modificacionRequest = new InvitadoRequest(TIPO_ENTRADA_ID, "Juan Pérez Modificado", "nuevo@correo.com", 2);
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        when(invitadoRepository.save(any(Invitado.class))).thenAnswer(i -> i.getArguments()[0]);
        simularPermisoStaff(STAFF_ID, true);

        Invitado result = invitadoService.modificarInvitado(INVITADO_ID, STAFF_ID, modificacionRequest);

        assertEquals("Juan Pérez Modificado", result.getNombreCompleto());
        assertEquals("nuevo@correo.com", result.getCorreo());
    }

    @Test
    void testModificarInvitado_Fallo_InvitadoNoEncontrado() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                invitadoService.modificarInvitado(INVITADO_ID, STAFF_ID, invitadoRequest)
        );
    }

    // ----------------------------------------------------------------------------------
    // Tests de Modificación de Cantidad (PUT /api/invitados/{id}/cantidad)
    // ----------------------------------------------------------------------------------

    @Test
    void testModificarCantidadEntradas_Exito() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        when(invitadoRepository.save(any(Invitado.class))).thenAnswer(i -> i.getArguments()[0]);
        simularPermisoStaff(STAFF_ID, true);

        Invitado result = invitadoService.modificarCantidadEntradas(INVITADO_ID, STAFF_ID, 5);

        assertEquals(5, result.getCantidad());
    }

    @Test
    void testModificarCantidadEntradas_Fallo_EntradasYaEmitidas() {
        when(invitadoRepository.findById(INVITADO_ID + 1)).thenReturn(Optional.of(invitadoEnviado));
        simularPermisoStaff(STAFF_ID, true);

        assertThrows(RuntimeException.class, () ->
                invitadoService.modificarCantidadEntradas(INVITADO_ID + 1, STAFF_ID, 5)
        );
        verify(invitadoRepository, never()).save(any());
    }

    @Test
    void testModificarCantidadEntradas_Fallo_CantidadInvalida() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        simularPermisoStaff(STAFF_ID, true);

        assertThrows(RuntimeException.class, () ->
                invitadoService.modificarCantidadEntradas(INVITADO_ID, STAFF_ID, -1)
        );
        verify(invitadoRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Eliminación (DELETE /api/invitados/{id})
    // ----------------------------------------------------------------------------------

    @Test
    void testEliminarInvitado_Exito_TicketsNoEmitidos() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        when(entradaEmitidaRepository.findAllByIdInvitado(INVITADO_ID)).thenReturn(Collections.emptyList());
        simularPermisoStaff(OWNER_ID, true);

        invitadoService.eliminarInvitado(INVITADO_ID, OWNER_ID);

        verify(entradaEmitidaRepository, times(1)).deleteAll(any());
        verify(tipoEntradaRepository, never()).save(any()); // No se toca el stock
        verify(invitadoRepository, times(1)).delete(invitadoPendiente);
    }

    @Test
    void testEliminarInvitado_Exito_TicketsEmitidos_RestauraStock() {
        when(invitadoRepository.findById(INVITADO_ID + 1)).thenReturn(Optional.of(invitadoEnviado));
        when(entradaEmitidaRepository.findAllByIdInvitado(invitadoEnviado.getIdInvitado())).thenReturn(Collections.singletonList(new EntradaEmitida()));
        simularPermisoStaff(OWNER_ID, true);

        invitadoService.eliminarInvitado(invitadoEnviado.getIdInvitado(), OWNER_ID);

        // Stock: 10 (inicial) - 3 (cantidad del invitado) = 7
        assertEquals(7, tipoEntrada.getCantidadEmitida());
        verify(tipoEntradaRepository, times(1)).save(tipoEntrada); // Se debe restaurar el stock
        verify(entradaEmitidaRepository, times(1)).deleteAll(any());
        verify(invitadoRepository, times(1)).delete(invitadoEnviado);
    }

    @Test
    void testEliminarInvitado_Fallo_SinPermisos() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        simularPermisoStaff(UNAUTHORIZED_ID, false);

        assertThrows(SecurityException.class, () ->
                invitadoService.eliminarInvitado(INVITADO_ID, UNAUTHORIZED_ID)
        );
        verify(invitadoRepository, never()).delete(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Emisión (POST /api/invitados/emitir/{id})
    // ----------------------------------------------------------------------------------

    @Test
    void testEmitirEntradasPorId_Exito_PrimeraEmision() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        // Mock del cliente de notificación para simular éxito
        doNothing().when(notificacionClient).enviarEntradas(any(EnvioEntradasRequest.class));
        when(invitadoRepository.save(any(Invitado.class))).thenAnswer(i -> i.getArguments()[0]);
        // Mock para simular que no hay entradas emitidas previamente
        when(entradaEmitidaRepository.findAllByIdInvitado(any())).thenReturn(Collections.emptyList());
        // AÑADIDO: Mock para simular la actualización del stock
        when(tipoEntradaRepository.save(any(TipoEntrada.class))).thenAnswer(invocation -> {
            TipoEntrada savedTipoEntrada = invocation.getArgument(0);
            tipoEntrada.setCantidadEmitida(savedTipoEntrada.getCantidadEmitida());
            return savedTipoEntrada;
        });

        Invitado result = invitadoService.emitirEntradasPorId(INVITADO_ID, OWNER_ID);

        // Verificaciones
        assertEquals(Invitado.EstadoEnvio.ENVIADO, result.getEstadoEnvio());
        assertEquals(12, tipoEntrada.getCantidadEmitida()); // Stock actualizado: 10 + 2 = 12
        verify(tipoEntradaRepository, times(1)).save(tipoEntrada);
        verify(entradaEmitidaRepository, times(1)).saveAll(any()); // 2 entradas creadas
        verify(notificacionClient, times(1)).enviarEntradas(any(EnvioEntradasRequest.class));
    }

    @Test
    void testEmitirEntradasPorId_Fallo_YaEmitido() {
        when(invitadoRepository.findById(INVITADO_ID + 1)).thenReturn(Optional.of(invitadoEnviado));

        // La validación de propiedad debe pasar para llegar a este punto
        doNothing().when(tipoEntradaService).validarPropiedadEvento(any(), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () ->
                invitadoService.emitirEntradasPorId(INVITADO_ID + 1, OWNER_ID)
        );
        verify(notificacionClient, never()).enviarEntradas(any());
        verify(tipoEntradaRepository, never()).save(any());
    }

    @Test
    void testEmitirEntradasPorId_Fallo_SinPropiedad() {
        // Simular que el usuario no es el Owner (Validación Falla)
        doThrow(new SecurityException("Acceso denegado.")).when(tipoEntradaService)
                .validarPropiedadEvento(any(), eq(UNAUTHORIZED_ID));
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));

        assertThrows(SecurityException.class, () ->
                invitadoService.emitirEntradasPorId(INVITADO_ID, UNAUTHORIZED_ID)
        );
        verify(notificacionClient, never()).enviarEntradas(any());
    }

    @Test
    void testEmitirEntradasPorId_Fallo_StockInsuficiente() {
        // Configurar tipoEntrada para fallar la validación de stock (10 emitidas, 100 total. Solicitamos 92)
        invitadoPendiente.setCantidad(92);
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));

        // La validación de propiedad debe pasar para llegar a la validación de stock
        doNothing().when(tipoEntradaService).validarPropiedadEvento(any(), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () ->
                invitadoService.emitirEntradasPorId(INVITADO_ID, OWNER_ID)
        );
        verify(tipoEntradaRepository, never()).save(any());
        verify(notificacionClient, never()).enviarEntradas(any());
    }

    @Test
    void testEmitirEntradasPorId_Fallo_Notificacion() {
        when(invitadoRepository.findById(INVITADO_ID)).thenReturn(Optional.of(invitadoPendiente));
        // Mock del cliente de notificación para simular fallo
        doThrow(new RuntimeException("Error de conexión")).when(notificacionClient).enviarEntradas(any(EnvioEntradasRequest.class));
        when(invitadoRepository.save(any(Invitado.class))).thenAnswer(i -> i.getArguments()[0]);
        when(entradaEmitidaRepository.findAllByIdInvitado(any())).thenReturn(Collections.emptyList());
        // AÑADIDO: Mock para simular la actualización del stock
        when(tipoEntradaRepository.save(any(TipoEntrada.class))).thenAnswer(invocation -> {
            TipoEntrada savedTipoEntrada = invocation.getArgument(0);
            tipoEntrada.setCantidadEmitida(savedTipoEntrada.getCantidadEmitida());
            return savedTipoEntrada;
        });

        Invitado result = invitadoService.emitirEntradasPorId(INVITADO_ID, OWNER_ID);

        // Verificaciones
        assertEquals(Invitado.EstadoEnvio.ERROR_ENVIO, result.getEstadoEnvio());
        assertEquals(12, tipoEntrada.getCantidadEmitida()); // El stock SÍ se actualiza
        verify(tipoEntradaRepository, times(1)).save(tipoEntrada);
        verify(entradaEmitidaRepository, times(1)).saveAll(any());
        verify(notificacionClient, times(1)).enviarEntradas(any(EnvioEntradasRequest.class));
    }

    @Test
    void testEmitirEntradasMasivas_Fallo_StockInsuficienteTotal() {
        // Invitados que sumarán 91 tickets (20 + 71)
        InvitadoRequest req1 = new InvitadoRequest(TIPO_ENTRADA_ID, "Bulk User 1", "b1@test.com", 20);
        InvitadoRequest req2 = new InvitadoRequest(TIPO_ENTRADA_ID, "Bulk User 2", "b2@test.com", 71);
        InvitadoBulkRequest bulkRequest = new InvitadoBulkRequest(TIPO_ENTRADA_ID, List.of(req1, req2));

        // 10 emitidas + 91 solicitadas = 101 > 100 total
        assertThrows(RuntimeException.class, () ->
                invitadoService.emitirEntradasMasivas(bulkRequest, OWNER_ID)
        );
        verify(invitadoRepository, never()).save(any());
        verify(tipoEntradaRepository, never()).save(any());
    }

    @Test
    void testEmitirEntradasMasivas_Fallo_SinPropiedad() {
        InvitadoBulkRequest bulkRequest = new InvitadoBulkRequest(TIPO_ENTRADA_ID, Collections.emptyList());

        // Simular que el usuario no es el Owner (Validación Falla)
        doThrow(new SecurityException("Acceso denegado.")).when(tipoEntradaService)
                .validarPropiedadEvento(any(), eq(UNAUTHORIZED_ID));

        assertThrows(SecurityException.class, () ->
                invitadoService.emitirEntradasMasivas(bulkRequest, UNAUTHORIZED_ID)
        );
    }
}