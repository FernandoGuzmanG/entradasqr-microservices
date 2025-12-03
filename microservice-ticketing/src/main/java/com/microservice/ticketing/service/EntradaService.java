package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.model.EntradaEmitida;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.repository.EntradaEmitidaRepository;
import com.microservice.ticketing.repository.InvitadoRepository;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import com.microservice.ticketing.dto.CheckinResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EntradaService {

    private final EntradaEmitidaRepository entradaEmitidaRepository;
    private final InvitadoRepository invitadoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final EventoClient eventoClient;
    // Asumimos que tienes el método findByCodigoQR en tu EntradaEmitidaRepository

    @Transactional
    public CheckinResponse validarYUsarEntrada(Long staffId, String codigoQR) {

        // 1. OBTENER LA ENTRADA POR QR (Fuente de Verdad)
        EntradaEmitida entrada = entradaEmitidaRepository.findByCodigoQR(codigoQR)
                .orElseThrow(() -> new RuntimeException("QR no válido o no encontrado."));

        // 2. DERIVAR EL ID DEL EVENTO DE LA ENTRADA
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(entrada.getIdTipoEntrada())
                .orElseThrow(() -> new RuntimeException("Error interno: Tipo de entrada no asociado a la entrada."));

        Long idEvento = tipoEntrada.getIdEvento();

        // 3. VERIFICACIÓN DE PERMISOS DEL STAFF (Llamada a microservice-eventos)

        // a) Verificar si el staff tiene el permiso específico
        boolean tienePermiso = eventoClient.staffTienePermiso(idEvento, staffId, "escanear_entrada");

        // b) Verificar si el staff es el Owner del evento (el owner siempre tiene permisos)
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(idEvento);
        boolean esOwner = eventoInfo.getOwnerId().equals(staffId);

        // Si no es el Owner Y no tiene el permiso de escanear, denegar.
        if (!esOwner && !tienePermiso) {
            throw new RuntimeException("Acceso Denegado. El Staff no tiene permisos para escanear en este evento.");
        }

        // 4. VALIDACIÓN DE ESTADO DE LA ENTRADA
        if (entrada.getEstadoUso() == EntradaEmitida.EstadoUso.UTILIZADA) {
            // Devolvemos el detalle de uso con el mensaje de denegación
            return buildResponse(entrada, "Entrada ya utilizada. Acceso denegado.");
        }

        // 5. ACTUALIZACIÓN (USO) - Transacción de Check-In
        entrada.setEstadoUso(EntradaEmitida.EstadoUso.UTILIZADA);
        entrada.setFechaUso(LocalDateTime.now());
        entradaEmitidaRepository.save(entrada);

        return buildResponse(entrada, "ACCESO CONCEDIDO.");
    }

    // Método auxiliar para construir la respuesta completa
    private CheckinResponse buildResponse(EntradaEmitida entrada, String mensaje) {
        Invitado invitado = invitadoRepository.findById(entrada.getIdInvitado()).orElse(null);
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(entrada.getIdTipoEntrada()).orElse(null);

        CheckinResponse response = new CheckinResponse();
        response.setMensaje(mensaje);
        response.setCodigoQR(entrada.getCodigoQR());
        response.setEstadoUso(entrada.getEstadoUso().name());
        response.setFechaUso(entrada.getFechaUso());

        // Agregar detalles del invitado y la entrada
        if (invitado != null) {
            response.setNombreInvitado(invitado.getNombreCompleto());
            response.setCorreo(invitado.getCorreo());
        }
        if (tipoEntrada != null) {
            response.setNombreTipoEntrada(tipoEntrada.getNombre());
        }

        return response;
    }
}