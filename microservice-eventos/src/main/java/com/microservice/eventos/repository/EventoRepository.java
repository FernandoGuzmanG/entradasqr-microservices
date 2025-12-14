package com.microservice.eventos.repository;


import com.microservice.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    long countByOwnerId(Long ownerId);

    @Query("SELECT DISTINCT e FROM Evento e " +
           "LEFT JOIN StaffEvento se ON se.evento.idEvento = e.idEvento " +
           "WHERE (e.ownerId = :userId OR (se.usuarioId = :userId AND se.activo = true)) " +
           "AND (e.fecha > :currentDate OR (e.fecha = :currentDate AND e.horaInicio >= :currentTime)) " +
           "ORDER BY e.fecha ASC, e.horaInicio ASC")
    List<Evento> findProximosEventos(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime);

    List<Evento> findAllByOwnerId(Long ownerId);
    List<Evento> findAllByIdEventoInOrderByFechaAsc(List<Long> idEventos);
    List<Evento> findAllByNombreContainingIgnoreCaseAndIdEventoInOrderByFechaAsc(String nombre, List<Long> idEventos);

    @Query("SELECT e FROM Evento e WHERE e.estado = :estadoPublicado " +
            "AND (e.fecha < :currentDate " +
            "OR (e.fecha = :currentDate AND e.horaTermino <= :currentTime))")
    List<Evento> findEventosToFinalize(
            @Param("estadoPublicado") String estadoPublicado,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime);
}