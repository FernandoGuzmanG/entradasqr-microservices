package com.microservice.ticketing.repository;

import com.microservice.ticketing.model.Invitado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitadoRepository extends JpaRepository<Invitado, Long> {
    List<Invitado> findAllByIdTipoEntrada(Long idTipoEntrada);
}