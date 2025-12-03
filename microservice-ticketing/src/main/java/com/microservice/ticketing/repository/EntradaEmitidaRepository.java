package com.microservice.ticketing.repository;

import com.microservice.ticketing.model.EntradaEmitida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntradaEmitidaRepository extends JpaRepository<EntradaEmitida, Long> {
    List<EntradaEmitida> findAllByIdInvitado(Long idInvitado);
    Optional<EntradaEmitida> findByCodigoQR(String codigoQR);
    List<EntradaEmitida> findAllByIdTipoEntrada(Long idTipoEntrada);
}