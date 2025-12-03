package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Solicitud para el registro y emisión masiva de entradas a múltiples invitados.")
public class InvitadoBulkRequest {

    @Schema(description = "ID del Tipo de Entrada al que se asociarán todos los invitados de esta lista.", example = "1")
    private Long idTipoEntrada;

    @Schema(description = "Lista de las solicitudes individuales de invitados (nombre, correo y cantidad).")
    private List<InvitadoRequest> invitados;
}