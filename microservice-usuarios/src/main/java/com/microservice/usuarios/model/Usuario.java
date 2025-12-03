package com.microservice.usuarios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalles completos de la entidad Usuario en la base de datos.")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del usuario.", example = "1")
    private Long idUsuario;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    @Schema(description = "Rol Único Tributario (identificador nacional).", example = "12345678-9", required = true)
    private String rut;

    @Column(name = "correo", nullable = false, unique = true, length = 255)
    @Schema(description = "Correo electrónico del usuario, usado para login.", example = "ferguzq@gmail.com", required = true)
    private String correo;

    @Column(name = "clave_hash", nullable = false, length = 255)
    @Schema(description = "Hash de la clave.", accessMode = Schema.AccessMode.WRITE_ONLY, required = true)
    private String claveHash;

    @Column(name = "nombres", nullable = false, length = 100)
    @Schema(description = "Nombres del usuario.", example = "Fernando Alonso", required = true)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    @Schema(description = "Apellidos del usuario.", example = "Guzmán González", required = true)
    private String apellidos;

    @Column(name = "telefono", length = 20)
    @Schema(description = "Número de teléfono de contacto.", example = "+56987751451")
    private String telefono;

    @Column(name = "fecha_registro")
    @Schema(description = "Fecha y hora del registro del usuario.", example = "2023-10-20T14:30:00")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del usuario en el sistema.", required = true)
    private EstadoUsuario estado;

    @Schema(description = "Posibles estados en los que puede estar un usuario.")
    public enum EstadoUsuario {
        Activo, Inactivo, Suspendido
    }
}