package com.microservice.usuarios.service;

import com.microservice.usuarios.dto.ChangePasswordRequest;
import com.microservice.usuarios.dto.LoginResponse;
import com.microservice.usuarios.dto.UsuarioRegistroRequest;
import com.microservice.usuarios.dto.UsuarioResponse;
import com.microservice.usuarios.dto.UsuarioUpdateRequest;
import com.microservice.usuarios.model.Usuario;
import com.microservice.usuarios.repository.UsuarioRepository;
import com.microservice.usuarios.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getIdUsuario())
                .correo(usuario.getCorreo())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .rut(usuario.getRut())
                .telefono(usuario.getTelefono())
                .build();
    }

    public UsuarioResponse registrarUsuario(UsuarioRegistroRequest request) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        if (usuarioRepository.findByRut(request.getRut()).isPresent()) {
            throw new IllegalArgumentException("El RUT ya está registrado.");
        }

        Usuario nuevoUsuario = Usuario.builder()
                .rut(request.getRut())
                .correo(request.getCorreo())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .fechaRegistro(LocalDateTime.now())
                .estado(Usuario.EstadoUsuario.Activo)
                .claveHash(passwordEncoder.encode(request.getPassword()))
                .build();

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);

        return mapToResponse(savedUser);
    }

    public LoginResponse login(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new NoSuchElementException("Credenciales inválidas."));

        if (!passwordEncoder.matches(password, usuario.getClaveHash())) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }
        String token = jwtUtil.generateToken(usuario);
        LoginResponse response = new LoginResponse(token);

        return response;
    }

    public Usuario getUsuarioById(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado."));
    }

    public Usuario getUsuarioByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado."));
    }

    public UsuarioResponse getProfile(Long idUsuario) {
        Usuario usuario = getUsuarioById(idUsuario);
        return mapToResponse(usuario);
    }

    public UsuarioResponse updateProfile(Long idUsuario, UsuarioUpdateRequest request) {
        Usuario usuario = getUsuarioById(idUsuario);
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setTelefono(request.getTelefono());

        Usuario updatedUser = usuarioRepository.save(usuario);

        return mapToResponse(updatedUser);
    }

    public void changePassword(Long idUsuario, ChangePasswordRequest request) {
        Usuario usuario = getUsuarioById(idUsuario);

        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getClaveHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden.");
        }

        if (request.getNewPassword().length() < 8 || request.getNewPassword().length() > 16) {
            throw new IllegalArgumentException("La nueva contraseña debe tener entre 8 y 16 caracteres.");
        }

        usuario.setClaveHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }
}