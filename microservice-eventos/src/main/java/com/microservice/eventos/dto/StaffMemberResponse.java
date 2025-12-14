package com.microservice.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@Schema(description = "Detalles de un miembro del Staff, incluyendo información personal obtenida del servicio de Usuarios.")
public class StaffMemberResponse {

    @Schema(description = "ID del registro de Staff.", example = "10")
    private Long idStaff;

    @Schema(description = "ID del Usuario asociado.", example = "101")
    private Long usuarioId;

    @Schema(description = "Nombre completo del usuario (Nombres + Apellidos).", example = "Juan Pérez")
    private String nombreCompleto;

    @Schema(description = "Correo electrónico del usuario.", example = "juan.perez@example.com")
    private String correo;

    @Schema(description = "Fecha en que fue asignado/invitado.", example = "2023-10-20T10:00:00")
    private LocalDateTime fechaAsignacion;

    @Schema(description = "Si el staff está activo en el evento.", example = "true")
    private boolean activo;

    @Schema(description = "Estado de la invitación (PENDIENTE, ACEPTADO, RECHAZADO).", example = "ACEPTADO")
    private String estadoInvitacion;

    @Schema(description = "Lista de permisos asignados.")
    private Set<String> permisos;
}