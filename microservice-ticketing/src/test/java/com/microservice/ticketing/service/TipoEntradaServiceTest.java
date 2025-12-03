package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.dto.TipoEntradaRequest;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.model.TipoEntrada.EstadoTipoEntrada;
import com.microservice.ticketing.repository.EntradaEmitidaRepository;
import com.microservice.ticketing.repository.InvitadoRepository;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoEntradaServiceTest {

    // Mocks de Repositorios y Clientes
    @Mock
    private TipoEntradaRepository tipoEntradaRepository;
    @Mock
    private InvitadoRepository invitadoRepository;
    @Mock
    private EntradaEmitidaRepository entradaEmitidaRepository;
    @Mock
    private EventoClient eventoClient;

    // Clase bajo prueba
    @InjectMocks
    private TipoEntradaService tipoEntradaService;

    // --- Datos de Prueba Fijos ---
    private final Long TIPO_ENTRADA_ID = 1L;
    private final Long EVENTO_ID = 100L;
    private final Long OWNER_ID = 50L;
    private final Long STAFF_ID = 51L;
    private final Long UNAUTHORIZED_ID = 52L;

    private TipoEntrada tipoEntrada;
    private TipoEntradaRequest tipoEntradaRequest;
    private EventoOwnerDTO eventoOwnerDTO;

    @BeforeEach
    void setUp() {
        // Objeto TipoEntrada de prueba
        tipoEntrada = TipoEntrada.builder()
                .idTipoEntrada(TIPO_ENTRADA_ID)
                .idEvento(EVENTO_ID)
                .nombre("Entrada VIP")
                .descripcion("Acceso exclusivo")
                .precio(BigDecimal.valueOf(50.00))
                .cantidadTotal(200)
                .cantidadEmitida(50)
                .fechaInicioVenta(LocalDateTime.now().minusDays(1))
                .fechaFinVenta(LocalDateTime.now().plusDays(5))
                .estado(EstadoTipoEntrada.ACTIVO)
                .build();

        // DTO de solicitud de prueba
        tipoEntradaRequest = new TipoEntradaRequest(
                EVENTO_ID,
                "Entrada General",
                "Acceso normal",
                BigDecimal.valueOf(25.00),
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5)
        );

        // DTO de información del Owner del Evento
        eventoOwnerDTO = new EventoOwnerDTO(OWNER_ID, "Conferencia Tech");
    }

    // ----------------------------------------------------------------------------------
    // Tests de Búsqueda y Utilidades
    // ----------------------------------------------------------------------------------

    @Test
    void testFindById_Exito() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));

        TipoEntrada result = tipoEntradaService.findById(TIPO_ENTRADA_ID);

        assertNotNull(result);
        assertEquals(TIPO_ENTRADA_ID, result.getIdTipoEntrada());
    }

    @Test
    void testFindById_NoEncontrado() {
        when(tipoEntradaRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tipoEntradaService.findById(999L)
        );

        assertEquals("Tipo de entrada no encontrado.", exception.getMessage());
    }

    @Test
    void testBuscarTiposPorEvento_Exito() {
        List<TipoEntrada> expectedList = List.of(tipoEntrada);
        when(tipoEntradaRepository.findAllByIdEvento(EVENTO_ID)).thenReturn(expectedList);

        List<TipoEntrada> result = tipoEntradaService.buscarTiposPorEvento(EVENTO_ID);

        assertEquals(1, result.size());
        assertEquals(TIPO_ENTRADA_ID, result.get(0).getIdTipoEntrada());
    }

    @Test
    void testBuscarTiposPorNombre_Exito() {
        List<TipoEntrada> expectedList = List.of(tipoEntrada);
        when(tipoEntradaRepository.findByNombreContainingIgnoreCase("vip")).thenReturn(expectedList);

        List<TipoEntrada> result = tipoEntradaService.buscarTiposPorNombre("vip");

        assertEquals(1, result.size());
        assertEquals("Entrada VIP", result.get(0).getNombre());
    }

    @Test
    void testBuscarTiposPorNombre_Vacio() {
        List<TipoEntrada> result = tipoEntradaService.buscarTiposPorNombre("");
        assertTrue(result.isEmpty());
        verify(tipoEntradaRepository, never()).findByNombreContainingIgnoreCase(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Validación de Seguridad
    // ----------------------------------------------------------------------------------

    @Test
    void testValidarPropiedadEvento_Exito_EsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Owner ID 50

        // No debería lanzar excepción
        assertDoesNotThrow(() ->
                tipoEntradaService.validarPropiedadEvento(TIPO_ENTRADA_ID, OWNER_ID)
        );
    }

    @Test
    void testValidarPropiedadEvento_Fallo_NoEsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Owner ID 50

        SecurityException exception = assertThrows(SecurityException.class, () ->
                tipoEntradaService.validarPropiedadEvento(TIPO_ENTRADA_ID, UNAUTHORIZED_ID) // Usuario ID 52
        );

        assertEquals("Acceso denegado: El usuario no es el propietario del evento.", exception.getMessage());
    }

    @Test
    void testValidarPermisoStaff_Exito_EsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Owner ID 50

        // El Owner siempre pasa, no se debe llamar a eventoClient.staffTienePermiso
        assertDoesNotThrow(() ->
                tipoEntradaService.validarPermisoStaff(TIPO_ENTRADA_ID, OWNER_ID, "registrar_invitados")
        );
        verify(eventoClient, never()).staffTienePermiso(anyLong(), anyLong(), any());
    }

    @Test
    void testValidarPermisoStaff_Exito_EsStaffConPermiso() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Owner ID 50
        when(eventoClient.staffTienePermiso(EVENTO_ID, STAFF_ID, "registrar_invitados")).thenReturn(true);

        // El Staff con ID 51 tiene permiso
        assertDoesNotThrow(() ->
                tipoEntradaService.validarPermisoStaff(TIPO_ENTRADA_ID, STAFF_ID, "registrar_invitados")
        );
        verify(eventoClient, times(1)).staffTienePermiso(EVENTO_ID, STAFF_ID, "registrar_invitados");
    }

    @Test
    void testValidarPermisoStaff_Fallo_NoTienePermiso() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Owner ID 50
        when(eventoClient.staffTienePermiso(EVENTO_ID, UNAUTHORIZED_ID, "registrar_invitados")).thenReturn(false);

        SecurityException exception = assertThrows(SecurityException.class, () ->
                tipoEntradaService.validarPermisoStaff(TIPO_ENTRADA_ID, UNAUTHORIZED_ID, "registrar_invitados")
        );

        assertEquals("Acceso denegado. El usuario no tiene el permiso requerido: registrar_invitados", exception.getMessage());
        verify(eventoClient, times(1)).staffTienePermiso(EVENTO_ID, UNAUTHORIZED_ID, "registrar_invitados");
    }

    // ----------------------------------------------------------------------------------
    // Tests de Creación (POST)
    // ----------------------------------------------------------------------------------

    @Test
    void testCrearTipoEntrada_Exito_EsOwner() {
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);
        when(tipoEntradaRepository.save(any(TipoEntrada.class))).thenAnswer(invocation -> {
            TipoEntrada saved = invocation.getArgument(0);
            saved.setIdTipoEntrada(TIPO_ENTRADA_ID); // Simular asignación de ID
            return saved;
        });

        TipoEntrada result = tipoEntradaService.crearTipoEntrada(tipoEntradaRequest, OWNER_ID);

        assertNotNull(result.getIdTipoEntrada());
        assertEquals(0, result.getCantidadEmitida());
        assertEquals(EstadoTipoEntrada.ACTIVO, result.getEstado());
        verify(tipoEntradaRepository, times(1)).save(any(TipoEntrada.class));
    }

    @Test
    void testCrearTipoEntrada_Fallo_NoEsOwner() {
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);

        SecurityException exception = assertThrows(SecurityException.class, () ->
                tipoEntradaService.crearTipoEntrada(tipoEntradaRequest, UNAUTHORIZED_ID)
        );

        assertEquals("Acceso denegado: La creación de Tipos de Entrada es exclusiva del propietario del evento.", exception.getMessage());
        verify(tipoEntradaRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Actualización (PUT)
    // ----------------------------------------------------------------------------------

    @Test
    void testActualizarTipoEntrada_Exito_EsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Validación de propiedad OK
        when(tipoEntradaRepository.save(any(TipoEntrada.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TipoEntradaRequest newRequest = new TipoEntradaRequest(
                EVENTO_ID,
                "Nombre Nuevo",
                "Desc Nueva",
                BigDecimal.valueOf(100.00),
                500,
                tipoEntradaRequest.getFechaInicioVenta(),
                tipoEntradaRequest.getFechaFinVenta()
        );

        TipoEntrada result = tipoEntradaService.actualizarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID, newRequest);

        assertEquals("Nombre Nuevo", result.getNombre());
        assertEquals(500, result.getCantidadTotal());
        // El campo 'cantidadEmitida' (50) NO debe cambiar
        assertEquals(50, result.getCantidadEmitida());
        verify(tipoEntradaRepository, times(1)).save(tipoEntrada);
    }

    @Test
    void testActualizarTipoEntrada_Fallo_NoEsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);

        SecurityException exception = assertThrows(SecurityException.class, () ->
                tipoEntradaService.actualizarTipoEntrada(TIPO_ENTRADA_ID, UNAUTHORIZED_ID, tipoEntradaRequest)
        );

        assertEquals("Acceso denegado: El usuario no es el propietario del evento.", exception.getMessage());
        verify(tipoEntradaRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------------------
    // Tests de Eliminación (DELETE)
    // ----------------------------------------------------------------------------------

    @Test
    void testEliminarTipoEntrada_Exito_EsOwner_CascadaManual() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO); // Validación de propiedad OK

        // Simular que existen datos asociados
        when(entradaEmitidaRepository.findAllByIdTipoEntrada(TIPO_ENTRADA_ID)).thenReturn(Collections.emptyList());
        when(invitadoRepository.findAllByIdTipoEntrada(TIPO_ENTRADA_ID)).thenReturn(Collections.emptyList());


        tipoEntradaService.eliminarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID);

        // Verificaciones de Cascada Manual
        verify(entradaEmitidaRepository, times(1)).findAllByIdTipoEntrada(TIPO_ENTRADA_ID);
        verify(entradaEmitidaRepository, times(1)).deleteAll(any());
        verify(invitadoRepository, times(1)).findAllByIdTipoEntrada(TIPO_ENTRADA_ID);
        verify(invitadoRepository, times(1)).deleteAll(any());

        // Verificación de Eliminación Final
        verify(tipoEntradaRepository, times(1)).deleteById(TIPO_ENTRADA_ID);
    }

    @Test
    void testEliminarTipoEntrada_Fallo_NoEsOwner() {
        when(tipoEntradaRepository.findById(TIPO_ENTRADA_ID)).thenReturn(Optional.of(tipoEntrada));
        when(eventoClient.getEventoOwnerById(EVENTO_ID)).thenReturn(eventoOwnerDTO);

        SecurityException exception = assertThrows(SecurityException.class, () ->
                tipoEntradaService.eliminarTipoEntrada(TIPO_ENTRADA_ID, UNAUTHORIZED_ID)
        );

        assertEquals("Acceso denegado: El usuario no es el propietario del evento.", exception.getMessage());
        verify(tipoEntradaRepository, never()).deleteById(anyLong());
    }
}