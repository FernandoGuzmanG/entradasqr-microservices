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
import org.springframework.data.domain.Sort;
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
    private final TipoEntradaService tipoEntradaService;
    private final NotificacionClient notificacionClient;
    private final EventoClient eventoClient;

    private final String PERMISO_REGISTRAR = "registrar_invitados";

    public List<Invitado> filtrarInvitados(Long idTipoEntrada, String termino, String ordenFecha) {
        Sort.Direction direction = Sort.Direction.DESC;
        if (ordenFecha != null && "ASC".equalsIgnoreCase(ordenFecha)) {
            direction = Sort.Direction.ASC;
        }
        Sort sort = Sort.by(direction, "fechaCreacion");

        return invitadoRepository.buscarPorTermino(idTipoEntrada, termino, sort);
    }

    // ----------------------------------------------------------------------------------
    // CRUD Básico
    // ----------------------------------------------------------------------------------

    public Invitado crearInvitado(InvitadoRequest request, Long usuarioId) {
        tipoEntradaService.validarPermisoStaff(request.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);
        if (request.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad de entradas debe ser positiva.");
        }
        Invitado nuevoInvitado = Invitado.builder()
                .idTipoEntrada(request.getIdTipoEntrada())
                .nombreCompleto(request.getNombreCompleto())
                .correo(request.getCorreo())
                .cantidad(request.getCantidad())
                .fechaCreacion(LocalDateTime.now())
                .estadoEnvio(EstadoEnvio.PENDIENTE)
                .build();
        return invitadoRepository.save(nuevoInvitado);
    }

    public Invitado modificarInvitado(Long idInvitado, Long usuarioId, InvitadoRequest request) {
        Invitado invitadoExistente = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));
        tipoEntradaService.validarPermisoStaff(invitadoExistente.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);
        invitadoExistente.setNombreCompleto(request.getNombreCompleto());
        invitadoExistente.setCorreo(request.getCorreo());
        return invitadoRepository.save(invitadoExistente);
    }

    @Transactional
    public Invitado modificarCantidadEntradas(Long idInvitado, Long usuarioId, Integer nuevaCantidad) {
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));
        tipoEntradaService.validarPermisoStaff(invitado.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE) {
            throw new RuntimeException("No se puede modificar la cantidad de entradas ya emitidas/enviadas.");
        }
        if (nuevaCantidad <= 0) {
            throw new RuntimeException("La nueva cantidad debe ser positiva.");
        }
        invitado.setCantidad(nuevaCantidad);
        return invitadoRepository.save(invitado);
    }

    public List<Invitado> buscarInvitadosPorTipoEntrada(Long idTipoEntrada) {
        return invitadoRepository.findAllByIdTipoEntrada(idTipoEntrada, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    @Transactional
    public void eliminarInvitado(Long idInvitado, Long usuarioId) {
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));
        tipoEntradaService.validarPermisoStaff(invitado.getIdTipoEntrada(), usuarioId, PERMISO_REGISTRAR);

        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE) {
            TipoEntrada tipoEntrada = tipoEntradaService.findById(invitado.getIdTipoEntrada());
            if (tipoEntrada.getCantidadEmitida() < invitado.getCantidad()) {
                throw new RuntimeException("Error de consistencia de stock al eliminar invitado.");
            }
            tipoEntrada.setCantidadEmitida(tipoEntrada.getCantidadEmitida() - invitado.getCantidad());
            tipoEntradaRepository.save(tipoEntrada);
        }
        entradaEmitidaRepository.deleteAll(entradaEmitidaRepository.findAllByIdInvitado(idInvitado));
        invitadoRepository.delete(invitado);
    }

    // ----------------------------------------------------------------------------------
    // Lógica de EMISIÓN
    // ----------------------------------------------------------------------------------

    @Transactional
    public Invitado emitirEntradasPorId(Long idInvitado, Long ownerId) {
        Invitado invitado = invitadoRepository.findById(idInvitado)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado."));
        
        // Validar propiedad del evento
        tipoEntradaService.validarPropiedadEvento(invitado.getIdTipoEntrada(), ownerId);

        if (invitado.getEstadoEnvio() != EstadoEnvio.PENDIENTE && invitado.getEstadoEnvio() != EstadoEnvio.ERROR_ENVIO) {
            throw new RuntimeException("Las entradas para este invitado ya fueron emitidas previamente.");
        }
        
        // Reutilizamos el método privado para casos individuales
        return realizarEmisionIndividual(invitado);
    }

    /**
     * Método optimizado para emisión masiva:
     * 1. Carga datos comunes una vez (TipoEntrada, InfoEvento).
     * 2. Actualiza stock en bloque.
     * 3. Itera generando tickets sin consultas redundantes.
     */
    @Transactional
    public List<Invitado> emitirEntradasMasivas(Long idTipoEntrada, Long ownerId) {
        
        // 1. Obtener y validar datos comunes UNA SOLA VEZ
        TipoEntrada tipoEntrada = tipoEntradaService.findById(idTipoEntrada);
        tipoEntradaService.validarPropiedadEvento(idTipoEntrada, ownerId);
        
        // Información del evento para el correo (evita N llamadas Feign)
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(tipoEntrada.getIdEvento());

        // 2. Buscar invitados pendientes
        List<Invitado> invitadosPendientes = invitadoRepository.findAllByIdTipoEntradaAndEstadoEnvioIn(
                idTipoEntrada, List.of(EstadoEnvio.PENDIENTE, EstadoEnvio.ERROR_ENVIO));

        if (invitadosPendientes.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Validar y Actualizar Stock EN BLOQUE
        // Solo descontamos stock para los que están PENDIENTES (los de ERROR ya descontaron stock antes)
        int stockRequeridoNuevo = invitadosPendientes.stream()
                .filter(i -> i.getEstadoEnvio() == EstadoEnvio.PENDIENTE)
                .mapToInt(Invitado::getCantidad)
                .sum();

        if (tipoEntrada.getCantidadEmitida() + stockRequeridoNuevo > tipoEntrada.getCantidadTotal()) {
            throw new RuntimeException("Stock insuficiente para emitir a todos los invitados pendientes. Faltan: " + 
                ((tipoEntrada.getCantidadEmitida() + stockRequeridoNuevo) - tipoEntrada.getCantidadTotal()));
        }

        // Actualizamos stock una sola vez en la DB
        if (stockRequeridoNuevo > 0) {
            tipoEntrada.setCantidadEmitida(tipoEntrada.getCantidadEmitida() + stockRequeridoNuevo);
            tipoEntradaRepository.save(tipoEntrada);
        }

        // 4. Procesar emisión iterativa (sin consultas extras)
        List<Invitado> invitadosProcesados = new ArrayList<>();
        
        for (Invitado invitado : invitadosPendientes) {
            // Llamamos al método auxiliar pasándole los datos ya cargados
            Invitado procesado = generarYNotificar(invitado, tipoEntrada, eventoInfo);
            invitadosProcesados.add(procesado);
        }

        return invitadosProcesados;
    }

    /**
     * Maneja la lógica de stock para un solo invitado y delega la generación.
     * Usado por el endpoint individual.
     */
    private Invitado realizarEmisionIndividual(Invitado invitado) {
        TipoEntrada tipoEntrada = tipoEntradaService.findById(invitado.getIdTipoEntrada());
        
        // Manejo de Stock Individual
        if (invitado.getEstadoEnvio() == EstadoEnvio.PENDIENTE) {
            if (tipoEntrada.getCantidadEmitida() + invitado.getCantidad() > tipoEntrada.getCantidadTotal()) {
                throw new RuntimeException("Stock insuficiente.");
            }
            tipoEntrada.setCantidadEmitida(tipoEntrada.getCantidadEmitida() + invitado.getCantidad());
            tipoEntradaRepository.save(tipoEntrada);
        }

        // Obtener info del evento (necesario aquí porque es individual)
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(tipoEntrada.getIdEvento());
        
        return generarYNotificar(invitado, tipoEntrada, eventoInfo);
    }

    /**
     * Lógica central de generación de tickets y notificación.
     * NO realiza operaciones de stock ni consultas de TipoEntrada/Evento.
     */
    private Invitado generarYNotificar(Invitado invitado, TipoEntrada tipoEntrada, EventoOwnerDTO eventoInfo) {
        
        // Limpiar intentos previos (para reintentos de error)
        entradaEmitidaRepository.deleteAll(entradaEmitidaRepository.findAllByIdInvitado(invitado.getIdInvitado()));

        // Generar Tickets
        List<EntradaEmitida> entradasEmitidas = new ArrayList<>();
        List<EnvioEntradasRequest.TicketData> ticketsData = new ArrayList<>();

        for (int i = 0; i < invitado.getCantidad(); i++) {
            String codigoQR = generateUniqueQRCode();
            
            EntradaEmitida entrada = EntradaEmitida.builder()
                    .idInvitado(invitado.getIdInvitado())
                    .idTipoEntrada(invitado.getIdTipoEntrada())
                    .codigoQR(codigoQR)
                    .fechaEmision(LocalDateTime.now())
                    .estadoUso(EstadoUso.NO_UTILIZADA)
                    .build();
            entradasEmitidas.add(entrada);

            EnvioEntradasRequest.TicketData ticketData = new EnvioEntradasRequest.TicketData();
            ticketData.setCodigoQR(codigoQR);
            ticketData.setEstadoUso(EstadoUso.NO_UTILIZADA.name());
            ticketsData.add(ticketData);
        }
        entradaEmitidaRepository.saveAll(entradasEmitidas);

        // Preparar Notificación
        EnvioEntradasRequest requestComunicaciones = new EnvioEntradasRequest();
        requestComunicaciones.setIdInvitado(invitado.getIdInvitado());
        requestComunicaciones.setCorreoDestino(invitado.getCorreo());
        requestComunicaciones.setNombreInvitado(invitado.getNombreCompleto());
        requestComunicaciones.setNombreEvento(eventoInfo.getNombre());
        requestComunicaciones.setIdTipoEntrada(tipoEntrada.getIdTipoEntrada());
        requestComunicaciones.setNombreTipoEntrada(tipoEntrada.getNombre());
        requestComunicaciones.setTickets(ticketsData);

        // Llamada a Comunicaciones
        try {
            notificacionClient.enviarEntradas(requestComunicaciones);
            invitado.setEstadoEnvio(EstadoEnvio.ENVIADO);
        } catch (Exception e) {
            System.err.println("--- ERROR COMUNICACIONES (Invitado ID " + invitado.getIdInvitado() + ") ---");
            e.printStackTrace();
            invitado.setEstadoEnvio(EstadoEnvio.ERROR_ENVIO);
        }

        return invitadoRepository.save(invitado);
    }

    private String generateUniqueQRCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}