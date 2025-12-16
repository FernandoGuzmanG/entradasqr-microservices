package com.microservice.eventos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "catalogo_permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Define un permiso disponible para ser asignado a un Staff.")
public class CatalogoPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del permiso.", example = "1")
    private Long idPermiso;

    @Column(name = "nombre_permiso", unique = true, nullable = false)
    @Schema(description = "Nombre clave del permiso (e.g., escanear_entrada, registrar_invitados).", 
            example = "escanear_entrada", 
            requiredMode = Schema.RequiredMode.REQUIRED) 
    private String nombrePermiso;
}