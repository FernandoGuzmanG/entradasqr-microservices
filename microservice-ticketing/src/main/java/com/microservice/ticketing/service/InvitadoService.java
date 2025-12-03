package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.client.NotificacionClient;
import com.microservice.ticketing.dto.EnvioEntradasRequest;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.dto.InvitadoBulkRequest;
import com.microservice.ticketing.dto.InvitadoRequest;
import com.microservice.ticketing.model.EntradaEmitida;
import com.microservice.ticketing.model.EntradaEmitida.EstadoUso;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.model.Invitado.EstadoEnvio;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.repository.EntradaEmitidaRepository;
import com.microservice.ticketing.repository.InvitadoRepository;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitadoService {

    private final InvitadoRepository invitadoRepository;
    private final EntradaEmitidaRepository entradaEmitidaRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final TipoEntradaService tipoEntradaService; // Para validaciones y stock
    private final NotificacionClient notificacionClient;
    private final EventoClient eventoClient;

    private final String PERMISO_REGISTRAR = "registrar_invitados";

    // ----------------------------------------------------------------------------------
    // CRUD Básico (Permitido a Staff con Permiso y Owner)
    // ----------------------------------------------------------------------------------

    /**
     * POST: Crea solo el registro del invitado (sin emisión de entradas ni afectar stock).
     */
    public Invitado crearInvitado(InvitadoRequest request, Long usuarioId) {

        // 1. Validar Permiso: Staff requiere "registrar_invitados" o ser Owner.
        tipoEntradaService.validarPermisoStaff(request.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        // 2. Validar que la cantidad sea positiva
        if (request.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad de entradas debe ser positiva.");
        }

        Invitado nuevoInvitado = Invitado.builder()
                .idTipoEntrada(request.getIdTipoEntrada())
                .nombreCompleto(request.getNombreCompleto())
                .correo(request.getCorreo())
                .cantidad(request.getCantidad())
                .fechaCreacion(LocalDateTime.now())
                .estadoEnvio(EstadoEnvio.PENDIENTE) // Estado inicial: Registrado, no emitido.
                .build();

        return invitadoRepository.save(nuevoInvitado);
    }

    /**
     * PUT: Modifica el nombre y correo del invitado.
     */
    public Invitado modificarInvitado(Long idInvitado, Long usuarioId, InvitadoRequest request) {
        Invitado invitadoExistente = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));

        // 1. Validar Permiso
        tipoEntradaService.validarPermisoStaff(invitadoExistente.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        invitadoExistente.setNombreCompleto(request.getNombreCompleto());
        invitadoExistente.setCorreo(request.getCorreo());

        return invitadoRepository.save(invitadoExistente);
    }

    /**
     * PUT: Modifica SOLO la cantidad de entradas.
     */
    @Transactional
    public Invitado modificarCantidadEntradas(Long idInvitado, Long usuarioId, Integer nuevaCantidad) {
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));

        // 1. Validar Permiso
        tipoEntradaService.validarPermisoStaff(invitado.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        // 2. Restricción: No se puede modificar si ya se emitieron (enviaron) las entradas.
        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE) {
            throw new RuntimeException("No se puede modificar la cantidad de entradas ya emitidas/enviadas.");
        }
        if (nuevaCantidad <= 0) {
            throw new RuntimeException("La nueva cantidad debe ser positiva.");
        }

        invitado.setCantidad(nuevaCantidad);
        return invitadoRepository.save(invitado);
    }

    /**
     * GET: Busca todos los invitados asociados a un TipoEntrada específico.
     */
    public List<Invitado> buscarInvitadosPorTipoEntrada(Long idTipoEntrada) {
        // No se requiere validación de Owner aquí, si la validación se hace a nivel de Controller
        // para asegurar que el usuario tenga acceso al Evento.
        return invitadoRepository.findAllByIdTipoEntrada(idTipoEntrada);
    }

    /**
     * DELETE: Elimina un invitado específico y TODAS sus entradas emitidas, restaurando stock.
     */
    @Transactional
    public void eliminarInvitado(Long idInvitado, Long usuarioId) { // Cambiado ownerId a usuarioId
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));

        // 1. Validar Permiso: Staff requiere "registrar_invitados" o ser Owner.
        tipoEntradaService.validarPermisoStaff(invitado.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        // 2. Si ya se emitieron entradas, ajustar stock
        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE) {
            TipoEntrada tipoEntrada = tipoEntradaService.findById(invitado.getIdTipoEntrada());

            // Verificación de seguridad para evitar stock negativo.
            if (tipoEntrada.getCantidadEmitida() < invitado.getCantidad()) {
                // Esto no debería pasar, pero es una buena guardia.
                throw new RuntimeException("Error de consistencia de stock al eliminar invitado.");
            }

            tipoEntrada.setCantidadEmitida(tipoEntrada.getCantidadEmitida() - invitado.getCantidad());
            tipoEntradaRepository.save(tipoEntrada);
        }

        // 3. Eliminar Entradas Emitidas
        entradaEmitidaRepository.deleteAll(entradaEmitidaRepository.findAllByIdInvitado(idInvitado));

        // 4. Eliminar Invitado
        invitadoRepository.delete(invitado);
    }

    // ----------------------------------------------------------------------------------
    // Lógica de EMISIÓN (Exclusivo Owner)
    // ----------------------------------------------------------------------------------

    /**
     * POST: Dispara la emisión de entradas y el envío de correo para un Invitado ya registrado.
     * EXCLUSIVO OWNER.
     */
    @Transactional
    public Invitado emitirEntradasPorId(Long idInvitado, Long ownerId) {
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));

        // 1. Validar Propiedad (Exclusivo Owner)
        tipoEntradaService.validarPropiedadEvento(invitado.getIdTipoEntrada(), ownerId);

        // 2. Validar que no haya sido emitido antes.
        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE && invitado.getEstadoEnvio() != EstadoEnvio.ERROR_ENVIO) {
            throw new RuntimeException("Las entradas para este invitado ya fueron emitidas previamente.");
        }

        // 3. Realizar Emisión (Generación de QR, Stock, Notificación)
        return realizarEmision(invitado);
    }

    /**
     * POST: Procesa la solicitud de una lista de invitados (carga masiva).
     * EXCLUSIVO OWNER.
     */
    @Transactional
    public List<Invitado> emitirEntradasMasivas(InvitadoBulkRequest request, Long ownerId) {

        TipoEntrada tipoEntrada = tipoEntradaService.findById(request.getIdTipoEntrada());

        // 1. Validar Propiedad (Exclusivo Owner)
        tipoEntradaService.validarPropiedadEvento(request.getIdTipoEntrada(), ownerId);

        int totalSolicitado = request.getInvitados().stream().mapToInt(InvitadoRequest::getCantidad).sum();

        // 2. Validar Stock Total
        if (tipoEntrada.getCantidadEmitida() + totalSolicitado > tipoEntrada.getCantidadTotal()) {
            throw new RuntimeException("Stock insuficiente para emitir la carga masiva. Solicitado: " + totalSolicitado);
        }

        List<Invitado> invitadosProcesados = new ArrayList<>();

        // 3. Procesar CADA INVITADO
        for (InvitadoRequest invitadoReq : request.getInvitados()) {

            // Creamos o encontramos al invitado (Aquí simplificamos creando un nuevo registro por cada solicitud masiva)
            Invitado nuevoInvitado = crearInvitado(invitadoReq, ownerId); // El owner tiene permiso de registro.

            // Ejecutamos la lógica de emisión.
            Invitado invitadoEmitido = realizarEmision(nuevoInvitado);
            invitadosProcesados.add(invitadoEmitido);
        }

        return invitadosProcesados;
    }

    // ----------------------------------------------------------------------------------
    // Lógica Privada de Emisión
    // ----------------------------------------------------------------------------------

    /**
     * Lógica compartida para generar QR, actualizar stock y notificar.
     */
    private Invitado realizarEmision(Invitado invitado) {

        TipoEntrada tipoEntrada = tipoEntradaService.findById(invitado.getIdTipoEntrada());

        // 1. ACTUALIZAR STOCK Y GESTIÓN DE ENTRADAS EMITIDAS PREVIAS
        // Asumo que la lógica de stock y eliminación/regeneración de entradas emitidas está correcta aquí.
        if (invitado.getEstadoEnvio() == EstadoEnvio.PENDIENTE || invitado.getEstadoEnvio() == EstadoEnvio.ERROR_ENVIO) {
            // Validación de Stock
            if (tipoEntrada.getCantidadEmitida() + invitado.getCantidad() > tipoEntrada.getCantidadTotal()) {
                throw new RuntimeException("Stock insuficiente para emitir " + invitado.getCantidad() + " entradas.");
            }

            tipoEntrada.setCantidadEmitida(tipoEntrada.getCantidadEmitida() + invitado.getCantidad());
            tipoEntradaRepository.save(tipoEntrada);
        }

        // Regenerar entradas emitidas si es re-emisión
        entradaEmitidaRepository.deleteAll(entradaEmitidaRepository.findAllByIdInvitado(invitado.getIdInvitado()));

        // 2. EMITIR ENTRADAS (CÓDIGOS QR)
        List<EntradaEmitida> entradasEmitidas = new ArrayList<>();
        List<EnvioEntradasRequest.TicketData> ticketsData = new ArrayList<>();

        for (int i = 0; i < invitado.getCantidad(); i++) {
            String codigoQR = generateUniqueQRCode();

            // Construcción de la Entrada Emitida (para DB local)
            EntradaEmitida entrada = EntradaEmitida.builder()
                    .idInvitado(invitado.getIdInvitado())
                    .idTipoEntrada(invitado.getIdTipoEntrada())
                    .codigoQR(codigoQR)
                    .fechaEmision(LocalDateTime.now())
                    .estadoUso(EstadoUso.NO_UTILIZADA)
                    .build();
            entradasEmitidas.add(entrada);

            // Construcción del DTO de TicketData (para el correo)
            EnvioEntradasRequest.TicketData ticketData = new EnvioEntradasRequest.TicketData();
            ticketData.setCodigoQR(codigoQR);
            ticketData.setEstadoUso(EstadoUso.NO_UTILIZADA.name());
            ticketsData.add(ticketData);
        }
        entradaEmitidaRepository.saveAll(entradasEmitidas);

        // 3. CONSTRUCCIÓN DEL DTO DE COMUNICACIONES
        // Asumiendo que getEventoOwnerById devuelve un DTO con el nombre
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(tipoEntrada.getIdEvento());

        EnvioEntradasRequest requestComunicaciones = new EnvioEntradasRequest();
        requestComunicaciones.setIdInvitado(invitado.getIdInvitado());
        requestComunicaciones.setCorreoDestino(invitado.getCorreo());
        requestComunicaciones.setNombreInvitado(invitado.getNombreCompleto());
        requestComunicaciones.setNombreEvento(eventoInfo.getNombre()); // Obtener el nombre del evento
        requestComunicaciones.setIdTipoEntrada(tipoEntrada.getIdTipoEntrada());
        requestComunicaciones.setNombreTipoEntrada(tipoEntrada.getNombre());
        requestComunicaciones.setTickets(ticketsData);

        // 4. INICIAR PROCESO DE COMUNICACIÓN
        try {
            // LLAMADA FEIGN AL MICROSERVICIO DE COMUNICACIONES
            notificacionClient.enviarEntradas(requestComunicaciones);
            invitado.setEstadoEnvio(EstadoEnvio.ENVIADO);
        } catch (Exception e) {
            // MEJOR MANEJO DE ERRORES: Registramos el fallo de la llamada Feign
            // Usa un logger apropiado si tienes @Slf4j
            // log.error("Fallo al llamar a microservice-comunicaciones para Invitado {}: {}", invitado.getIdInvitado(), e.getMessage(), e);

            // Si no usas logger:
            System.err.println("--- ERROR DE LLAMADA FEIGN A COMUNICACIONES ---");
            e.printStackTrace();
            System.err.println("----------------------------------------------");

            invitado.setEstadoEnvio(EstadoEnvio.ERROR_ENVIO);
        }

        return invitadoRepository.save(invitado);
    }

    /**
     * Genera un código QR único (UUID simplificado).
     */
    private String generateUniqueQRCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}