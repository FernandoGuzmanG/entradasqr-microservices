package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta del microservicio de Eventos, utilizado para validar la propiedad y obtener el nombre del evento.")
public class EventoOwnerDTO {

    @Schema(description = "ID del usuario propietario (owner) del evento.", example = "1")
    public Long ownerId;

    @Schema(description = "Nombre del evento.", example = "Concierto Rock 2024")
    public String nombre;
}