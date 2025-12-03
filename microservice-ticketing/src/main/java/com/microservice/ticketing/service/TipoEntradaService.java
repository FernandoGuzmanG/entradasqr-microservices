package com.microservice.ticketing.service;

import com.microservice.ticketing.client.EventoClient;
import com.microservice.ticketing.dto.EventoOwnerDTO;
import com.microservice.ticketing.dto.TipoEntradaRequest;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.model.TipoEntrada.EstadoTipoEntrada;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import com.microservice.ticketing.repository.InvitadoRepository;
import com.microservice.ticketing.repository.EntradaEmitidaRepository;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEntradaService {

    private final TipoEntradaRepository tipoEntradaRepository;
    private final InvitadoRepository invitadoRepository;
    private final EntradaEmitidaRepository entradaEmitidaRepository;
    private final EventoClient eventoClient;

    /**
     * Busca un TipoEntrada por ID o lanza una excepción.
     */
    public TipoEntrada findById(Long idTipoEntrada) {
        return tipoEntradaRepository.findById(idTipoEntrada)
                .orElseThrow(() -> new RuntimeException("Tipo de entrada no encontrado."));
    }

    // ----------------------------------------------------------------------------------
    // Métodos de Validación (Seguridad S2S)
    // ----------------------------------------------------------------------------------

    /**
     * Valida que el usuario sea el Owner del Evento asociado al TipoEntrada.
     */
    public void validarPropiedadEvento(Long idTipoEntrada, Long usuarioId) {
        TipoEntrada tipoEntrada = findById(idTipoEntrada);
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(tipoEntrada.getIdEvento());

        if (!eventoInfo.getOwnerId().equals(usuarioId)) {
            throw new SecurityException("Acceso denegado: El usuario no es el propietario del evento.");
        }
    }

    /**
     * Verifica si un usuario (Owner o Staff) tiene el permiso requerido para un evento.
     * Si es el Owner, el permiso siempre es concedido.
     */
    public void validarPermisoStaff(Long idTipoEntrada, Long usuarioId, String permisoNecesario) {
        TipoEntrada tipoEntrada = findById(idTipoEntrada);
        Long idEvento = tipoEntrada.getIdEvento();

        // 1. Obtener Owner y verificar si el usuario es el Owner (siempre permitido)
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(idEvento);
        if (eventoInfo.getOwnerId().equals(usuarioId)) {
            return; // Es el Owner, conceder permiso.
        }

        // 2. Verificar Permiso de Staff
        boolean tienePermiso = eventoClient.staffTienePermiso(idEvento, usuarioId, permisoNecesario);

        if (!tienePermiso) {
            throw new SecurityException("Acceso denegado. El usuario no tiene el permiso requerido: " + permisoNecesario);
        }
    }

    // ----------------------------------------------------------------------------------
    // Métodos de CRUD
    // ----------------------------------------------------------------------------------

    /**
     * Crea un nuevo tipo de entrada, validando la propiedad del evento (Solo Owner).
     */
    public TipoEntrada crearTipoEntrada(TipoEntradaRequest request, Long ownerId) {

        // La validación de propiedad se hace directamente con el Feign Client al evento
        EventoOwnerDTO eventoInfo = eventoClient.getEventoOwnerById(request.getIdEvento());

        if (!eventoInfo.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Acceso denegado: La creación de Tipos de Entrada es exclusiva del propietario del evento.");
        }

        TipoEntrada nuevoTipo = TipoEntrada.builder()
                .idEvento(request.getIdEvento())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .cantidadTotal(request.getCantidadTotal())
                .cantidadEmitida(0)
                .fechaInicioVenta(request.getFechaInicioVenta())
                .fechaFinVenta(request.getFechaFinVenta())
                .estado(EstadoTipoEntrada.ACTIVO)
                .build();

        return tipoEntradaRepository.save(nuevoTipo);
    }

    /**
     * Obtiene todos los tipos de entrada para un evento específico.
     */
    public List<TipoEntrada> buscarTiposPorEvento(Long idEvento) {
        return tipoEntradaRepository.findAllByIdEvento(idEvento);
    }

    /**
     * Implementación de la búsqueda LIKE '%%'.
     */
    public List<TipoEntrada> buscarTiposPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }
        return tipoEntradaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Actualiza un tipo de entrada existente, validando la propiedad (Solo Owner).
     */
    public TipoEntrada actualizarTipoEntrada(Long idTipoEntrada, Long ownerId, TipoEntradaRequest request) {
        TipoEntrada tipoExistente = findById(idTipoEntrada);

        // Validación de propiedad (Solo Owner)
        validarPropiedadEvento(idTipoEntrada, ownerId);

        tipoExistente.setNombre(request.getNombre());
        tipoExistente.setDescripcion(request.getDescripcion());
        tipoExistente.setPrecio(request.getPrecio());
        tipoExistente.setCantidadTotal(request.getCantidadTotal());
        tipoExistente.setFechaInicioVenta(request.getFechaInicioVenta());
        tipoExistente.setFechaFinVenta(request.getFechaFinVenta());

        return tipoEntradaRepository.save(tipoExistente);
    }

    /**
     * Elimina el tipo de entrada y toda su data asociada (cascada manual),
     * validando la propiedad (Solo Owner).
     */
    @Transactional
    public void eliminarTipoEntrada(Long idTipoEntrada, Long ownerId) {

        // 1. VALIDACIÓN DE PROPIEDAD
        validarPropiedadEvento(idTipoEntrada, ownerId);

        // 2. ELIMINACIÓN DE DATOS ASOCIADOS (CASCADA MANUAL)
        entradaEmitidaRepository.deleteAll(entradaEmitidaRepository.findAllByIdTipoEntrada(idTipoEntrada));
        invitadoRepository.deleteAll(invitadoRepository.findAllByIdTipoEntrada(idTipoEntrada));

        // 3. ELIMINAR TIPO DE ENTRADA
        tipoEntradaRepository.deleteById(idTipoEntrada);
    }
}