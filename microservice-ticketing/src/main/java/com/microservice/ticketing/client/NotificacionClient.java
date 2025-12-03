package com.microservice.ticketing.client;

import com.microservice.ticketing.dto.EnvioEntradasRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


// Asumimos que el microservicio se llamará microservice-comunicaciones
@FeignClient(name = "microservice-comunicaciones", path = "/api/notificaciones")
public interface NotificacionClient {

    // DTOs y objetos deben ser compatibles entre servicios
    @PostMapping("/enviar-entradas")
    void enviarEntradas(@RequestBody EnvioEntradasRequest request);
}