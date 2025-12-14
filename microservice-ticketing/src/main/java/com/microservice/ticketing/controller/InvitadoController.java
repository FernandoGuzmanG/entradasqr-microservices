package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.InvitadoRequest;
import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.service.InvitadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitados")
@RequiredArgsConstructor
@Tag(name = "Invitados y Emisión de Entradas", description = "Gestión (CRUD) de la lista de invitados/órdenes de compra y el proceso de emisión de códigos QR.")
public class InvitadoController {

    private final InvitadoService invitadoService;

    // --- Endpoints de BÚSQUEDA y FILTRADO ---

    @Operation(summary = "Filtrar y Ordenar Invitados",
            description = "Busca invitados por un término general (coincidencia en nombre O correo) y permite ordenar los resultados por fecha de creación.")
    @ApiResponse(responseCode = "200", description = "Lista de invitados filtrada y ordenada.")
    @ApiResponse(responseCode = "204", description = "No se encontraron resultados.")
    @GetMapping("/buscar")
    public ResponseEntity<List<Invitado>> filtrarInvitados(
            @Parameter(description = "ID del tipo de entrada (Obligatorio).", required = true)
            @RequestParam Long idTipoEntrada,

            @Parameter(description = "Término de búsqueda (busca coincidencias en nombre O correo).")
            @RequestParam(required = false) String termino,

            @Parameter(description = "Ordenamiento por fecha de creación: 'ASC' (más antiguos primero) o 'DESC' (más recientes primero). Por defecto es DESC.")
            @RequestParam(required = false, defaultValue = "DESC") String ordenFecha) {

        List<Invitado> invitados = invitadoService.filtrarInvitados(idTipoEntrada, termino, ordenFecha);
        
        if (invitados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(invitados);
    }

    // --- Endpoints de GESTIÓN (CRUD - Permitido a OWNER y STAFF con permiso "registrar_invitados") ---

    @PostMapping
    @Operation(summary = "Registra un nuevo invitado/orden de compra.",
            description = "Crea el registro del invitado SIN EMITIR los códigos QR. Se requiere el permiso 'registrar_invitados' o ser Owner.")
    @ApiResponse(responseCode = "201", description = "Invitado registrado con éxito (Estado: PENDIENTE).")
    @ApiResponse(responseCode = "403", description = "Acceso denegado por falta de permisos.")
    @ApiResponse(responseCode = "400", description = "Error de validación (ej: cantidad no positiva).")
    public ResponseEntity<Invitado> crearInvitado(
            @RequestBody InvitadoRequest request,
            @Parameter(description = "ID del usuario (Owner o Staff) que realiza el registro.", required = true)
            @RequestHeader(value = "X-User-ID") Long usuarioId) {
        try {
            Invitado invitado = invitadoService.crearInvitado(request, usuarioId);
            return new ResponseEntity<>(invitado, HttpStatus.CREATED); // 201
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST); // 400
        }
    }

    @PutMapping("/{idInvitado}")
    @Operation(summary = "Modifica nombre y correo de un invitado.",
            description = "Solo permite la modificación de datos personales. Se requiere permiso 'registrar_invitados' o ser Owner.")
    @ApiResponse(responseCode = "200", description = "Invitado modificado con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado por falta de permisos.")
    @ApiResponse(responseCode = "404", description = "Invitado no encontrado.")
    public ResponseEntity<Invitado> modificarInvitado(
            @Parameter(description = "ID del invitado a modificar.")
            @PathVariable Long idInvitado,
            @RequestBody InvitadoRequest request,
            @Parameter(description = "ID del usuario (Owner o Staff).", required = true)
            @RequestHeader(value = "X-User-ID") Long usuarioId) {

        try {
            Invitado modificado = invitadoService.modificarInvitado(idInvitado, usuarioId, request);
            return ResponseEntity.ok(modificado); // 200
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (RuntimeException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND); // 404
        }
    }

    @PutMapping("/{idInvitado}/cantidad")
    @Operation(summary = "Modifica la cantidad de entradas asociadas a un invitado.",
            description = "Solo es posible si las entradas aún no han sido emitidas (EstadoEnvio: PENDIENTE). Requiere permiso 'registrar_invitados' o ser Owner.")
    @ApiResponse(responseCode = "200", description = "Cantidad modificada con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado por falta de permisos.")
    @ApiResponse(responseCode = "400", description = "No se puede modificar si ya se emitieron, o cantidad inválida.")
    public ResponseEntity<Invitado> modificarCantidad(
            @Parameter(description = "ID del invitado.")
            @PathVariable Long idInvitado,
            @Parameter(description = "Nueva cantidad de entradas.")
            @RequestParam Integer cantidad,
            @Parameter(description = "ID del usuario (Owner o Staff).", required = true)
            @RequestHeader(value = "X-User-ID") Long usuarioId) {

        try {
            Invitado modificado = invitadoService.modificarCantidadEntradas(idInvitado, usuarioId, cantidad);
            return ResponseEntity.ok(modificado); // 200
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (RuntimeException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST); // 400 (por reglas de negocio)
        }
    }

    @GetMapping("/tipo-entrada/{idTipoEntrada}")
    @Operation(summary = "Lista los invitados por Tipo de Entrada.",
            description = "Lista todos los invitados asociados a un TipoEntrada específico.")
    @ApiResponse(responseCode = "200", description = "Lista de invitados.")
    @ApiResponse(responseCode = "204", description = "No hay invitados registrados para este tipo de entrada.")
    public ResponseEntity<List<Invitado>> listarInvitadosPorTipoEntrada(
            @Parameter(description = "ID del tipo de entrada.")
            @PathVariable Long idTipoEntrada) {
        List<Invitado> invitados = invitadoService.buscarInvitadosPorTipoEntrada(idTipoEntrada);
        if (invitados.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(invitados); // 200
    }

    @DeleteMapping("/{idInvitado}")
    @Operation(summary = "Elimina un invitado y sus tickets asociados.",
            description = "Elimina el registro del invitado, borra todas sus EntradasEmitidas y recupera el stock de 'cantidadEmitida'. Requiere permiso 'registrar_invitados' o ser Owner.")
    @ApiResponse(responseCode = "204", description = "Invitado eliminado con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado por falta de permisos.")
    @ApiResponse(responseCode = "404", description = "Invitado no encontrado.")
    public ResponseEntity<Void> eliminarInvitado(
            @Parameter(description = "ID del invitado a eliminar.")
            @PathVariable Long idInvitado,
            @Parameter(description = "ID del usuario (Owner o Staff).", required = true)
            @RequestHeader(value = "X-User-ID") Long usuarioId) {
        try {
            invitadoService.eliminarInvitado(idInvitado, usuarioId);
            return ResponseEntity.noContent().build(); // 204
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (RuntimeException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND); // 404
        }
    }

    // --- Endpoints de EMISIÓN (Exclusivo OWNER) ---

    @PostMapping("/emitir/{idInvitado}")
    @Operation(summary = "Emite tickets y los envía por correo a un invitado ya registrado.",
            description = "Genera los códigos QR, actualiza el stock y dispara la notificación de envío. EXCLUSIVO OWNER.")
    @ApiResponse(responseCode = "200", description = "Tickets emitidos y envío iniciado (EstadoEnvio: ENVIADO o ERROR_ENVIO).")
    @ApiResponse(responseCode = "403", description = "Acceso denegado. No es el Owner.")
    @ApiResponse(responseCode = "400", description = "Error de stock insuficiente o ya fueron emitidos previamente.")
    public ResponseEntity<Invitado> emitirEntradasRegistradas(
            @Parameter(description = "ID del invitado al que se le emitirán los tickets.")
            @PathVariable Long idInvitado,
            @Parameter(description = "ID del usuario Owner del evento.", required = true)
            @RequestHeader(value = "X-User-ID") Long ownerId) {
        try {
            Invitado invitadoEmitido = invitadoService.emitirEntradasPorId(idInvitado, ownerId);
            return ResponseEntity.ok(invitadoEmitido); // 200
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (RuntimeException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST); // 400 (Stock, ya emitido)
        }
    }

    @PostMapping("/emitir/tipo-entrada/{idTipoEntrada}")
    @Operation(summary = "Emisión Masiva Automática por Tipo de Entrada.",
            description = "Busca TODOS los invitados de un tipo de entrada que estén PENDIENTES o con ERROR y procesa su emisión. EXCLUSIVO OWNER.")
    @ApiResponse(responseCode = "200", description = "Lista de invitados procesados.")
    @ApiResponse(responseCode = "204", description = "No había invitados pendientes para procesar.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado. No es el Owner.")
    @ApiResponse(responseCode = "400", description = "Error de stock insuficiente.")
    public ResponseEntity<List<Invitado>> emitirEntradasMasivasPorTipo(
            @Parameter(description = "ID del Tipo de Entrada.")
            @PathVariable Long idTipoEntrada,
            @Parameter(description = "ID del usuario Owner del evento.", required = true)
            @RequestHeader(value = "X-User-ID") Long ownerId) {
        
        try {
            List<Invitado> invitadosProcesados = invitadoService.emitirEntradasMasivas(idTipoEntrada, ownerId);
            
            if (invitadosProcesados.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(invitadosProcesados); // 200 OK
            
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN); // 403
        } catch (RuntimeException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST); // 400 (Stock insuficiente)
        }
    }
}