package com.microservice.usuarios.config;

import com.microservice.usuarios.model.Usuario;
import com.microservice.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // --- Usuario 1 (Owner Principal) ---
        final String correoAdmin = "ferguzq@gmail.com";
        if (usuarioRepository.findByCorreo(correoAdmin).isEmpty()) {
            Usuario admin = Usuario.builder()
                    .rut("21.612.392-1")
                    .correo(correoAdmin)
                    .nombres("Fernando Alonso")
                    .apellidos("Guzmán González")
                    .telefono("987654321")
                    .fechaRegistro(LocalDateTime.now())
                    .estado(Usuario.EstadoUsuario.Activo)
                    .claveHash(passwordEncoder.encode("123456"))
                    .build();

            usuarioRepository.save(admin);
            System.out.println("✅ Usuario 1 (Owner) precargado: " + correoAdmin + " / 123456");
        } else {
            System.out.println("ℹ️ Usuario 1 ya existe.");
        }

        // --- Nuevo Usuario 2 (Staff Tester) ---
        final String correoTester = "fer.guzmang@duocuc.cl";
        if (usuarioRepository.findByCorreo(correoTester).isEmpty()) {
            Usuario tester = Usuario.builder()
                    .rut("22.329.119-8")
                    .correo(correoTester)
                    .nombres("Fernandito")
                    .apellidos("elguzman")
                    .telefono("900000000")
                    .fechaRegistro(LocalDateTime.now())
                    .estado(Usuario.EstadoUsuario.Activo)
                    .claveHash(passwordEncoder.encode("123456"))
                    .build();

            usuarioRepository.save(tester);
            System.out.println("✅ Usuario 2 (Tester) precargado: " + correoTester + " / tester123");
        } else {
            System.out.println("ℹ️ Usuario 2 ya existe.");
        }
    }
}