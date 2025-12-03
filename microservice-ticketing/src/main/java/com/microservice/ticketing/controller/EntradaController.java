package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.CheckinResponse;
import com.microservice.ticketing.service.EntradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entradas")
@RequiredArgsConstructor
@Tag(name = "Check-In y Validación de Entradas", description = "Operaciones de escaneo y validación de códigos QR en la puerta del evento.")
public class EntradaController {

    private final EntradaService entradaService;

    @PostMapping("/checkin/{codigoQR}")
    @Operation(summary = "Valida y marca el uso de un ticket (Check-In).",
            description = "Busca el ticket por código QR, verifica su estado y permisos del Staff, y marca el ticket como 'UTILIZADA'.")
    @ApiResponse(responseCode = "200", description = "Check-In exitoso. ACCESO CONCEDIDO.")
    @ApiResponse(responseCode = "401", description = "Falta el ID del Staff en la cabecera.")
    @ApiResponse(responseCode = "403", description = "El Staff no tiene el permiso 'escanear_entrada' para este evento.")
    @ApiResponse(responseCode = "400", description = "Error de negocio: QR no válido, o entrada ya utilizada.")
    public ResponseEntity<?> checkinEntrada(
            @Parameter(description = "Código QR único del ticket a validar.")
            @PathVariable String codigoQR,
            @Parameter(description = "ID del usuario Staff (o Owner) que realiza el escaneo.", required = true)
            @RequestHeader("X-User-ID") Long staffId) {

        if (staffId == null) {
            return new ResponseEntity<>("Se requiere ID de Staff para el check-in.", HttpStatus.UNAUTHORIZED);
        }

        try {
            CheckinResponse response = entradaService.validarYUsarEntrada(staffId, codigoQR);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            CheckinResponse errorResponse = new CheckinResponse();
            errorResponse.setMensaje(e.getMessage());
            errorResponse.setCodigoQR(codigoQR);

            // Manejo de errores de Seguridad (Permisos)
            if (e.getMessage().contains("Acceso Denegado")) {
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN); // 403
            }

            // Manejo de errores de negocio (QR no válido, ya usada, etc.)
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400
        }
    }
}