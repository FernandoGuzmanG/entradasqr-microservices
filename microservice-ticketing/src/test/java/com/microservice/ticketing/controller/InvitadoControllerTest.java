package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.InvitadoBulkRequest;
import com.microservice.ticketing.dto.InvitadoRequest;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.service.InvitadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitadoControllerTest {

    @Mock
    private InvitadoService invitadoService;

    @InjectMocks
    private InvitadoController invitadoController;

    private final Long OWNER_ID = 1L;
    private final Long STAFF_ID = 10L;
    private final Long INVITADO_ID = 5L;
    private final Long TIPO_ENTRADA_ID = 101L;

    private InvitadoRequest invitadoRequest;
    private Invitado invitadoMock;
    private InvitadoBulkRequest bulkRequest;

    @BeforeEach
    void setUp() {
        // Mock de datos para la solicitud
        invitadoRequest = new InvitadoRequest();
        invitadoRequest.setNombreCompleto("Carlos Ríos");
        invitadoRequest.setCorreo("carlos.rios@test.com");
        invitadoRequest.setCantidad(2);
        invitadoRequest.setIdTipoEntrada(TIPO_ENTRADA_ID);

        // Mock de datos para la respuesta del servicio
        invitadoMock = new Invitado();
        invitadoMock.setIdInvitado(INVITADO_ID);
        invitadoMock.setNombreCompleto(invitadoRequest.getNombreCompleto());
        invitadoMock.setEstadoEnvio(Invitado.EstadoEnvio.PENDIENTE);

        // Mock de datos para la solicitud masiva
        bulkRequest = new InvitadoBulkRequest();
        bulkRequest.setInvitados(Collections.singletonList(invitadoRequest));
    }

    // ----------------------------------------------------------------------------------
    // 1. Endpoints de GESTIÓN (CRUD)
    // ----------------------------------------------------------------------------------

    @Test
    void testCrearInvitado_Exito_201() {
        when(invitadoService.crearInvitado(any(InvitadoRequest.class), eq(STAFF_ID))).thenReturn(invitadoMock);

        ResponseEntity<Invitado> response = invitadoController.crearInvitado(invitadoRequest, STAFF_ID);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(invitadoMock, response.getBody());
        verify(invitadoService, times(1)).crearInvitado(invitadoRequest, STAFF_ID);
    }

    @Test
    void testCrearInvitado_Fallo_SinPermisos_403() {
        doThrow(new SecurityException("Acceso denegado. Permiso requerido: registrar_invitados"))
                .when(invitadoService).crearInvitado(any(InvitadoRequest.class), eq(STAFF_ID));

        ResponseEntity<Invitado> response = invitadoController.crearInvitado(invitadoRequest, STAFF_ID);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acceso denegado. Permiso requerido: registrar_invitados", response.getBody());
    }

    @Test
    void testCrearInvitado_Fallo_BadRequest_400() {
        doThrow(new IllegalArgumentException("Cantidad de entradas debe ser positiva"))
                .when(invitadoService).crearInvitado(any(InvitadoRequest.class), eq(STAFF_ID));

        ResponseEntity<Invitado> response = invitadoController.crearInvitado(invitadoRequest, STAFF_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cantidad de entradas debe ser positiva", response.getBody());
    }

    @Test
    void testModificarInvitado_Exito_200() {
        when(invitadoService.modificarInvitado(eq(INVITADO_ID), eq(STAFF_ID), any(InvitadoRequest.class))).thenReturn(invitadoMock);

        ResponseEntity<Invitado> response = invitadoController.modificarInvitado(INVITADO_ID, invitadoRequest, STAFF_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitadoMock, response.getBody());
        verify(invitadoService, times(1)).modificarInvitado(INVITADO_ID, STAFF_ID, invitadoRequest);
    }

    @Test
    void testModificarInvitado_Fallo_InvitadoNoEncontrado_404() {
        doThrow(new RuntimeException("Invitado no encontrado con ID: " + INVITADO_ID))
                .when(invitadoService).modificarInvitado(eq(INVITADO_ID), eq(STAFF_ID), any(InvitadoRequest.class));

        ResponseEntity<Invitado> response = invitadoController.modificarInvitado(INVITADO_ID, invitadoRequest, STAFF_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Invitado no encontrado con ID: " + INVITADO_ID, response.getBody());
    }

    @Test
    void testModificarCantidad_Exito_200() {
        int newQuantity = 5;
        when(invitadoService.modificarCantidadEntradas(eq(INVITADO_ID), eq(STAFF_ID), eq(newQuantity))).thenReturn(invitadoMock);

        ResponseEntity<Invitado> response = invitadoController.modificarCantidad(INVITADO_ID, newQuantity, STAFF_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitadoMock, response.getBody());
        verify(invitadoService, times(1)).modificarCantidadEntradas(INVITADO_ID, STAFF_ID, newQuantity);
    }

    @Test
    void testModificarCantidad_Fallo_BusinessRule_400() {
        int newQuantity = 5;
        doThrow(new RuntimeException("No se puede modificar la cantidad porque las entradas ya fueron emitidas."))
                .when(invitadoService).modificarCantidadEntradas(eq(INVITADO_ID), eq(STAFF_ID), eq(newQuantity));

        ResponseEntity<Invitado> response = invitadoController.modificarCantidad(INVITADO_ID, newQuantity, STAFF_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No se puede modificar la cantidad porque las entradas ya fueron emitidas.", response.getBody());
    }

    @Test
    void testListarInvitadosPorTipoEntrada_Exito_200() {
        List<Invitado> invitados = Collections.singletonList(invitadoMock);
        when(invitadoService.buscarInvitadosPorTipoEntrada(eq(TIPO_ENTRADA_ID))).thenReturn(invitados);

        ResponseEntity<List<Invitado>> response = invitadoController.listarInvitadosPorTipoEntrada(TIPO_ENTRADA_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitados, response.getBody());
    }

    @Test
    void testListarInvitadosPorTipoEntrada_NoContent_204() {
        when(invitadoService.buscarInvitadosPorTipoEntrada(eq(TIPO_ENTRADA_ID))).thenReturn(Collections.emptyList());

        ResponseEntity<List<Invitado>> response = invitadoController.listarInvitadosPorTipoEntrada(TIPO_ENTRADA_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testEliminarInvitado_Exito_204() {
        doNothing().when(invitadoService).eliminarInvitado(eq(INVITADO_ID), eq(OWNER_ID));

        ResponseEntity<Void> response = invitadoController.eliminarInvitado(INVITADO_ID, OWNER_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(invitadoService, times(1)).eliminarInvitado(INVITADO_ID, OWNER_ID);
    }

    @Test
    void testEliminarInvitado_Fallo_InvitadoNoEncontrado_404() {
        doThrow(new RuntimeException("Invitado no encontrado para eliminar."))
                .when(invitadoService).eliminarInvitado(eq(INVITADO_ID), eq(OWNER_ID));

        ResponseEntity<Void> response = invitadoController.eliminarInvitado(INVITADO_ID, OWNER_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Invitado no encontrado para eliminar.", response.getBody());
    }

    // ----------------------------------------------------------------------------------
    // 2. Endpoints de EMISIÓN (Exclusivo OWNER)
    // ----------------------------------------------------------------------------------

    @Test
    void testEmitirEntradasRegistradas_Exito_200() {
        invitadoMock.setEstadoEnvio(Invitado.EstadoEnvio.ENVIADO);
        when(invitadoService.emitirEntradasPorId(eq(INVITADO_ID), eq(OWNER_ID))).thenReturn(invitadoMock);

        ResponseEntity<Invitado> response = invitadoController.emitirEntradasRegistradas(INVITADO_ID, OWNER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitadoMock, response.getBody());
        verify(invitadoService, times(1)).emitirEntradasPorId(INVITADO_ID, OWNER_ID);
    }

    @Test
    void testEmitirEntradasRegistradas_Fallo_NoOwner_403() {
        doThrow(new SecurityException("Solo el Owner puede emitir tickets."))
                .when(invitadoService).emitirEntradasPorId(eq(INVITADO_ID), eq(STAFF_ID));

        ResponseEntity<Invitado> response = invitadoController.emitirEntradasRegistradas(INVITADO_ID, STAFF_ID);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Emisión denegada. Solo el Owner puede emitir tickets.", response.getBody());
    }

    @Test
    void testEmitirEntradasRegistradas_Fallo_BusinessRule_400() {
        doThrow(new RuntimeException("Las entradas ya fueron emitidas para este invitado."))
                .when(invitadoService).emitirEntradasPorId(eq(INVITADO_ID), eq(OWNER_ID));

        ResponseEntity<Invitado> response = invitadoController.emitirEntradasRegistradas(INVITADO_ID, OWNER_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Las entradas ya fueron emitidas para este invitado.", response.getBody());
    }

    @Test
    void testEmitirEntradasMasivas_Exito_201() {
        List<Invitado> invitadosEmitidos = Collections.singletonList(invitadoMock);
        when(invitadoService.emitirEntradasMasivas(any(InvitadoBulkRequest.class), eq(OWNER_ID))).thenReturn(invitadosEmitidos);

        ResponseEntity<List<Invitado>> response = invitadoController.emitirEntradasMasivas(bulkRequest, OWNER_ID);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(invitadosEmitidos, response.getBody());
        verify(invitadoService, times(1)).emitirEntradasMasivas(bulkRequest, OWNER_ID);
    }

    @Test
    void testEmitirEntradasMasivas_Fallo_StockInsuficiente_400() {
        doThrow(new RuntimeException("Stock insuficiente para el tipo de entrada."))
                .when(invitadoService).emitirEntradasMasivas(any(InvitadoBulkRequest.class), eq(OWNER_ID));

        ResponseEntity<List<Invitado>> response = invitadoController.emitirEntradasMasivas(bulkRequest, OWNER_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Stock insuficiente para el tipo de entrada.", response.getBody());
    }
}