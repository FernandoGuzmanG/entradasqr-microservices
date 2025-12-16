package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.EmisionMasivaResponse;
import com.microservice.ticketing.dto.InvitadoRequest;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.model.Invitado.EstadoEnvio;
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
    private List<Invitado> invitadosMockList;

    @BeforeEach
    void setUp() {
        // Mock de datos para la solicitud
        invitadoRequest = new InvitadoRequest();
        invitadoRequest.setNombreCompleto("Carlos Ríos");
        invitadoRequest.setCorreo("carlos.rios@test.com");
        invitadoRequest.setCantidad(2);
        invitadoRequest.setIdTipoEntrada(TIPO_ENTRADA_ID);

        // Mock de datos para la respuesta del servicio
        invitadoMock = Invitado.builder()
                .idInvitado(INVITADO_ID)
                .nombreCompleto(invitadoRequest.getNombreCompleto())
                .correo(invitadoRequest.getCorreo())
                .cantidad(invitadoRequest.getCantidad())
                .estadoEnvio(EstadoEnvio.PENDIENTE)
                .build();
        
        invitadosMockList = Collections.singletonList(invitadoMock);

    }

    // ----------------------------------------------------------------------------------
    // 1. Endpoints de BÚSQUEDA y FILTRADO
    // ----------------------------------------------------------------------------------

    @Test
    void testFiltrarInvitados_Exito_200() {
        when(invitadoService.filtrarInvitados(eq(TIPO_ENTRADA_ID), eq("Carlos"), eq("DESC"))).thenReturn(invitadosMockList);

        ResponseEntity<List<Invitado>> response = invitadoController.filtrarInvitados(TIPO_ENTRADA_ID, "Carlos", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(invitadoService, times(1)).filtrarInvitados(TIPO_ENTRADA_ID, "Carlos", "DESC");
    }

    // ----------------------------------------------------------------------------------
    // 2. Endpoints de GESTIÓN (CRUD)
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
        doThrow(new SecurityException("Acceso denegado. Permiso requerido."))
                .when(invitadoService).crearInvitado(any(InvitadoRequest.class), eq(STAFF_ID));

        assertThrows(SecurityException.class, () -> 
            invitadoController.crearInvitado(invitadoRequest, STAFF_ID)
        );
        // El GlobalExceptionHandler manejará esta SecurityException y la mapeará a 403.
    }

    @Test
    void testCrearInvitado_Fallo_BadRequest_400() {
        doThrow(new RuntimeException("La cantidad de entradas debe ser positiva"))
                .when(invitadoService).crearInvitado(any(InvitadoRequest.class), eq(STAFF_ID));

        assertThrows(RuntimeException.class, () -> 
            invitadoController.crearInvitado(invitadoRequest, STAFF_ID)
        );
        // El GlobalExceptionHandler manejará esta RuntimeException y la mapeará a 400.
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
        doThrow(new RuntimeException("Invitado no encontrado."))
                .when(invitadoService).modificarInvitado(eq(INVITADO_ID), eq(STAFF_ID), any(InvitadoRequest.class));

        assertThrows(RuntimeException.class, () ->
            invitadoController.modificarInvitado(INVITADO_ID, invitadoRequest, STAFF_ID)
        );
        // La RuntimeException será mapeada a 404 por el GlobalExceptionHandler si contiene "no encontrado".
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
        doThrow(new RuntimeException("No se puede modificar la cantidad de entradas ya emitidas."))
                .when(invitadoService).modificarCantidadEntradas(eq(INVITADO_ID), eq(STAFF_ID), eq(newQuantity));

        assertThrows(RuntimeException.class, () ->
            invitadoController.modificarCantidad(INVITADO_ID, newQuantity, STAFF_ID)
        );
        // La RuntimeException será mapeada a 400.
    }
    
    @Test
    void testListarInvitadosPorTipoEntrada_Exito_200() {
        when(invitadoService.buscarInvitadosPorTipoEntrada(eq(TIPO_ENTRADA_ID))).thenReturn(invitadosMockList);

        ResponseEntity<List<Invitado>> response = invitadoController.listarInvitadosPorTipoEntrada(TIPO_ENTRADA_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitadosMockList, response.getBody());
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
        doThrow(new RuntimeException("Invitado no encontrado."))
                .when(invitadoService).eliminarInvitado(eq(INVITADO_ID), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () ->
            invitadoController.eliminarInvitado(INVITADO_ID, OWNER_ID)
        );
        // La RuntimeException será mapeada a 404.
    }

    // ----------------------------------------------------------------------------------
    // 3. Endpoints de EMISIÓN (Exclusivo OWNER)
    // ----------------------------------------------------------------------------------

    @Test
    void testEmitirEntradasRegistradas_Exito_200() {
        invitadoMock.setEstadoEnvio(EstadoEnvio.ENVIADO);
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

        assertThrows(SecurityException.class, () -> 
            invitadoController.emitirEntradasRegistradas(INVITADO_ID, STAFF_ID)
        );
        // La SecurityException será mapeada a 403.
    }

    @Test
    void testEmitirEntradasRegistradas_Fallo_BusinessRule_400() {
        doThrow(new RuntimeException("Las entradas ya fueron emitidas para este invitado."))
                .when(invitadoService).emitirEntradasPorId(eq(INVITADO_ID), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () -> 
            invitadoController.emitirEntradasRegistradas(INVITADO_ID, OWNER_ID)
        );
        // La RuntimeException será mapeada a 400.
    }

    @Test
    void testEmitirEntradasMasivasPorTipo_Exito_200() {
        // Configurar el servicio para devolver una lista de invitados procesados
        when(invitadoService.emitirEntradasMasivas(eq(TIPO_ENTRADA_ID), eq(OWNER_ID))).thenReturn(invitadosMockList);

        invitadoMock.setEstadoEnvio(EstadoEnvio.ENVIADO);

        ResponseEntity<EmisionMasivaResponse> response = invitadoController.emitirEntradasMasivasPorTipo(TIPO_ENTRADA_ID, OWNER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verificamos que el cálculo en el controlador es correcto
        assertEquals(2, response.getBody().getTotalProcesados());
        assertEquals(2, response.getBody().getEnviadas());
        verify(invitadoService, times(1)).emitirEntradasMasivas(TIPO_ENTRADA_ID, OWNER_ID);
    }

    @Test
    void testEmitirEntradasMasivasPorTipo_NoContent_204() {
        when(invitadoService.emitirEntradasMasivas(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        ResponseEntity<EmisionMasivaResponse> response = invitadoController.emitirEntradasMasivasPorTipo(TIPO_ENTRADA_ID, OWNER_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void testEmitirEntradasMasivasPorTipo_Fallo_NoOwner_403() {
         doThrow(new SecurityException("Solo el Owner puede iniciar emisión masiva."))
                .when(invitadoService).emitirEntradasMasivas(eq(TIPO_ENTRADA_ID), eq(STAFF_ID));

        assertThrows(SecurityException.class, () -> 
            invitadoController.emitirEntradasMasivasPorTipo(TIPO_ENTRADA_ID, STAFF_ID)
        );
        // La SecurityException será mapeada a 403.
    }
    
    @Test
    void testEmitirEntradasMasivasPorTipo_Fallo_StockInsuficiente_400() {
        doThrow(new RuntimeException("Stock insuficiente para emitir a todos los invitados pendientes."))
                .when(invitadoService).emitirEntradasMasivas(eq(TIPO_ENTRADA_ID), eq(OWNER_ID));

        assertThrows(RuntimeException.class, () -> 
            invitadoController.emitirEntradasMasivasPorTipo(TIPO_ENTRADA_ID, OWNER_ID)
        );
        // La RuntimeException será mapeada a 400.
    }
}