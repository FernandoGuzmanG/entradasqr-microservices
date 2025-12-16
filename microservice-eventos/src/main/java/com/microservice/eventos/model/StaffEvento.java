package com.microservice.eventos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "staff_eventos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"evento_id", "usuario_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relación entre un Evento y un Usuario Staff, incluyendo sus permisos y estado de invitación.")
public class StaffEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la asignación de Staff.", example = "200")
    private Long idStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Referencia al evento asignado.")
    private Evento evento;

    @Column(name = "usuario_id", nullable = false)
    @Schema(description = "ID del usuario asignado como Staff.", example = "105", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long usuarioId;

    @Column(name = "fecha_asignacion")
    @Schema(description = "Fecha y hora en que se realizó la invitación/asignación.")
    private LocalDateTime fechaAsignacion;

    @Column(nullable = false)
    @Schema(description = "Indica si el staff está activo en el evento (debe haber aceptado la invitación).", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_invitacion", nullable = false)
    @Schema(description = "Estado del flujo de invitación (PENDIENTE, ACEPTADO, RECHAZADO).", example = "PENDIENTE")
    private EstadoInvitacion estadoInvitacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "staff_permisos",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Schema(description = "Conjunto de permisos específicos que el Staff tiene para este evento.")
    private Set<CatalogoPermiso> permisos;

    public enum EstadoInvitacion {
        PENDIENTE,
        ACEPTADO,
        RECHAZADO
    }
}