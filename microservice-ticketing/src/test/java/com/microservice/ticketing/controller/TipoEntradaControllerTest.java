package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.TipoEntradaRequest;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.service.TipoEntradaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoEntradaControllerTest {

    @Mock
    private TipoEntradaService tipoEntradaService;

    @InjectMocks
    private TipoEntradaController tipoEntradaController;

    private final Long OWNER_ID = 1L;
    private final Long OTHER_USER_ID = 99L;
    private final Long TIPO_ENTRADA_ID = 20L;
    private final Long EVENTO_ID = 100L;

    private TipoEntradaRequest tipoEntradaRequest;
    private TipoEntrada tipoEntradaMock;

    @BeforeEach
    void setUp() {
        // --- 1. Configuración de TipoEntradaRequest (DTO de entrada) ---
        tipoEntradaRequest = new TipoEntradaRequest();
        tipoEntradaRequest.setIdEvento(EVENTO_ID);
        tipoEntradaRequest.setNombre("Entrada VIP");
        tipoEntradaRequest.setDescripcion("Incluye acceso a área lounge y barra libre.");
        tipoEntradaRequest.setPrecio(new BigDecimal("150.00"));
        tipoEntradaRequest.setCantidadTotal(100);
        tipoEntradaRequest.setFechaInicioVenta(LocalDateTime.of(2024, 10, 1, 0, 0));
        tipoEntradaRequest.setFechaFinVenta(LocalDateTime.of(2024, 11, 1, 23, 59));

        // --- 2. Configuración del Mock de respuesta (TipoEntrada - Modelo) ---
        tipoEntradaMock = new TipoEntrada();
        tipoEntradaMock.setIdTipoEntrada(TIPO_ENTRADA_ID); 
        tipoEntradaMock.setNombre(tipoEntradaRequest.getNombre());
        tipoEntradaMock.setPrecio(tipoEntradaRequest.getPrecio());
        tipoEntradaMock.setCantidadTotal(tipoEntradaRequest.getCantidadTotal());
        tipoEntradaMock.setCantidadEmitida(0);
        tipoEntradaMock.setIdEvento(EVENTO_ID);
        tipoEntradaMock.setEstado(TipoEntrada.EstadoTipoEntrada.ACTIVO);
    }

    // ----------------------------------------------------------------------------------
    // 1. POST /api/tipos-entrada (Creación)
    // ----------------------------------------------------------------------------------

    @Test
    void testCrearTipoEntrada_Exito_201() {
        when(tipoEntradaService.crearTipoEntrada(any(TipoEntradaRequest.class), eq(OWNER_ID))).thenReturn(tipoEntradaMock);

        ResponseEntity<TipoEntrada> response = tipoEntradaController.crearTipoEntrada(tipoEntradaRequest, OWNER_ID);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tipoEntradaMock, response.getBody());
        verify(tipoEntradaService, times(1)).crearTipoEntrada(tipoEntradaRequest, OWNER_ID);
    }

    @Test
    void testCrearTipoEntrada_Fallo_NoOwner_403() {
        doThrow(new SecurityException("Acceso denegado."))
                .when(tipoEntradaService).crearTipoEntrada(any(TipoEntradaRequest.class), eq(OTHER_USER_ID));

        assertThrows(SecurityException.class, () -> 
            tipoEntradaController.crearTipoEntrada(tipoEntradaRequest, OTHER_USER_ID)
        );
        // La SecurityException es propagada y mapeada a 403 por el GlobalExceptionHandler
    }

    @Test
    void testCrearTipoEntrada_Fallo_BadRequest_400() {
        doThrow(new IllegalArgumentException("Error de validación de datos."))
                .when(tipoEntradaService).crearTipoEntrada(any(TipoEntradaRequest.class), eq(OWNER_ID));

        assertThrows(IllegalArgumentException.class, () -> 
            tipoEntradaController.crearTipoEntrada(tipoEntradaRequest, OWNER_ID)
        );
        // La IllegalArgumentException es propagada y mapeada a 400
    }

    // ----------------------------------------------------------------------------------
    // 2. GET /api/tipos-entrada/{idTipoEntrada} (Buscar por ID)
    // ----------------------------------------------------------------------------------

    @Test
    void testGetTipoEntradaById_Exito_200() {
        when(tipoEntradaService.findById(eq(TIPO_ENTRADA_ID))).thenReturn(tipoEntradaMock);

        ResponseEntity<TipoEntrada> response = tipoEntradaController.getTipoEntradaById(TIPO_ENTRADA_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tipoEntradaMock, response.getBody());
        verify(tipoEntradaService, times(1)).findById(TIPO_ENTRADA_ID);
    }

    @Test
    void testGetTipoEntradaById_Fallo_NotFound_404() {
        doThrow(new RuntimeException("Tipo de entrada no encontrado.")).when(tipoEntradaService).findById(eq(TIPO_ENTRADA_ID));

        assertThrows(RuntimeException.class, () -> 
            tipoEntradaController.getTipoEntradaById(TIPO_ENTRADA_ID)
        );
        // La RuntimeException es propagada y mapeada a 404
    }

    // ----------------------------------------------------------------------------------
    // 3. GET /api/tipos-entrada/evento/{idEvento} (Buscar por Evento)
    // ----------------------------------------------------------------------------------

    @Test
    void testBuscarTiposPorEvento_Exito_200() {
        List<TipoEntrada> tipos = Collections.singletonList(tipoEntradaMock);
        when(tipoEntradaService.buscarTiposPorEvento(eq(EVENTO_ID))).thenReturn(tipos);

        ResponseEntity<List<TipoEntrada>> response = tipoEntradaController.buscarTiposPorEvento(EVENTO_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tipos, response.getBody());
        verify(tipoEntradaService, times(1)).buscarTiposPorEvento(EVENTO_ID);
    }

    @Test
    void testBuscarTiposPorEvento_NoContent_204() {
        when(tipoEntradaService.buscarTiposPorEvento(eq(EVENTO_ID))).thenReturn(Collections.emptyList());

        ResponseEntity<List<TipoEntrada>> response = tipoEntradaController.buscarTiposPorEvento(EVENTO_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ----------------------------------------------------------------------------------
    // 4. GET /api/tipos-entrada/buscar?nombre={nombre} (Buscar por Nombre)
    // ----------------------------------------------------------------------------------

    @Test
    void testBuscarTiposPorNombre_Exito_200() {
        String nombre = "VIP";
        List<TipoEntrada> tipos = Collections.singletonList(tipoEntradaMock);
        when(tipoEntradaService.buscarTiposPorNombre(eq(nombre))).thenReturn(tipos);

        ResponseEntity<List<TipoEntrada>> response = tipoEntradaController.buscarTiposPorNombre(nombre);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tipos, response.getBody());
        verify(tipoEntradaService, times(1)).buscarTiposPorNombre(nombre);
    }

    @Test
    void testBuscarTiposPorNombre_NoContent_204() {
        String nombre = "Inexistente";
        when(tipoEntradaService.buscarTiposPorNombre(eq(nombre))).thenReturn(Collections.emptyList());

        ResponseEntity<List<TipoEntrada>> response = tipoEntradaController.buscarTiposPorNombre(nombre);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ----------------------------------------------------------------------------------
    // 5. PUT /api/tipos-entrada/{idTipoEntrada} (Actualizar)
    // ----------------------------------------------------------------------------------

    @Test
    void testActualizarTipoEntrada_Exito_200() {
        tipoEntradaRequest.setNombre("VIP Actualizada");
        when(tipoEntradaService.actualizarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OWNER_ID), any(TipoEntradaRequest.class))).thenReturn(tipoEntradaMock);

        ResponseEntity<TipoEntrada> response = tipoEntradaController.actualizarTipoEntrada(TIPO_ENTRADA_ID, tipoEntradaRequest, OWNER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tipoEntradaMock, response.getBody());
        verify(tipoEntradaService, times(1)).actualizarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID, tipoEntradaRequest);
    }

    @Test
    void testActualizarTipoEntrada_Fallo_NoOwner_403() {
        doThrow(new SecurityException("Acceso denegado."))
                .when(tipoEntradaService).actualizarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OTHER_USER_ID), any(TipoEntradaRequest.class));

        assertThrows(SecurityException.class, () -> 
            tipoEntradaController.actualizarTipoEntrada(TIPO_ENTRADA_ID, tipoEntradaRequest, OTHER_USER_ID)
        );
        // La SecurityException es propagada y mapeada a 403.
    }

    @Test
    void testActualizarTipoEntrada_Fallo_NotFound_404() {
        doThrow(new RuntimeException("Tipo de entrada no encontrado."))
                .when(tipoEntradaService).actualizarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OWNER_ID), any(TipoEntradaRequest.class));

        assertThrows(RuntimeException.class, () -> 
            tipoEntradaController.actualizarTipoEntrada(TIPO_ENTRADA_ID, tipoEntradaRequest, OWNER_ID)
        );
        // La RuntimeException es propagada y mapeada a 404.
    }

    // ----------------------------------------------------------------------------------
    // 6. DELETE /api/tipos-entrada/{idTipoEntrada} (Eliminar)
    // ----------------------------------------------------------------------------------

    @Test
    void testEliminarTipoEntrada_Exito_204() {
        doNothing().when(tipoEntradaService).eliminarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OWNER_ID));

        ResponseEntity<Void> response = tipoEntradaController.eliminarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tipoEntradaService, times(1)).eliminarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID);
    }

    @Test
    void testEliminarTipoEntrada_Fallo_NoOwner_403() {
        doThrow(new SecurityException("Acceso denegado."))
                .when(tipoEntradaService).eliminarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OTHER_USER_ID));

        assertThrows(SecurityException.class, () -> 
            tipoEntradaController.eliminarTipoEntrada(TIPO_ENTRADA_ID, OTHER_USER_ID)
        );
        // La SecurityException es propagada y mapeada a 403.
    }

    @Test
    void testEliminarTipoEntrada_Fallo_NotFound_404() {
        doThrow(new RuntimeException("Tipo de entrada no encontrado."))
                .when(tipoEntradaService).eliminarTipoEntrada(eq(TIPO_ENTRADA_ID), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () -> 
            tipoEntradaController.eliminarTipoEntrada(TIPO_ENTRADA_ID, OWNER_ID)
        );
        // La RuntimeException es propagada y mapeada a 404.
    }
}