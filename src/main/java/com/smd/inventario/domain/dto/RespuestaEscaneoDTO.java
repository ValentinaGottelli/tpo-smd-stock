package com.smd.inventario.domain.dto;

import com.smd.inventario.domain.model.EstadoProducto;

import java.time.LocalDateTime;

/**
 * Resultado de una operación de escaneo.
 *
 * @param codigo         código del ítem afectado
 * @param estadoAnterior estado antes de la operación
 * @param estadoNuevo    estado después de la operación
 * @param cantidad       unidades afectadas
 * @param mensaje        descripción legible del resultado
 * @param fechaHora      momento en que se registró el movimiento
 */
public record RespuestaEscaneoDTO(
        String codigo,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad,
        String mensaje,
        LocalDateTime fechaHora
) { }
