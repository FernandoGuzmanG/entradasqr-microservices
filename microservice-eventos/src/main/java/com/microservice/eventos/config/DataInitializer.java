package com.microservice.eventos.config;

import com.microservice.eventos.model.CatalogoPermiso;
import com.microservice.eventos.model.Evento;
import com.microservice.eventos.model.Evento.EstadoEvento;
import com.microservice.eventos.model.StaffEvento;
import com.microservice.eventos.model.StaffEvento.EstadoInvitacion;
import com.microservice.eventos.repository.CatalogoPermisoRepository;
import com.microservice.eventos.repository.EventoRepository;
import com.microservice.eventos.repository.StaffEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EventoRepository eventoRepository;
    private final CatalogoPermisoRepository catalogoPermisoRepository;
    private final StaffEventoRepository staffEventoRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Precargar Permisos
        if (catalogoPermisoRepository.count() == 0) {
            List<String> permisos = List.of("escanear_entrada", "registrar_invitados");
            permisos.stream()
                    .map(nombre -> new CatalogoPermiso(null, nombre))
                    .forEach(catalogoPermisoRepository::save);
            System.out.println("✅ Catálogo de permisos precargado.");
        }

        // Obtener todos los permisos para asignarlos a las invitaciones de prueba
        Set<CatalogoPermiso> todosLosPermisos = new HashSet<>(catalogoPermisoRepository.findAll());

        // 2. Precargar Evento 1 (Owner: Usuario 1)
        Evento evento1 = eventoRepository.findById(1L).orElse(null);
        if (evento1 == null) {
            evento1 = Evento.builder()
                    .ownerId(1L) // Usuario 1 es Owner
                    .nombre("Concierto de Prueba (Owner 1)")
                    .categoria("Música")
                    .descripcion("Evento precargado del usuario 1.")
                    .direccion("Calle Falsa 123")
                    .fecha(LocalDate.now().plusDays(30))
                    .horaInicio(LocalTime.of(20, 0, 0))
                    .horaCierrePuertas(LocalTime.of(20, 30, 0))
                    .horaTermino(LocalTime.of(23, 0, 0))
                    .capacidadMaxima(250)
                    .estado(EstadoEvento.Publicado)
                    .build();

            evento1 = eventoRepository.save(evento1);
            System.out.println("✅ Evento 1 precargado (ID: " + evento1.getIdEvento() + ")");
        }

        // 3. Precargar Evento 2 (Owner: Usuario 2)
        Evento evento2 = eventoRepository.findById(2L).orElse(null);
        if (evento2 == null) {
            evento2 = Evento.builder()
                    .ownerId(2L) // Usuario 2 es Owner
                    .nombre("Feria Gastronómica (Owner 2)")
                    .categoria("Comida")
                    .descripcion("Evento precargado del usuario 2.")
                    .direccion("Avenida Siempre Viva 742")
                    .fecha(LocalDate.now().plusDays(15))
                    .horaInicio(LocalTime.of(12, 0, 0))
                    .horaCierrePuertas(LocalTime.of(18, 0, 0))
                    .horaTermino(LocalTime.of(20, 0, 0))
                    .capacidadMaxima(500)
                    .estado(EstadoEvento.Publicado)
                    .build();

            evento2 = eventoRepository.save(evento2);
            System.out.println("✅ Evento 2 precargado (ID: " + evento2.getIdEvento() + ")");
        }

        // 4. Invitación Cruzada: Usuario 2 es invitado como Staff al Evento 1
        if (staffEventoRepository.findByEvento_IdEventoAndUsuarioId(evento1.getIdEvento(), 2L).isEmpty()) {
            StaffEvento invitacionParaUsuario2 = StaffEvento.builder()
                    .evento(evento1)
                    .usuarioId(2L) // Invitado
                    .fechaAsignacion(LocalDateTime.now())
                    .activo(false) // No activo hasta que acepte
                    .estadoInvitacion(EstadoInvitacion.PENDIENTE)
                    .permisos(todosLosPermisos)
                    .build();
            
            staffEventoRepository.save(invitacionParaUsuario2);
            System.out.println("✅ Invitación creada: Usuario 2 -> Evento 1 (PENDIENTE)");
        }

        // 5. Invitación Cruzada: Usuario 1 es invitado como Staff al Evento 2
        if (staffEventoRepository.findByEvento_IdEventoAndUsuarioId(evento2.getIdEvento(), 1L).isEmpty()) {
            StaffEvento invitacionParaUsuario1 = StaffEvento.builder()
                    .evento(evento2)
                    .usuarioId(1L) // Invitado
                    .fechaAsignacion(LocalDateTime.now())
                    .activo(false)
                    .estadoInvitacion(EstadoInvitacion.PENDIENTE)
                    .permisos(todosLosPermisos)
                    .build();

            staffEventoRepository.save(invitacionParaUsuario1);
            System.out.println("✅ Invitación creada: Usuario 1 -> Evento 2 (PENDIENTE)");
        }
    }
}