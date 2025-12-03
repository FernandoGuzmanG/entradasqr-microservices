package com.microservice.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO de entrada para aplicar filtros al buscar eventos.")
public class EventoFiltroRequest {

    @Schema(description = "Filtro de búsqueda por nombre del evento (contiene).", example = "Concierto")
    private String nombre;

    @Schema(description = "Filtro por rol del usuario en relación al evento.", example = "OWNER o STAFF")
    private String relacion;
}