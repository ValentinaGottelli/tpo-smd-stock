package com.smd.inventario.presentation.dto;

import java.time.LocalDateTime;

/**
 * Cuerpo estándar de error de la API.
 *
 * @param codigo    código estable para el cliente (por ejemplo {@code TRANSICION_INVALIDA})
 * @param mensaje   descripción legible
 * @param fechaHora momento del error
 * @param path      ruta que originó el error
 */
public record ErrorResponse(
        String codigo,
        String mensaje,
        LocalDateTime fechaHora,
        String path
) { }
