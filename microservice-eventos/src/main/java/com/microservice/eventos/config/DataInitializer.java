package com.microservice.eventos.config;

import com.microservice.eventos.model.Evento;
import com.microservice.eventos.model.Evento.EstadoEvento;
import com.microservice.eventos.model.CatalogoPermiso;
import com.microservice.eventos.repository.EventoRepository;
import com.microservice.eventos.repository.CatalogoPermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EventoRepository eventoRepository;
    private final CatalogoPermisoRepository catalogoPermisoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (catalogoPermisoRepository.count() == 0) {
            List<String> permisos = List.of("escanear_entrada", "registrar_invitados");
            permisos.stream()
                    .map(nombre -> new CatalogoPermiso(null, nombre))
                    .forEach(catalogoPermisoRepository::save);
            System.out.println("✅ Catálogo de permisos precargado.");
        }

        if (eventoRepository.count() == 0) {
            Evento eventoPrueba = Evento.builder()
                    .ownerId(1L) // ID del usuario de prueba (admin@eventoapp.com)
                    .nombre("Concierto de Prueba")
                    .categoria("Música")
                    .descripcion("Evento precargado para tests de API.")
                    .direccion("Calle Falsa 123")
                    .fecha(LocalDate.now().plusDays(30)) // Evento en 30 días
                    .horaInicio(LocalTime.of(20, 0, 0))
                    .horaCierrePuertas(LocalTime.of(20, 30, 0))
                    .horaTermino(LocalTime.of(23, 0, 0))
                    .capacidadMaxima(250)
                    .estado(EstadoEvento.Publicado)
                    .build();

            eventoRepository.save(eventoPrueba);
            System.out.println("✅ Evento de prueba precargado (ID: 1).");
        }
    }
}