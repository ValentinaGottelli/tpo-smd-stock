package com.smd.inventario.domain.strategy;

import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * Estrategia de reserva para canales mayoristas (Rubin).
 * Exige un minimo de 5 unidades.
 */
public class ReservaMayorista implements EstrategiaReserva {

    private static final int MINIMO_MAYORISTA = 5;

    @Override
    public CanalVenta canal() {
        return CanalVenta.RUBIN;
    }

    @Override
    public void validar(ItemInventario item) {
        if (item == null || item.getCantidad() < MINIMO_MAYORISTA) {
            int cantidad = item == null ? 0 : item.getCantidad();
            throw new ReglaDeNegocioException(
                    "La reserva mayorista por canal RUBIN exige un minimo de " + MINIMO_MAYORISTA
                            + " unidades. Cantidad recibida: " + cantidad);
        }
    }
}
