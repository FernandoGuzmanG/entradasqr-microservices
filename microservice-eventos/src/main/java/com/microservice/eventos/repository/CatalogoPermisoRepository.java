package com.microservice.eventos.repository;

import com.microservice.eventos.model.CatalogoPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoPermisoRepository extends JpaRepository<CatalogoPermiso, Long> {
    Optional<CatalogoPermiso> findByNombrePermiso(String nombrePermiso);
}