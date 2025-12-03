package com.microservice.ticketing.repository;

import com.microservice.ticketing.model.TipoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TipoEntradaRepository extends JpaRepository<TipoEntrada, Long> {
    List<TipoEntrada> findByNombreContainingIgnoreCase(String nombre);
    List<TipoEntrada> findAllByIdEvento(Long idEvento);
}
