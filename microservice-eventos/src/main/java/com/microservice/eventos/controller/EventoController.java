package com.microservice.eventos.controller;

import com.microservice.eventos.dto.*;
import com.microservice.eventos.model.Evento;
import com.microservice.eventos.model.StaffEvento;
import com.microservice.eventos.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión completa del ciclo de vida de Eventos y Staff.")
public class EventoController {

    private final EventoService eventoService;

    @Operation(summary = "Obtener Dashboard de Usuario", description = "Retorna estadísticas y próximos eventos para la pantalla de inicio.")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestHeader("X-User-ID") Long userId) {
        return ResponseEntity.ok(eventoService.obtenerDashboard(userId));
    }

    // --- INVITACIONES STAFF ---

    @Operation(summary = "Listar Invitaciones Pendientes", description = "Muestra los eventos a los que el usuario ha sido invitado como Staff pero aún no acepta.")
    @GetMapping("/invitaciones")
    public ResponseEntity<List<EventoResponse>> getInvitacionesPendientes(
            @RequestHeader("X-User-ID") Long userId) {
        return ResponseEntity.ok(eventoService.listarInvitacionesPendientes(userId));
    }

    @Operation(summary = "Responder Invitación de Staff", description = "Permite al usuario aceptar (true) o rechazar (false) una invitación.")
    @PostMapping("/invitaciones/{idEvento}/responder")
    public ResponseEntity<Void> responderInvitacion(
            @PathVariable Long idEvento,
            @RequestParam boolean aceptar,
            @RequestHeader("X-User-ID") Long userId) {
        
        try {
            eventoService.responderInvitacion(idEvento, userId, aceptar);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(
            summary = "Crear Nuevo Evento",
            description = "Registra un nuevo evento. El estado inicial es 'Publicado'. Requiere el ID del Owner inyectado por el Gateway.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Evento creado con éxito.",
                            content = @Content(schema = @Schema(implementation = Evento.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de evento inválidos o Owner Inactivo.")
            }
    )
    @PostMapping
    public ResponseEntity<Evento> crearEvento(
            @RequestBody Evento evento,
            @Parameter(description = "ID del usuario creador (Owner) inyectado por el Gateway.")
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        Evento nuevoEvento = eventoService.crearEvento(evento, ownerId);
        return new ResponseEntity<>(nuevoEvento, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar Evento",
            description = "Permite al Owner modificar los detalles de un evento existente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento modificado con éxito."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado. Solo el Owner puede actualizar."),
                    @ApiResponse(responseCode = "404", description = "Evento no encontrado.")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Evento> actualizarEvento(
            @Parameter(description = "ID del evento a modificar.") @PathVariable("id") Long idEvento,
            @RequestBody Evento eventoActualizado,
            @Parameter(description = "ID del usuario editor (debe ser el Owner).")
            @RequestHeader(value = "X-User-ID") Long editorId) {

        try {
            Evento eventoModificado = eventoService.actualizarEvento(idEvento, editorId, eventoActualizado);
            return ResponseEntity.ok(eventoModificado);
        } catch (SecurityException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Obtener Evento por ID",
            description = "Retorna la entidad completa del Evento.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento encontrado."),
                    @ApiResponse(responseCode = "404", description = "Evento no encontrado.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Evento> getEventoById(
            @Parameter(description = "ID del evento a consultar.") @PathVariable("id") Long idEvento) {

        Evento evento = eventoService.findById(idEvento);
        return ResponseEntity.ok(evento);
    }


    @Operation(
            summary = "Cancelar Evento",
            description = "Cambia el estado de un evento a 'Cancelado'. Solo permitido por el Owner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento cancelado con éxito."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado. Solo el Owner puede cancelar."),
                    @ApiResponse(responseCode = "404", description = "Evento no encontrado.")
            }
    )
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Evento> cancelarEvento(
            @Parameter(description = "ID del evento a cancelar.") @PathVariable("id") Long id,
            @Parameter(description = "ID del usuario que solicita la cancelación (debe ser el Owner).")
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        try {
            Evento eventoCancelado = eventoService.cancelarEvento(id, ownerId);
            return ResponseEntity.ok(eventoCancelado);
        } catch (SecurityException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(
            summary = "Listar Eventos de Usuario (Owner o Staff)",
            description = "Retorna una lista de eventos donde el usuario logueado tiene un rol (Owner y/o Staff), aplicando filtros opcionales.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de eventos retornada con éxito."),
                    @ApiResponse(responseCode = "204", description = "No se encontraron eventos para el usuario y filtros dados.")
            }
    )
    @GetMapping("/mis-eventos")
    public ResponseEntity<List<EventoResponse>> getMyFilteredEvents(
            @Parameter(description = "ID del usuario logueado (inyectado por el Gateway).")
            @RequestHeader("X-User-ID") Long usuarioId,

            @Parameter(description = "Filtros de búsqueda por nombre y/o relación (OWNER/STAFF).",
                    schema = @Schema(implementation = EventoFiltroRequest.class))
            EventoFiltroRequest filtros) {

        List<EventoResponse> eventos = eventoService.buscarEventosFiltrados(usuarioId, filtros);
        if (eventos.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(eventos);
    }


    @Operation(
            summary = "Asignar/Actualizar Staff y Permisos",
            description = "Asigna un usuario como Staff a un evento o actualiza sus permisos existentes. Solo permitido por el OWNER.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Staff asignado/actualizado con éxito."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado. Solo el Owner puede asignar Staff."),
                    @ApiResponse(responseCode = "400", description = "ID de Evento o Permisos Inválidos.")
            }
    )
    @PostMapping("/staff/asignar")
    public ResponseEntity<StaffEvento> asignarStaff(
            @RequestBody StaffAsignacionRequest request,
            @Parameter(description = "ID del usuario Owner.")
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        try {
            StaffEvento staffAsignado = eventoService.asignarStaffYPermisos(request, ownerId);
            return new ResponseEntity<>(staffAsignado, HttpStatus.CREATED);
        } catch (SecurityException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Revocar Staff",
            description = "Marca la asignación de Staff como inactiva para un evento. Solo permitido por el OWNER.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Staff revocado con éxito."),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado."),
                    @ApiResponse(responseCode = "404", description = "Relación Staff/Evento no encontrada.")
            }
    )
    @DeleteMapping("/{idEvento}/staff/{idStaffUsuario}")
    public ResponseEntity<Void> revocarStaff(
            @Parameter(description = "ID del evento.") @PathVariable("idEvento") Long idEvento,
            @Parameter(description = "ID del usuario Staff a revocar.") @PathVariable("idStaffUsuario") Long idStaffUsuario,
            @Parameter(description = "ID del usuario Owner.") @RequestHeader(value = "X-User-ID") Long ownerId) {

        try {
            eventoService.revocarStaff(idEvento, idStaffUsuario, ownerId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Verificar Permiso de Staff",
            description = "Verifica si un usuario (Owner o Staff) tiene un permiso específico para un evento.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Retorna true/false.")
            }
    )
    @GetMapping("/{idEvento}/permisos/check")
    public ResponseEntity<Boolean> staffTienePermiso(
            @Parameter(description = "ID del evento.") @PathVariable("idEvento") Long idEvento,
            @Parameter(description = "ID del usuario a verificar.") @RequestParam("usuarioId") Long idUsuario,
            @Parameter(description = "Nombre del permiso a chequear (ej: escanear_entrada).") @RequestParam("permiso") String nombrePermiso) {

        boolean tienePermiso = eventoService.staffTienePermiso(idEvento, idUsuario, nombrePermiso);
        return ResponseEntity.ok(tienePermiso);
    }

    @Operation(
            summary = "Obtener Lista de Permisos",
            description = "Retorna todos los permisos activos del usuario (si es Staff) o todos los permisos disponibles (si es Owner) para el evento.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Retorna un Set de nombres de permisos (cadenas).")
            }
    )
    @GetMapping("/{idEvento}/permisos/lista")
    public ResponseEntity<Set<String>> obtenerPermisos(
            @Parameter(description = "ID del evento.") @PathVariable("idEvento") Long idEvento,
            @Parameter(description = "ID del usuario a consultar.") @RequestParam("usuarioId") Long idUsuario) {

        Set<String> permisos = eventoService.obtenerPermisosStaff(idEvento, idUsuario);
        return ResponseEntity.ok(permisos);
    }

    @Operation(
            summary = "Listar Staff del Evento",
            description = "Obtiene la lista completa de miembros del staff para un evento específico, enriquecida con información personal (nombres, correo) obtenida del microservicio de Usuarios. Solo el Owner del evento puede realizar esta acción.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de staff retornada con éxito.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StaffMemberResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario solicitante no es el Owner del evento."),
                    @ApiResponse(responseCode = "404", description = "Evento no encontrado o error al recuperar datos.")
            }
    )
    @GetMapping("/{idEvento}/staff")
    public ResponseEntity<List<StaffMemberResponse>> listarStaffDelEvento(
            @Parameter(description = "ID del evento del cual se quiere listar el staff.", required = true, example = "1")
            @PathVariable("idEvento") Long idEvento,

            @Parameter(description = "ID del usuario que realiza la solicitud (debe ser el Owner). Inyectado por el Gateway.", required = true)
            @RequestHeader(value = "X-User-ID") Long userId) {

        try {
            List<StaffMemberResponse> staffList = eventoService.listarStaffPorEvento(idEvento, userId);
            return ResponseEntity.ok(staffList);
        } catch (SecurityException e) {
            // Retorna 403 si el usuario no es el Owner
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            // Retorna 404 si el evento no existe o hay un error de inconsistencia
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}