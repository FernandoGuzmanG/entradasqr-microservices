package com.microservice.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "DTO de entrada para asignar o modificar los permisos de un Staff en un evento.")
public class StaffAsignacionRequest {

    @Schema(description = "ID del Evento al cual se está asignando el Staff.", example = "50", required = true)
    private Long idEvento;

    @Schema(description = "Correo electrónico del usuario a ser asignado como Staff.", example = "staff.persona@email.com", required = true)
    private String CorreoUsuarioStaff;

    @Schema(description = "Lista de nombres de permisos (cadenas) a asignar al Staff.", example = "[\"escanear_entrada\", \"registrar_invitados\"]", required = true)
    private List<String> permisos;
}