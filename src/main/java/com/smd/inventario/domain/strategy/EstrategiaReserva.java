package com.smd.inventario.domain.strategy;

import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * Estrategia de validacion de reservas segun el canal de venta (Patron Strategy).
 */
public interface EstrategiaReserva {

    CanalVenta canal();

    /**
     * Valida si el item puede ser reservado por el canal correspondiente.
     *
     * @param item item a reservar
     * @throws com.smd.inventario.domain.exception.ReglaDeNegocioException si no cumple con las reglas del canal
     */
    void validar(ItemInventario item);
}
