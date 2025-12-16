package com.microservice.usuarios.controller;

import com.microservice.usuarios.dto.ChangePasswordRequest;
import com.microservice.usuarios.dto.LoginRequest;
import com.microservice.usuarios.dto.LoginResponse; // Importar el nuevo DTO
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

    // --- HELPER para manejar ID faltante ---
    private void checkUserId(Long id) {
        if (id == null) {
            // Se lanza SecurityException, que será mapeada a 401/403 por GlobalExceptionHandler.
            throw new SecurityException("ID de usuario (X-User-ID) requerido para esta operación.");
        }
    }

    @Operation(
            summary = "Registrar Nuevo Usuario",
            description = "Crea una nueva cuenta de usuario en el sistema. Retorna el perfil básico del usuario creado.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito."),
                    @ApiResponse(responseCode = "409", description = "El correo o RUT ya están registrados."),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos.")
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
                    // Actualizado: Referencia a LoginResponse en la documentación
                    @ApiResponse(responseCode = "200", description = "Login exitoso. Retorna JWT.",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas (correo o contraseña)."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @PostMapping("/login")
    // Actualizado: Cambio de String a LoginResponse
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response = usuarioService.login(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(response);
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

    // Endpoint de perfil (GET)
    @Operation(
            summary = "Obtener Perfil de Usuario (App)",
            description = "Retorna la información del perfil del usuario (ID, nombres, correo, etc.) mapeada a un DTO de respuesta. Ideal para la pantalla de Perfil en la App.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil encontrado."),
                    @ApiResponse(responseCode = "401", description = "Token o ID de usuario faltante."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> getProfile(
            @Parameter(description = "ID del usuario inyectado por el Gateway.", required = true)
            @RequestHeader(value = "X-User-ID", required = false) Long id) {

        // Propagamos la excepción si ID es nulo
        checkUserId(id);

        UsuarioResponse profile = usuarioService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    // Endpoint de actualización de perfil (PUT)
    @Operation(
            summary = "Actualizar Perfil de Usuario",
            description = "Permite actualizar datos básicos del perfil (RUT, nombres, apellidos, teléfono). Los campos nulos en la petición se ignoran.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito."),
                    @ApiResponse(responseCode = "401", description = "Token o ID de usuario faltante."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos.")
            }
    )
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> updateProfile(
            @Parameter(description = "ID del usuario inyectado por el Gateway.", required = true)
            @RequestHeader(value = "X-User-ID", required = false) Long id,
            @RequestBody UsuarioUpdateRequest request) {

        // Propagamos la excepción si ID es nulo
        checkUserId(id);

        UsuarioResponse updatedProfile = usuarioService.updateProfile(id, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @Operation(
            summary = "Cambiar Contraseña",
            description = "Permite al usuario cambiar su contraseña actual validando la anterior y confirmando la nueva.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña actualizada con éxito."),
                    @ApiResponse(responseCode = "401", description = "Token o ID de usuario faltante."),
                    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta, contraseñas nuevas no coinciden o formato inválido."),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
            }
    )
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID del usuario inyectado por el Gateway.", required = true)
            @RequestHeader(value = "X-User-ID", required = false) Long id,
            @RequestBody ChangePasswordRequest request) {

        // Propagamos la excepción si ID es nulo
        checkUserId(id);

        // La lógica de negocio propaga las excepciones 400 y 404
        usuarioService.changePassword(id, request);
        
        return ResponseEntity.ok().build();
    }
}