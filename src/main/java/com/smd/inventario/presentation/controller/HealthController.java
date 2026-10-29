package com.smd.inventario.presentation.controller;

import com.smd.inventario.persistence.repository.ItemInventarioRepository;
import com.smd.inventario.presentation.dto.HealthResponse;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Endpoint técnico para verificar que la aplicación y la conexión con la base
 * de datos están operativas.
 *
 * <p>No forma parte del flujo de negocio del TP: cuando se implemente la
 * {@code FachadaEscaneo}, este controlador puede quedarse como chequeo de
 * infraestructura o reemplazarse por Spring Boot Actuator.</p>
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ItemInventarioRepository itemInventarioRepository;
    private final Environment environment;

    public HealthController(ItemInventarioRepository itemInventarioRepository,
                            Environment environment) {
        this.itemInventarioRepository = itemInventarioRepository;
        this.environment = environment;
    }

    @GetMapping
    public HealthResponse verificar() {
        long items = itemInventarioRepository.count();

        return new HealthResponse(
                "OK",
                environment.getProperty("spring.application.name", "smd-stock"),
                perfilesActivos(),
                environment.getProperty("app.database.nombre", "desconocida"),
                items,
                LocalDateTime.now()
        );
    }

    private String perfilesActivos() {
        String[] activos = environment.getActiveProfiles();
        String[] perfiles = activos.length > 0 ? activos : environment.getDefaultProfiles();
        return String.join(",", perfiles);
    }
}
