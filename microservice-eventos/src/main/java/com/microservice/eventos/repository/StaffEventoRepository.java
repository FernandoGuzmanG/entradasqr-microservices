package com.microservice.eventos.repository;

import com.microservice.eventos.model.StaffEvento;
import com.microservice.eventos.model.StaffEvento.EstadoInvitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StaffEventoRepository extends JpaRepository<StaffEvento, Long> {

    Optional<StaffEvento> findByEvento_IdEventoAndUsuarioId(Long idEvento, Long usuarioId);

    @Query("SELECT se.evento.idEvento FROM StaffEvento se WHERE se.usuarioId = :usuarioId AND se.activo = true")
    Set<Long> findActiveEventIdsByUsuarioId(@Param("usuarioId") Long usuarioId);

    long countByUsuarioIdAndActivoTrue(Long usuarioId);

    long countByUsuarioIdAndEstadoInvitacion(Long usuarioId, EstadoInvitacion estadoInvitacion);

    List<StaffEvento> findByUsuarioIdAndEstadoInvitacion(Long usuarioId, EstadoInvitacion estadoInvitacion);
    
    List<StaffEvento> findAllByEvento_IdEvento(Long idEvento);
}