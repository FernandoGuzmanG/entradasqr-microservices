package com.microservice.eventos.repository;

import com.microservice.eventos.model.StaffEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffEventoRepository extends JpaRepository<StaffEvento, Long> {
    Optional<StaffEvento> findByEvento_IdEventoAndUsuarioId(Long idEvento, Long usuarioId);
    @Query("SELECT se.evento.idEvento FROM StaffEvento se WHERE se.usuarioId = :usuarioId AND se.activo = true")
    List<Long> findActiveEventIdsByUsuarioId(@Param("usuarioId") Long usuarioId);
}