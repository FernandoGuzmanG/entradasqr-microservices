package com.microservice.usuarios.controller;

import com.microservice.usuarios.dto.LoginRequest;
import com.microservice.usuarios.dto.UsuarioRegistroRequest;
import com.microservice.usuarios.dto.UsuarioResponse;
import com.microservice.usuarios.dto.UsuarioUpdateRequest;
import com.microservice.usuarios.model.Usuario;
import com.microservice.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de autenticación y perfiles de usuario.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Registrar Nuevo Usuario",
            description = "Crea una nueva cuenta de usuario en el sistema. Retorna el perfil básico del usuario creado.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito.",
                            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o correo/RUT ya registrado.")
            }
    )
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrar(
            @RequestBody UsuarioRegistroRequest request) {

        UsuarioResponse nuevoUsuario = usuarioService.registrarUsuario(request);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Autenticación de Usuario (Login)",
            description = "Valida las credenciales del usuario y retorna un token JWT para la sesión.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso. Retorna JWT.",
                            content = @Content(schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1Ni..."))),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas (correo o contraseña)."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request) {

        String token = usuarioService.login(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "Obtener Usuario por ID (Interno)",
            description = "Retorna la entidad completa del usuario por su ID. Este endpoint es principalmente para uso interno o de debug.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @GetMapping("/id/{id}")
    public ResponseEntity<Usuario> getUsuarioById(
            @Parameter(description = "ID del usuario.") @PathVariable("id") Long id) {

        Usuario usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Obtener Usuario por Correo (Interno)",
            description = "Retorna la entidad completa del usuario por su correo electrónico. Este endpoint es principalmente para uso interno o de debug.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @GetMapping("/correo/{correo}")
    public ResponseEntity<Usuario> getUsuarioByCorreo(
            @Parameter(description = "Correo del usuario.") @PathVariable("correo") String correo) {

        Usuario usuario = usuarioService.getUsuarioByCorreo(correo);
        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Obtener Perfil de Usuario (App)",
            description = "Retorna la información del perfil del usuario (ID, nombres, correo, etc.) mapeada a un DTO de respuesta. Ideal para la pantalla de Perfil en la App.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil encontrado.",
                            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (si la seguridad JWT no permite ver el perfil de otro).")
            }
    )
    @GetMapping("/perfil/{id}")
    public ResponseEntity<UsuarioResponse> getProfile(
            @Parameter(description = "ID del usuario cuyo perfil se solicita.") @PathVariable("id") Long id) {

        UsuarioResponse profile = usuarioService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Actualizar Perfil de Usuario",
            description = "Permite actualizar datos básicos del perfil (RUT, nombres, apellidos, teléfono). Los campos nulos en la petición se ignoran.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito.",
                            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado.")
            }
    )
    @PutMapping("/perfil/{id}")
    public ResponseEntity<UsuarioResponse> updateProfile(
            @Parameter(description = "ID del usuario a actualizar.") @PathVariable("id") Long id,
            @RequestBody UsuarioUpdateRequest request) {

        UsuarioResponse updatedProfile = usuarioService.updateProfile(id, request);
        return ResponseEntity.ok(updatedProfile);
    }
}