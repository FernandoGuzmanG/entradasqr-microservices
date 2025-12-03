package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.CheckinResponse;
import com.microservice.ticketing.service.EntradaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EntradaControllerTest {

    @Mock
    private EntradaService entradaService;

    @InjectMocks
    private EntradaController entradaController;

    private final Long STAFF_ID = 10L;
    private final String VALID_QR = "ABCD123456";
    private CheckinResponse successfulResponse;

    @BeforeEach
    void setUp() {
        // Inicialización de la respuesta exitosa conforme al DTO CheckinResponse
        successfulResponse = new CheckinResponse();
        successfulResponse.setCodigoQR(VALID_QR);
        successfulResponse.setMensaje("ACCESO CONCEDIDO.");
        successfulResponse.setEstadoUso("UTILIZADA");
        successfulResponse.setFechaUso(LocalDateTime.now());
        successfulResponse.setNombreInvitado("Juan Pérez");
        successfulResponse.setCorreo("juan.perez@example.com");
        successfulResponse.setNombreTipoEntrada("Entrada General - Fase 1");
    }

    // ----------------------------------------------------------------------------------
    // Tests de Check-In Exitoso (200 OK)
    // ----------------------------------------------------------------------------------

    @Test
    void testCheckinEntrada_Exito() {
        // Configuración del mock para simular un check-in exitoso
        doReturn(successfulResponse).when(entradaService).validarYUsarEntrada(eq(STAFF_ID), eq(VALID_QR));

        // Ejecución del método del controlador
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, STAFF_ID);

        // Verificaciones
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        CheckinResponse actualResponse = (CheckinResponse) responseEntity.getBody();
        assertNotNull(actualResponse);

        // Verificación de los campos del DTO
        assertEquals(successfulResponse.getMensaje(), actualResponse.getMensaje());
        assertEquals(VALID_QR, actualResponse.getCodigoQR());
        assertEquals("UTILIZADA", actualResponse.getEstadoUso());
        assertEquals("Juan Pérez", actualResponse.getNombreInvitado());
        assertNotNull(actualResponse.getFechaUso());

        verify(entradaService, times(1)).validarYUsarEntrada(STAFF_ID, VALID_QR);
    }

    // ----------------------------------------------------------------------------------
    // Tests de Errores de Cabecera y Seguridad (401, 403)
    // ----------------------------------------------------------------------------------

    @Test
    void testCheckinEntrada_Fallo_StaffIdNulo_401() {
        // Ejecución con staffId nulo (como si no se enviara la cabecera)
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, null);

        // Verificaciones
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Se requiere ID de Staff para el check-in.", responseEntity.getBody());
        verify(entradaService, times(0)).validarYUsarEntrada(any(), any());
    }

    @Test
    void testCheckinEntrada_Fallo_SinPermisos_403() {
        // Configuración del mock para simular fallo de seguridad
        String forbiddenMessage = "Acceso Denegado: Staff no tiene el permiso 'escanear_entrada'.";
        doThrow(new RuntimeException(forbiddenMessage)).when(entradaService).validarYUsarEntrada(eq(STAFF_ID), eq(VALID_QR));

        // Ejecución
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, STAFF_ID);

        // Verificaciones
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()); // Mapeado a 403

        CheckinResponse errorResponse = (CheckinResponse) responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(forbiddenMessage, errorResponse.getMensaje());
        assertEquals(VALID_QR, errorResponse.getCodigoQR());
        verify(entradaService, times(1)).validarYUsarEntrada(STAFF_ID, VALID_QR);
    }

    // ----------------------------------------------------------------------------------
    // Tests de Errores de Negocio (400 Bad Request)
    // ----------------------------------------------------------------------------------

    @Test
    void testCheckinEntrada_Fallo_EntradaYaUsada_400() {
        // Configuración del mock para simular una entrada ya utilizada
        String businessErrorMessage = "La entrada con QR ABCD123456 ya ha sido utilizada.";
        doThrow(new RuntimeException(businessErrorMessage)).when(entradaService).validarYUsarEntrada(eq(STAFF_ID), eq(VALID_QR));

        // Ejecución
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, STAFF_ID);

        // Verificaciones
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()); // Mapeado a 400

        CheckinResponse errorResponse = (CheckinResponse) responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(businessErrorMessage, errorResponse.getMensaje());
        assertEquals(VALID_QR, errorResponse.getCodigoQR());
        verify(entradaService, times(1)).validarYUsarEntrada(STAFF_ID, VALID_QR);
    }

    @Test
    void testCheckinEntrada_Fallo_QRNoValido_400() {
        // Configuración del mock para simular un QR no encontrado
        String businessErrorMessage = "El código QR no corresponde a ninguna entrada activa.";
        doThrow(new RuntimeException(businessErrorMessage)).when(entradaService).validarYUsarEntrada(eq(STAFF_ID), eq(VALID_QR));

        // Ejecución
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, STAFF_ID);

        // Verificaciones
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()); // Mapeado a 400

        CheckinResponse errorResponse = (CheckinResponse) responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(businessErrorMessage, errorResponse.getMensaje());
        assertEquals(VALID_QR, errorResponse.getCodigoQR());
        verify(entradaService, times(1)).validarYUsarEntrada(STAFF_ID, VALID_QR);
    }

    @Test
    void testCheckinEntrada_Fallo_OtroError_400() {
        // Configuración del mock para simular cualquier otra RuntimeException
        String genericErrorMessage = "Error inesperado durante el check-in.";
        doThrow(new RuntimeException(genericErrorMessage)).when(entradaService).validarYUsarEntrada(eq(STAFF_ID), eq(VALID_QR));

        // Ejecución
        ResponseEntity<?> responseEntity = entradaController.checkinEntrada(VALID_QR, STAFF_ID);

        // Verificaciones
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()); // Mapeado a 400 por defecto

        CheckinResponse errorResponse = (CheckinResponse) responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(genericErrorMessage, errorResponse.getMensaje());
        assertEquals(VALID_QR, errorResponse.getCodigoQR());
        verify(entradaService, times(1)).validarYUsarEntrada(STAFF_ID, VALID_QR);
    }
}