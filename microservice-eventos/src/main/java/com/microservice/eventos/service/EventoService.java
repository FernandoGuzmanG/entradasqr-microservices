package com.microservice.eventos.service;

import com.microservice.eventos.client.UsuarioClient;
import com.microservice.eventos.dto.EventoFiltroRequest;
import com.microservice.eventos.dto.EventoResponse;
import com.microservice.eventos.dto.StaffAsignacionRequest;
import com.microservice.eventos.dto.UsuarioDto;
import com.microservice.eventos.model.*;
import com.microservice.eventos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled; // Importación necesaria
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final StaffEventoRepository staffEventoRepository;
    private final CatalogoPermisoRepository catalogoPermisoRepository;
    private final UsuarioClient usuarioClient;


    public Evento crearEvento(Evento evento, Long ownerId) {
        UsuarioDto owner = usuarioClient.getUsuarioById(ownerId);
        if (owner == null || !"Activo".equalsIgnoreCase(owner.getEstado())) {
            throw new IllegalArgumentException("El Owner del evento no es válido o no está activo.");
        }

        evento.setOwnerId(ownerId);
        evento.setEstado(Evento.EstadoEvento.Publicado);

        return eventoRepository.save(evento);
    }

    public Evento actualizarEvento(Long idEvento, Long editorId, Evento eventoActualizado) {
        if (!esOwner(idEvento, editorId)) {
            throw new SecurityException("Acceso denegado. Solo el Owner puede modificar el evento.");
        }

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new NoSuchElementException("Evento no encontrado."));

        if (evento.getEstado() == Evento.EstadoEvento.Finalizado || evento.getEstado() == Evento.EstadoEvento.Cancelado) {
            throw new IllegalArgumentException("No se puede modificar un evento que está " + evento.getEstado() + ".");
        }

        evento.setNombre(eventoActualizado.getNombre());
        evento.setDescripcion(eventoActualizado.getDescripcion());
        evento.setCapacidadMaxima(eventoActualizado.getCapacidadMaxima());
        evento.setCategoria(eventoActualizado.getCategoria());
        evento.setEstado(eventoActualizado.getEstado());
        evento.setFecha(eventoActualizado.getFecha());
        evento.setDireccion(eventoActualizado.getDireccion());
        evento.setHoraInicio(eventoActualizado.getHoraInicio());
        evento.setHoraCierrePuertas(eventoActualizado.getHoraCierrePuertas());
        evento.setHoraTermino(eventoActualizado.getHoraTermino());

        return eventoRepository.save(evento);
    }

    public Evento cancelarEvento(Long idEvento, Long ownerId) {
        if (!esOwner(idEvento, ownerId)) {
            throw new SecurityException("Acceso denegado. Solo el Owner puede cancelar el evento.");
        }

        Evento evento = findById(idEvento);

        if (evento.getEstado() == Evento.EstadoEvento.Finalizado) {
            throw new IllegalArgumentException("No se puede cancelar un evento que ya está Finalizado.");
        }
        if (evento.getEstado() == Evento.EstadoEvento.Cancelado) {
            // Ya está cancelado, no hace nada, pero retorna el objeto.
            return evento;
        }

        evento.setEstado(Evento.EstadoEvento.Cancelado);
        return eventoRepository.save(evento);
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void finalizarEventosExpirados() {
        LocalDateTime now = LocalDateTime.now();

        List<Evento> eventosAFinalizar = eventoRepository.findEventosToFinalize(
                Evento.EstadoEvento.Publicado.name(),
                now.toLocalDate(),
                now.toLocalTime());

        if (!eventosAFinalizar.isEmpty()) {
            eventosAFinalizar.forEach(evento -> {
                evento.setEstado(Evento.EstadoEvento.Finalizado);
                eventoRepository.save(evento);
            });
            System.out.println("SCHEDULER: Se finalizaron " + eventosAFinalizar.size() + " eventos.");
        }
    }


    public StaffEvento asignarStaffYPermisos(StaffAsignacionRequest request, Long ownerId) {
        if (!esOwner(request.getIdEvento(), ownerId)) {
            throw new SecurityException("Acceso denegado. Solo el Owner puede asignar Staff.");
        }

        UsuarioDto staffDto = usuarioClient.getUsuarioByCorreo(request.getCorreoUsuarioStaff());
        if (staffDto == null || !"Activo".equalsIgnoreCase(staffDto.getEstado())) {
            throw new IllegalArgumentException("El usuario a asignar como Staff no existe o no está activo.");
        }

        Evento evento = eventoRepository.findById(request.getIdEvento())
                .orElseThrow(() -> new NoSuchElementException("Evento no encontrado."));

        Set<CatalogoPermiso> permisos = request.getPermisos().stream()
                .map(nombre -> catalogoPermisoRepository.findByNombrePermiso(nombre)
                        .orElseThrow(() -> new NoSuchElementException("Permiso no válido: " + nombre)))
                .collect(Collectors.toSet());

        StaffEvento staffEvento = staffEventoRepository.findByEvento_IdEventoAndUsuarioId(
                        request.getIdEvento(), staffDto.getIdUsuario())
                .orElse(StaffEvento.builder()
                        .evento(evento)
                        .usuarioId(staffDto.getIdUsuario())
                        .fechaAsignacion(LocalDateTime.now())
                        .build());

        staffEvento.setActivo(true);
        staffEvento.setPermisos(permisos);

        return staffEventoRepository.save(staffEvento);
    }

    public void revocarStaff(Long idEvento, Long staffUsuarioId, Long ownerId) {
        if (!esOwner(idEvento, ownerId)) {
            throw new SecurityException("Acceso denegado. Solo el Owner puede revocar Staff.");
        }

        StaffEvento staffEvento = staffEventoRepository.findByEvento_IdEventoAndUsuarioId(idEvento, staffUsuarioId)
                .orElseThrow(() -> new NoSuchElementException("Relación Staff/Evento no encontrada."));

        staffEvento.setActivo(false);
        staffEventoRepository.save(staffEvento);
    }


    public boolean esOwner(Long idEvento, Long idUsuario) {
        return eventoRepository.findById(idEvento)
                .map(e -> e.getOwnerId().equals(idUsuario))
                .orElse(false);
    }

    public boolean staffTienePermiso(Long idEvento, Long idUsuario, String nombrePermiso) {
        if (esOwner(idEvento, idUsuario)) {
            return true;
        }

        Optional<StaffEvento> staff = staffEventoRepository.findByEvento_IdEventoAndUsuarioId(idEvento, idUsuario);

        if (staff.isEmpty() || !staff.get().isActivo()) {
            return false;
        }

        return staff.get().getPermisos().stream()
                .anyMatch(p -> p.getNombrePermiso().equalsIgnoreCase(nombrePermiso));
    }

    public Set<String> obtenerPermisosStaff(Long idEvento, Long idUsuario) {
        if (esOwner(idEvento, idUsuario)) {
            return catalogoPermisoRepository.findAll().stream()
                    .map(CatalogoPermiso::getNombrePermiso)
                    .collect(Collectors.toSet());
        }

        return staffEventoRepository.findByEvento_IdEventoAndUsuarioId(idEvento, idUsuario)
                .filter(StaffEvento::isActivo)
                .map(staff -> staff.getPermisos().stream()
                        .map(CatalogoPermiso::getNombrePermiso)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }


    public Evento findById(Long idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new NoSuchElementException("Evento no encontrado."));}


    public List<EventoResponse> buscarEventosFiltrados(Long usuarioId, EventoFiltroRequest filtros) {

        Set<Long> ownerEventIds = Set.of();
        Set<Long> staffEventIds = Set.of();

        String relacion = filtros.getRelacion() != null ? filtros.getRelacion().toUpperCase() : null;

        if (relacion == null || "OWNER".equals(relacion)) {
            ownerEventIds = eventoRepository.findAllByOwnerId(usuarioId)
                    .stream()
                    .map(Evento::getIdEvento)
                    .collect(Collectors.toSet());
        }

        if (relacion == null || "STAFF".equals(relacion)) {
            staffEventIds = Set.copyOf(staffEventoRepository.findActiveEventIdsByUsuarioId(usuarioId));
        }

        List<Long> idsFinales = Stream.concat(ownerEventIds.stream(), staffEventIds.stream())
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, String> rolPorEvento = new java.util.HashMap<>();

        ownerEventIds.forEach(id -> rolPorEvento.put(id, "OWNER"));
        staffEventIds.forEach(id -> rolPorEvento.putIfAbsent(id, "STAFF"));

        if (idsFinales.isEmpty()) {
            return List.of();
        }

        List<Evento> eventosFiltrados;
        if (filtros.getNombre() != null && !filtros.getNombre().isBlank()) {
            eventosFiltrados = eventoRepository.findAllByNombreContainingIgnoreCaseAndIdEventoInOrderByFechaAsc(
                    filtros.getNombre(), idsFinales);
        } else {
            eventosFiltrados = eventoRepository.findAllByIdEventoInOrderByFechaAsc(idsFinales);
        }

        return eventosFiltrados.stream()
                .map(evento -> {
                    EventoResponse dto = EventoResponse.fromEntity(evento);
                    dto.setRelacionUsuario(rolPorEvento.get(evento.getIdEvento()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}