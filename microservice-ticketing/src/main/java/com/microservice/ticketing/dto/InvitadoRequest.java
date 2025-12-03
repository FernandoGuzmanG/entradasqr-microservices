package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Solicitud de datos para registrar o modificar un invitado.")
public class InvitadoRequest {

    @Schema(description = "ID del Tipo de Entrada al que se asocia el invitado.", example = "1")
    private Long idTipoEntrada;

    @Schema(description = "Nombre completo del invitado.", example = "Andrea Soto")
    private String nombreCompleto;

    @Schema(description = "Correo electrónico del invitado.", example = "andrea.soto@email.com")
    private String correo;

    @Schema(description = "Cantidad de entradas que se le asignan a este invitado.", example = "2")
    private Integer cantidad;
}