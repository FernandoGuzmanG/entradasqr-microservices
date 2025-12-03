package com.microservice.comunicaciones.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnvioEntradasRequest {
    private Long idInvitado;
    private String correoDestino;
    private String nombreInvitado;
    private String nombreEvento;
    private Long idTipoEntrada;
    private String nombreTipoEntrada;

    private List<TicketData> tickets;

    @Data
    public static class TicketData {
        private String codigoQR; // El código de 32 caracteres
        private String estadoUso;
    }
}