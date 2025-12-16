package com.microservice.comunicaciones.controller;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import com.microservice.comunicaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Comunicaciones y Notificaciones", description = "Gestión del envío de correos electrónicos transaccionales.")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping("/enviar-entradas")
    @Operation(
            summary = "Enviar Entradas por Correo (Uso Feign Client)",
            description = "Recibe la solicitud del microservicio de Ticketing para generar y enviar los códigos QR de las entradas por correo electrónico al invitado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud de envío procesada con éxito."),
                    @ApiResponse(responseCode = "400", description = "No se proporcionaron entradas o datos de solicitud inválidos."),
                    @ApiResponse(responseCode = "500", description = "Fallo en el envío (ej: error de conexión SMTP, error interno del servicio).")
            }
    )
    public ResponseEntity<String> enviarEntradas(@RequestBody EnvioEntradasRequest request) {

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            return new ResponseEntity<String>("No se proporcionaron entradas para enviar.", HttpStatus.BAD_REQUEST);
        }

        try {
            notificacionService.procesarEnvioEntradas(request);

            return ResponseEntity.ok("Solicitud de envío procesada con éxito.");
        } catch (Exception e) {
            return new ResponseEntity<String>("Fallo en el envío: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
        }
    }
}