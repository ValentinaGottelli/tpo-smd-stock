package com.smd.inventario.domain.strategy;

import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * Estrategia de reserva para canales minoristas (dreamo).
 * Permite reservas unitarias (cantidad >= 1).
 */
public class ReservaMinorista implements EstrategiaReserva {

    private final CanalVenta canal;

    public ReservaMinorista(CanalVenta canal) {
        this.canal = canal;
    }

    @Override
    public CanalVenta canal() {
        return canal;
    }

    @Override
    public void validar(ItemInventario item) {
        if (item == null || item.getCantidad() < 1) {
            throw new ReglaDeNegocioException("La reserva minorista requiere al menos 1 unidad.");
        }
    }
}
