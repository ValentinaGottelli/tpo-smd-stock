package com.smd.inventario.presentation.dto;

import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.model.TipoMovimiento;

import java.time.LocalDateTime;

/**
 * Representación HTTP de un movimiento de auditoría.
 */
public record MovimientoResponse(
        String codigoItem,
        TipoMovimiento tipo,
        CanalVenta canal,
        String usuario,
        LocalDateTime fechaHora,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad
) {

    public static MovimientoResponse desde(Movimiento movimiento) {
        return new MovimientoResponse(
                movimiento.codigoItem(),
                movimiento.tipo(),
                movimiento.canal(),
                movimiento.usuario(),
                movimiento.fechaHora(),
                movimiento.estadoAnterior(),
                movimiento.estadoNuevo(),
                movimiento.cantidad());
    }
}
