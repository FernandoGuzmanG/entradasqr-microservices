package com.microservice.ticketing.config;

import com.microservice.ticketing.model.TipoEntrada;
import com.microservice.ticketing.model.TipoEntrada.EstadoTipoEntrada;
import com.microservice.ticketing.repository.TipoEntradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // Necesitas el repositorio de TipoEntrada
    private final TipoEntradaRepository tipoEntradaRepository;
    
    // NOTA: Para obtener las fechas de inicio, necesitarás cargar los Eventos 1 y 2
    // o asumir los IDs y las fechas que están en el DataInitializer de Eventos.
    // Asumiremos que EVENTO_ID_1 = 1 y EVENTO_ID_2 = 2.
    private final Long EVENTO_ID_1 = 1L; // Concierto de Prueba (Fecha +30 días)
    private final Long EVENTO_ID_2 = 2L; // Feria Gastronómica (Fecha +15 días)
    
    // Estas fechas se deben ajustar al momento de ejecución, pero para el código
    // usaremos la lógica de desfase.
    private final LocalDate FECHA_EVENTO_1 = LocalDate.now().plusDays(30);
    private final LocalDate FECHA_EVENTO_2 = LocalDate.now().plusDays(15);


    @Override
    public void run(String... args) throws Exception {
        
        // --- 1. Entradas para Evento 1: Concierto de Prueba (ID: 1) ---

        // Tipo 1.1: General (Empieza 2 semanas antes)
        // CORRECCIÓN: Quitamos findById(1L) y lo reemplazamos por una búsqueda que garantice unicidad por nombre/evento
        if (tipoEntradaRepository.findAllByIdEvento(EVENTO_ID_1).stream().noneMatch(t -> t.getNombre().contains("General"))) {
            TipoEntrada tipo1_1 = TipoEntrada.builder()
                    // REMOVIDO: .idTipoEntrada(1L)
                    .idEvento(EVENTO_ID_1)
                    .nombre("General - Fase 1")
                    .descripcion("Acceso básico al concierto.")
                    .precio(new BigDecimal("28990")) // Precio actualizado a CLP
                    .cantidadTotal(150)
                    .cantidadEmitida(0)
                    .fechaInicioVenta(FECHA_EVENTO_1.minus(14, ChronoUnit.DAYS).atStartOfDay()) // Empieza 2 semanas antes
                    .fechaFinVenta(FECHA_EVENTO_1.atTime(LocalTime.of(19, 0))) // Cierra 1 hora antes del inicio (20:00)
                    .estado(EstadoTipoEntrada.ACTIVO)
                    .build();
            tipoEntradaRepository.save(tipo1_1);
            System.out.println("✅ Tipo Entrada 1 (General) creado para Evento 1.");
        }

        // Tipo 1.2: VIP (Empieza 1 semana antes, Stock limitado)
        if (tipoEntradaRepository.findAllByIdEvento(EVENTO_ID_1).stream().noneMatch(t -> t.getNombre().contains("VIP"))) {
             TipoEntrada tipo1_2 = TipoEntrada.builder()
                    // REMOVIDO: .idTipoEntrada(2L)
                    .idEvento(EVENTO_ID_1)
                    .nombre("VIP - Lounge")
                    .descripcion("Acceso a zona lounge privada.")
                    .precio(new BigDecimal("59990")) // Precio actualizado a CLP
                    .cantidadTotal(50)
                    .cantidadEmitida(0)
                    .fechaInicioVenta(FECHA_EVENTO_1.minus(7, ChronoUnit.DAYS).atStartOfDay()) // Empieza 1 semana antes
                    .fechaFinVenta(FECHA_EVENTO_1.atTime(LocalTime.of(19, 0))) 
                    .estado(EstadoTipoEntrada.ACTIVO)
                    .build();
            tipoEntradaRepository.save(tipo1_2);
            System.out.println("✅ Tipo Entrada 2 (VIP) creado para Evento 1.");
        }


        // --- 2. Entradas para Evento 2: Feria Gastronómica (ID: 2) ---

        // Tipo 2.1: Acceso Día Completo (Empieza 1 semana antes)
        if (tipoEntradaRepository.findAllByIdEvento(EVENTO_ID_2).stream().noneMatch(t -> t.getNombre().contains("Full Day"))) {
            TipoEntrada tipo2_1 = TipoEntrada.builder()
                    // REMOVIDO: .idTipoEntrada(3L)
                    .idEvento(EVENTO_ID_2)
                    .nombre("Acceso Full Day")
                    .descripcion("Acceso de 12:00 a 20:00.")
                    .precio(new BigDecimal("9990")) // Precio actualizado a CLP
                    .cantidadTotal(300)
                    .cantidadEmitida(0)
                    .fechaInicioVenta(FECHA_EVENTO_2.minus(7, ChronoUnit.DAYS).atStartOfDay()) 
                    .fechaFinVenta(FECHA_EVENTO_2.atTime(LocalTime.of(11, 0))) // Cierra 1 hora antes del inicio (12:00)
                    .estado(EstadoTipoEntrada.ACTIVO)
                    .build();
            tipoEntradaRepository.save(tipo2_1);
            System.out.println("✅ Tipo Entrada 3 (Full Day) creado para Evento 2.");
        }

        // Tipo 2.2: Pase Staff (Gratuito, Empieza 3 semanas antes)
        if (tipoEntradaRepository.findAllByIdEvento(EVENTO_ID_2).stream().noneMatch(t -> t.getNombre().contains("Staff"))) {
            TipoEntrada tipo2_2 = TipoEntrada.builder()
                    // REMOVIDO: .idTipoEntrada(4L)
                    .idEvento(EVENTO_ID_2)
                    .nombre("Pase Staff (QR)")
                    .descripcion("Pase de identificación para Staff y Proveedores.")
                    .precio(BigDecimal.ZERO)
                    .cantidadTotal(50)
                    .cantidadEmitida(0)
                    .fechaInicioVenta(FECHA_EVENTO_2.minus(21, ChronoUnit.DAYS).atStartOfDay()) 
                    .fechaFinVenta(FECHA_EVENTO_2.atTime(LocalTime.of(10, 0))) 
                    .estado(EstadoTipoEntrada.ACTIVO)
                    .build();
            tipoEntradaRepository.save(tipo2_2);
            System.out.println("✅ Tipo Entrada 4 (Staff) creado para Evento 2.");
        }
    }
}