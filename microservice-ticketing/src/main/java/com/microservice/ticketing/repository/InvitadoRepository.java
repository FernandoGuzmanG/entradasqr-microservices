package com.microservice.ticketing.repository;

import com.microservice.ticketing.model.Invitado;
import com.microservice.ticketing.model.Invitado.EstadoEnvio;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitadoRepository extends JpaRepository<Invitado, Long> {
    List<Invitado> findAllByIdTipoEntrada(Long idTipoEntrada, Sort sort);

    List<Invitado> findAllByIdTipoEntradaAndEstadoEnvioIn(Long idTipoEntrada, List<EstadoEnvio> estados);

    @Query("SELECT i FROM Invitado i WHERE i.idTipoEntrada = :idTipoEntrada " +
           "AND (:termino IS NULL OR :termino = '' OR " +
           "LOWER(i.nombreCompleto) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(i.correo) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Invitado> buscarPorTermino(
            @Param("idTipoEntrada") Long idTipoEntrada,
            @Param("termino") String termino,
            Sort sort);
}