package com.microservice.eventos.client;

import com.microservice.eventos.dto.UsuarioDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-usuarios", path = "/api/usuarios")
public interface UsuarioClient {

    @GetMapping("/id/{id}")
    UsuarioDto getUsuarioById(@PathVariable("id") Long id);

    @GetMapping("/correo/{correo}")
    UsuarioDto getUsuarioByCorreo(@PathVariable("correo") String correo);
}