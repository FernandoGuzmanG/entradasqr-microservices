package com.microservice.usuarios.service;

import com.microservice.usuarios.dto.UsuarioRegistroRequest;
import com.microservice.usuarios.dto.UsuarioResponse;
import com.microservice.usuarios.dto.UsuarioUpdateRequest;
import com.microservice.usuarios.model.Usuario;
import com.microservice.usuarios.repository.UsuarioRepository;
import com.microservice.usuarios.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Permite usar anotaciones de Mockito
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioActivo;
    private UsuarioRegistroRequest registroRequest;
    private UsuarioUpdateRequest updateRequest;
    private final String RAW_PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "hashedPassword";

    @BeforeEach
    void setUp() {
        // Inicialización de un Usuario de prueba
        usuarioActivo = Usuario.builder()
                .idUsuario(1L)
                .rut("11111111-1")
                .correo("test@mail.cl")
                .nombres("Test")
                .apellidos("User")
                .telefono("987654321")
                .estado(Usuario.EstadoUsuario.Activo)
                .fechaRegistro(LocalDateTime.now())
                .claveHash(ENCODED_PASSWORD)
                .build();

        // Inicialización de DTOs de prueba (corregido usando setters en lugar de builder)
        registroRequest = new UsuarioRegistroRequest();
        registroRequest.setRut("12345678-9");
        registroRequest.setCorreo("nuevo@mail.cl");
        registroRequest.setNombres("Nuevo");
        registroRequest.setApellidos("Registro");
        registroRequest.setTelefono("11112222");
        registroRequest.setPassword(RAW_PASSWORD);

        updateRequest = new UsuarioUpdateRequest();
        updateRequest.setNombres("Test Updated");
        updateRequest.setTelefono("99998888");
    }

    // --- Pruebas para registrarUsuario ---

    @Test
    void registrarUsuario_exitoso_debeRetornarUsuarioResponse() {
        // Arrange
        when(usuarioRepository.findByCorreo(registroRequest.getCorreo())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        // Act
        UsuarioResponse response = usuarioService.registrarUsuario(registroRequest);

        // Assert
        assertNotNull(response);
        assertEquals(usuarioActivo.getCorreo(), response.getCorreo());
        // Verificamos que se codificó la contraseña y se guardó en el repositorio
        verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_correoExistente_debeLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findByCorreo(registroRequest.getCorreo())).thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrarUsuario(registroRequest);
        }, "Debe lanzar IllegalArgumentException si el correo ya existe.");

        // Verificamos que no se intentó guardar
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- Pruebas para login ---

    @Test
    void login_credencialesCorrectas_debeRetornarToken() {
        // Arrange
        final String expectedToken = "mocked.jwt.token";
        when(usuarioRepository.findByCorreo(usuarioActivo.getCorreo())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(usuarioActivo)).thenReturn(expectedToken);

        // Act
        String token = usuarioService.login(usuarioActivo.getCorreo(), RAW_PASSWORD);

        // Assert
        assertEquals(expectedToken, token);
        verify(jwtUtil, times(1)).generateToken(usuarioActivo);
    }

    @Test
    void login_usuarioNoEncontrado_debeLanzarNoSuchElementException() {
        // Arrange
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            usuarioService.login("noexist@mail.cl", RAW_PASSWORD);
        }, "Debe lanzar NoSuchElementException si el usuario no existe.");
    }

    @Test
    void login_claveIncorrecta_debeLanzarIllegalArgumentException() {
        // Arrange
        when(usuarioRepository.findByCorreo(usuarioActivo.getCorreo())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.login(usuarioActivo.getCorreo(), RAW_PASSWORD);
        }, "Debe lanzar IllegalArgumentException si la clave es incorrecta.");
    }

    // --- Pruebas para getUsuarioById ---

    @Test
    void getUsuarioById_encontrado_debeRetornarUsuario() {
        // Arrange
        when(usuarioRepository.findById(usuarioActivo.getIdUsuario())).thenReturn(Optional.of(usuarioActivo));

        // Act
        Usuario encontrado = usuarioService.getUsuarioById(usuarioActivo.getIdUsuario());

        // Assert
        assertEquals(usuarioActivo.getIdUsuario(), encontrado.getIdUsuario());
        verify(usuarioRepository, times(1)).findById(usuarioActivo.getIdUsuario());
    }

    @Test
    void getUsuarioById_noEncontrado_debeLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            usuarioService.getUsuarioById(999L);
        }, "Debe lanzar NoSuchElementException si no se encuentra el usuario.");
    }

    // --- Pruebas para updateProfile ---

    @Test
    void updateProfile_actualizaNombresYTelefono_debeRetornarUsuarioResponseActualizado() {
        // Arrange
        // 1. Simular la obtención del usuario actual
        when(usuarioRepository.findById(usuarioActivo.getIdUsuario())).thenReturn(Optional.of(usuarioActivo));

        // 2. Simular el guardado, capturando el objeto que se intentó guardar
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario updated = invocation.getArgument(0);
            // Simular el DTO de respuesta para la validación
            return updated;
        });

        // Act
        UsuarioResponse response = usuarioService.updateProfile(usuarioActivo.getIdUsuario(), updateRequest);

        // Assert
        assertNotNull(response);
        // Verificar que los campos fueron actualizados
        assertEquals(updateRequest.getNombres(), response.getNombres());
        assertEquals(updateRequest.getTelefono(), response.getTelefono());
        // Verificar que el campo no incluido (rut) se mantuvo
        assertEquals(usuarioActivo.getRut(), response.getRut());

        // Verificar que se llamó a save
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}