package com.smd.inventario.persistence.mapper;

import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.persistence.entity.MovimientoEntity;

/**
 * Traduce entre el registro de dominio y la entidad JPA de auditoría.
 */
public final class MovimientoMapper {

    private MovimientoMapper() {
    }

    public static MovimientoEntity aEntidad(Movimiento movimiento) {
        return new MovimientoEntity(
                movimiento.codigoItem(),
                movimiento.tipo(),
                movimiento.canal(),
                movimiento.usuario(),
                movimiento.fechaHora(),
                movimiento.estadoAnterior(),
                movimiento.estadoNuevo(),
                movimiento.cantidad());
    }

    public static Movimiento aDominio(MovimientoEntity entidad) {
        return new Movimiento(
                entidad.getCodigoItem(),
                entidad.getTipo(),
                entidad.getCanal(),
                entidad.getUsuario(),
                entidad.getFechaHora(),
                entidad.getEstadoAnterior(),
                entidad.getEstadoNuevo(),
                entidad.getCantidad());
    }
}
