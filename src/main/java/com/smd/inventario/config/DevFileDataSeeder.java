package com.smd.inventario.config;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.TipoItem;
import com.smd.inventario.persistence.entity.ItemInventarioEntity;
import com.smd.inventario.persistence.repository.ItemInventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    private final ItemInventarioRepository itemInventarioRepository;

    public DevFileDataSeeder(ItemInventarioRepository itemInventarioRepository) {
        this.itemInventarioRepository = itemInventarioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existentes = itemInventarioRepository.count();

        if (existentes > 0) {
            log.info("Base H2 en archivo con {} items: no se cargan datos de ejemplo.", existentes);
            return;
        }

        itemInventarioRepository.saveAll(List.of(
                new ItemInventarioEntity(
                        "COLCHON-QR-0001", TipoItem.COLCHON,
                        EstadoProducto.EN_DEPOSITO, 1, LocalDate.of(2026, 9, 1)),
                new ItemInventarioEntity(
                        "COLCHON-QR-0002", TipoItem.COLCHON,
                        EstadoProducto.EN_DEPOSITO, 1, LocalDate.of(2026, 9, 5)),
                new ItemInventarioEntity(
                        "LOTE-QR-0001", TipoItem.LOTE,
                        EstadoProducto.PRODUCIDO, 5, LocalDate.of(2026, 9, 10))
        ));

        log.info("Base H2 en archivo inicializada con 3 items de ejemplo.");
    }
}
