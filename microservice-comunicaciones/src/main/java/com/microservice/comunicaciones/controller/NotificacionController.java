package com.microservice.comunicaciones.controller;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import com.microservice.comunicaciones.service.NotificacionService;
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
public class NotificacionController {

    private final NotificacionService notificacionService;

    // Endpoint de prueba que simula la recepción del mensaje del Broker/Ticketing
    @PostMapping("/enviar-entradas")
    public ResponseEntity<String> enviarEntradas(@RequestBody EnvioEntradasRequest request) {

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            return new ResponseEntity("No se proporcionaron entradas para enviar.", HttpStatus.BAD_REQUEST);
        }

        try {
            // Ejecutamos la lógica de envío
            notificacionService.procesarEnvioEntradas(request);

            return ResponseEntity.ok("Solicitud de envío procesada con éxito.");
        } catch (Exception e) {
            // Aquí vemos el error real de la conexión SMTP
            return new ResponseEntity("Fallo en el envío: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }
}