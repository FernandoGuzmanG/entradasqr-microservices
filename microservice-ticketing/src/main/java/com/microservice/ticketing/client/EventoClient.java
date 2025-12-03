package com.microservice.ticketing.client;

import com.microservice.ticketing.dto.EventoOwnerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microservice-eventos", path = "/api/eventos")
public interface EventoClient {

    @GetMapping("/{id}")
    EventoOwnerDTO getEventoOwnerById(@PathVariable("id") Long id);

    @GetMapping("/{idEvento}/permisos/check")
    Boolean staffTienePermiso(
            @PathVariable("idEvento") Long idEvento,
            @RequestParam("usuarioId") Long idUsuario,
            @RequestParam("permiso") String nombrePermiso
    );
}