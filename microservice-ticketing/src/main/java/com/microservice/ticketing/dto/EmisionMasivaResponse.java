package com.microservice.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resumen del resultado del proceso de emisión masiva de entradas.")
public class EmisionMasivaResponse {

    @Schema(description = "Mensaje descriptivo del resultado.", example = "Proceso de emisión finalizado.")
    private String mensaje;

    @Schema(description = "Número total de invitados procesados (pendientes o con error previo).", example = "50")
    private int totalProcesados;

    @Schema(description = "Cantidad de correos enviados exitosamente.", example = "48")
    private int enviadas;

    @Schema(description = "Cantidad de envíos fallidos.", example = "2")
    private int fallidas;
}