package com.smd.inventario.presentation.dto;

import java.time.LocalDateTime;

/**
 * Respuesta del endpoint de verificación {@code GET /api/health}.
 *
 * @param estado           resultado del chequeo ("OK" o "ERROR").
 * @param aplicacion       nombre lógico de la aplicación.
 * @param perfiles         perfiles de Spring activos.
 * @param baseDeDatos      motor de base de datos en uso.
 * @param itemsRegistrados cantidad de filas leídas desde la base.
 * @param fechaHora        momento del chequeo.
 */
public record HealthResponse(
        String estado,
        String aplicacion,
        String perfiles,
        String baseDeDatos,
        long itemsRegistrados,
        LocalDateTime fechaHora
) {
}
