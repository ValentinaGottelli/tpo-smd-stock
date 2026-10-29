package com.smd.inventario.domain.model;

import java.time.LocalDateTime;

/**
 * Registro inmutable de un movimiento de stock, usado para la auditoría.
 *
 * @param codigoItem     código del ítem escaneado
 * @param tipo           operación realizada
 * @param canal          canal de venta; {@code null} en INGRESO y AJUSTE
 * @param usuario        operario que realizó el escaneo
 * @param fechaHora      momento del movimiento
 * @param estadoAnterior estado antes de la operación
 * @param estadoNuevo    estado después de la operación
 * @param cantidad       unidades afectadas
 */
public record Movimiento(
        String codigoItem,
        TipoMovimiento tipo,
        CanalVenta canal,
        String usuario,
        LocalDateTime fechaHora,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad
) { }
