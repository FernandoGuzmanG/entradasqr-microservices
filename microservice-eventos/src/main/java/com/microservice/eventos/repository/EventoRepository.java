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