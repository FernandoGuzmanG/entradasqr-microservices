package com.microservice.ticketing.controller;

import com.microservice.ticketing.dto.TipoEntradaRequest;
import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.service.TipoEntradaService;
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
@RequestMapping("/api/tipos-entrada")
@RequiredArgsConstructor
@Tag(name = "Tipos de Entrada", description = "Gestión de la configuración de tickets por evento (Stock, Precios, Venta).")
public class TipoEntradaController {

    private final TipoEntradaService tipoEntradaService;

    // --- Endpoints de CRUD y Búsqueda ---

    @PostMapping
    @Operation(summary = "Crea un nuevo tipo de ticket.",
               description = "Solo el Owner del evento puede crear un nuevo tipo de entrada.")
    @ApiResponse(responseCode = "201", description = "Tipo de entrada creado con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario no es el Owner del evento.")
    @ApiResponse(responseCode = "400", description = "Error de validación de datos.")
    public ResponseEntity<TipoEntrada> crearTipoEntrada(
            @RequestBody TipoEntradaRequest request,
            @Parameter(description = "ID del usuario propietario del evento (Owner).", required = true)
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        // Propaga SecurityException y IllegalArgumentException
        TipoEntrada nuevoTipo = tipoEntradaService.crearTipoEntrada(request, ownerId);
        return new ResponseEntity<>(nuevoTipo, HttpStatus.CREATED); // 201
    }

    @GetMapping("/{idTipoEntrada}")
    @Operation(summary = "Obtiene un tipo de entrada específico por ID.")
    @ApiResponse(responseCode = "200", description = "Tipo de entrada encontrado.")
    @ApiResponse(responseCode = "404", description = "Tipo de entrada no encontrado.")
    public ResponseEntity<TipoEntrada> getTipoEntradaById(
            @Parameter(description = "ID del tipo de entrada a buscar.")
            @PathVariable Long idTipoEntrada) {
        
        // Propaga RuntimeException/NoSuchElementException (404)
        TipoEntrada tipo = tipoEntradaService.findById(idTipoEntrada);
        return ResponseEntity.ok(tipo); // 200
    }

    @GetMapping("/evento/{idEvento}")
    @Operation(summary = "Obtiene todos los tipos de entrada para un evento.")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de entrada.")
    @ApiResponse(responseCode = "204", description = "No hay tipos de entrada para el evento.")
    public ResponseEntity<List<TipoEntrada>> buscarTiposPorEvento(
            @Parameter(description = "ID del evento.")
            @PathVariable Long idEvento) {
        
        List<TipoEntrada> tipos = tipoEntradaService.buscarTiposPorEvento(idEvento);
        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(tipos); // 200
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca tipos de entrada por una parte del nombre (LIKE '%%').")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de entrada que coinciden.")
    @ApiResponse(responseCode = "204", description = "No se encontraron coincidencias.")
    public ResponseEntity<List<TipoEntrada>> buscarTiposPorNombre(
            @Parameter(description = "Parte del nombre a buscar.")
            @RequestParam String nombre) {
        
        List<TipoEntrada> tipos = tipoEntradaService.buscarTiposPorNombre(nombre);
        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(tipos); // 200
    }

    @PutMapping("/{idTipoEntrada}")
    @Operation(summary = "Actualiza un tipo de entrada existente.",
               description = "Permite al Owner actualizar stock, precio y fechas de venta.")
    @ApiResponse(responseCode = "200", description = "Tipo de entrada actualizado con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario no es el Owner.")
    @ApiResponse(responseCode = "404", description = "Tipo de entrada no encontrado.")
    public ResponseEntity<TipoEntrada> actualizarTipoEntrada(
            @Parameter(description = "ID del tipo de entrada a modificar.")
            @PathVariable Long idTipoEntrada,
            @RequestBody TipoEntradaRequest request,
            @Parameter(description = "ID del usuario propietario del evento (Owner).", required = true)
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        // Propaga SecurityException y RuntimeException/NoSuchElementException
        TipoEntrada modificado = tipoEntradaService.actualizarTipoEntrada(idTipoEntrada, ownerId, request);
        return ResponseEntity.ok(modificado); // 200
    }

    @DeleteMapping("/{idTipoEntrada}")
    @Operation(summary = "Elimina un tipo de entrada.",
               description = "Solo el Owner puede eliminar. Esto borra de forma permanente todas las entradas emitidas y los registros de invitados asociados.")
    @ApiResponse(responseCode = "204", description = "Tipo de entrada eliminado con éxito.")
    @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario no es el Owner.")
    @ApiResponse(responseCode = "404", description = "Tipo de entrada no encontrado.")
    public ResponseEntity<Void> eliminarTipoEntrada(
            @Parameter(description = "ID del tipo de entrada a eliminar.")
            @PathVariable Long idTipoEntrada,
            @Parameter(description = "ID del usuario propietario del evento (Owner).", required = true)
            @RequestHeader(value = "X-User-ID") Long ownerId) {

        // Propaga SecurityException y RuntimeException/NoSuchElementException
        tipoEntradaService.eliminarTipoEntrada(idTipoEntrada, ownerId);
        return ResponseEntity.noContent().build(); // 204
    }
}