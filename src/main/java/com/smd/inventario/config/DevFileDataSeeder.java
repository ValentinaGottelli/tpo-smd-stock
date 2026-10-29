package com.smd.inventario.config;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.service.GestorDeStock;
import com.smd.inventario.domain.service.ServicioConsultas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga datos de ejemplo al arrancar con el perfil {@code devfile}.
 *
 * <p>A diferencia de {@code data.sql}, este seeder corre una sola vez: si la
 * base ya tiene ítems no inserta nada, para no duplicar registros ni pisar los
 * cambios hechos en ejecuciones anteriores.</p>
 */
@Component
@Profile("devfile")
public class DevFileDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevFileDataSeeder.class);

    private final GestorDeStock gestor;
    private final ServicioConsultas consultas;

    public DevFileDataSeeder(GestorDeStock gestor, ServicioConsultas consultas) {
        this.gestor = gestor;
        this.consultas = consultas;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!consultas.listar(null).isEmpty()) {
            log.info("Base H2 en archivo con datos: no se cargan items de ejemplo.");
            return;
        }

        gestor.registrarColchon(
                "COLCHON-QR-0001", EstadoProducto.EN_DEPOSITO, LocalDate.of(2026, 9, 1));
        gestor.registrarColchon(
                "COLCHON-QR-0002", EstadoProducto.EN_DEPOSITO, LocalDate.of(2026, 9, 5));
        gestor.registrarLote(
                "LOTE-QR-0001", EstadoProducto.EN_DEPOSITO, LocalDate.of(2026, 9, 10), 5);

        log.info("Base H2 en archivo inicializada con 3 items raiz y un lote de 5 colchones.");
    }
}
