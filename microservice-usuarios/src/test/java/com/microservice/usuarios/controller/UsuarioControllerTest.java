package com.microservice.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.usuarios.dto.UsuarioRegistroRequest;
import com.microservice.usuarios.dto.UsuarioResponse;
import com.microservice.usuarios.dto.UsuarioUpdateRequest;
import com.microservice.usuarios.model.Usuario;
import com.microservice.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Inicializa el contexto de Spring solo con la capa web (Controller)
@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Se mockea el Service para aislar la capa Controller
    @MockBean
    private UsuarioService usuarioService;

    private Long userId = 1L;
    private UsuarioRegistroRequest registroRequest;
    private UsuarioUpdateRequest updateRequest;
    private UsuarioResponse usuarioResponse;
    private Usuario usuarioEntity;
    private final String CORREO_TEST = "test@mail.cl";
    private final String PASSWORD_TEST = "password123";

    @BeforeEach
    void setUp() {
        // Inicialización del DTO de registro
        registroRequest = new UsuarioRegistroRequest();
        registroRequest.setCorreo("nuevo@mail.cl");
        registroRequest.setPassword(PASSWORD_TEST);
        registroRequest.setNombres("Nuevo");

        // Inicialización del DTO de actualización
        updateRequest = new UsuarioUpdateRequest();
        updateRequest.setNombres("Test Updated");

        // Inicialización del DTO de respuesta (simulando el usuario ya creado)
        usuarioResponse = UsuarioResponse.builder()
                .idUsuario(userId)
                .correo(CORREO_TEST)
                .nombres("Test")
                .estado(Usuario.EstadoUsuario.Activo)
                .build();

        // Inicialización de la entidad Usuario (para endpoints internos)
        usuarioEntity = Usuario.builder()
                .idUsuario(userId)
                .correo(CORREO_TEST)
                .nombres("Test")
                .build();
    }

    // --- 1. POST /registrar ---
    @Test
    void registrarUsuario_debeRetornar201_yUsuarioResponse() throws Exception {
        // Arrange
        when(usuarioService.registrarUsuario(any(UsuarioRegistroRequest.class))).thenReturn(usuarioResponse);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.correo").value(CORREO_TEST));

        verify(usuarioService, times(1)).registrarUsuario(any(UsuarioRegistroRequest.class));
    }

    @Test
    void registrarUsuario_correoYaExiste_debeRetornar400() throws Exception {
        // Arrange
        when(usuarioService.registrarUsuario(any(UsuarioRegistroRequest.class)))
                .thenThrow(new IllegalArgumentException("El correo ya está registrado."));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isBadRequest()); // Espera 400
    }

    // --- 2. POST /login ---
    @Test
    void login_credencialesValidas_debeRetornar200_yToken() throws Exception {
        // Arrange
        final String token = "mocked.jwt.token";

        // Creamos el JSON del LoginRequest de forma manual (asumiendo su estructura)
        String loginJson = String.format("{\"correo\": \"%s\", \"password\": \"%s\"}", CORREO_TEST, PASSWORD_TEST);

        when(usuarioService.login(eq(CORREO_TEST), eq(PASSWORD_TEST))).thenReturn(token);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()) // Espera 200
                .andExpect(content().string(token)); // Verifica que el cuerpo es el token

        verify(usuarioService, times(1)).login(eq(CORREO_TEST), eq(PASSWORD_TEST));
    }

    @Test
    void login_credencialesInvalidas_debeRetornar400() throws Exception {
        // Arrange
        String loginJson = String.format("{\"correo\": \"%s\", \"password\": \"%s\"}", CORREO_TEST, PASSWORD_TEST);

        // Simulamos error de credenciales inválidas (IllegalArgumentException)
        when(usuarioService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas."));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest()); // Espera 400
    }

    @Test
    void login_usuarioNoEncontrado_debeRetornar404() throws Exception {
        // Arrange
        String loginJson = String.format("{\"correo\": \"%s\", \"password\": \"%s\"}", CORREO_TEST, PASSWORD_TEST);

        // Simulamos usuario no encontrado (NoSuchElementException)
        when(usuarioService.login(anyString(), anyString()))
                .thenThrow(new NoSuchElementException("Credenciales inválidas."));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isNotFound()); // Espera 404
    }

    // --- 3. GET /id/{id} (Interno) ---
    @Test
    void getUsuarioById_encontrado_debeRetornar200() throws Exception {
        // Arrange
        when(usuarioService.getUsuarioById(userId)).thenReturn(usuarioEntity);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/id/{id}", userId))
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.idUsuario").value(userId));

        verify(usuarioService, times(1)).getUsuarioById(userId);
    }

    @Test
    void getUsuarioById_noEncontrado_debeRetornar404() throws Exception {
        // Arrange
        when(usuarioService.getUsuarioById(anyLong()))
                .thenThrow(new NoSuchElementException("Usuario no encontrado."));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/id/{id}", 999L))
                .andExpect(status().isNotFound()); // Espera 404
    }

    // --- 5. GET /perfil/{id} ---
    @Test
    void getProfile_debeRetornar200_yUsuarioResponse() throws Exception {
        // Arrange
        when(usuarioService.getProfile(userId)).thenReturn(usuarioResponse);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/perfil/{id}", userId))
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.nombres").value(usuarioResponse.getNombres()));

        verify(usuarioService, times(1)).getProfile(userId);
    }

    // --- 6. PUT /perfil/{id} ---
    @Test
    void updateProfile_debeRetornar200_yUsuarioResponseActualizado() throws Exception {
        // Arrange
        UsuarioResponse updatedResponse = UsuarioResponse.builder()
                .idUsuario(userId)
                .nombres(updateRequest.getNombres())
                .correo(CORREO_TEST)
                .estado(Usuario.EstadoUsuario.Activo)
                .build();

        when(usuarioService.updateProfile(eq(userId), any(UsuarioUpdateRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/perfil/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.nombres").value(updateRequest.getNombres()));

        verify(usuarioService, times(1)).updateProfile(eq(userId), any(UsuarioUpdateRequest.class));
    }

    @Test
    void updateProfile_usuarioNoEncontrado_debeRetornar404() throws Exception {
        // Arrange
        when(usuarioService.updateProfile(eq(userId), any(UsuarioUpdateRequest.class)))
                .thenThrow(new NoSuchElementException("Usuario no encontrado."));

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/perfil/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound()); // Espera 404
    }
}