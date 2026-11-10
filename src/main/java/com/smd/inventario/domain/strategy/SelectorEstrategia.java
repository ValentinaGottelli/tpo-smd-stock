package com.smd.inventario.domain.strategy;

import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.CanalVenta;

import java.util.EnumMap;
import java.util.Map;

/**
 * Selector de estrategias de reserva segun el canal de venta.
 */
public class SelectorEstrategia {

    private final Map<CanalVenta, EstrategiaReserva> estrategias = new EnumMap<>(CanalVenta.class);

    public SelectorEstrategia() {
        estrategias.put(CanalVenta.DREAMO_LOCAL, new ReservaMinorista(CanalVenta.DREAMO_LOCAL));
        estrategias.put(CanalVenta.DREAMO_MELI, new ReservaMinorista(CanalVenta.DREAMO_MELI));
        estrategias.put(CanalVenta.DREAMO_FRAVEGA, new ReservaMinorista(CanalVenta.DREAMO_FRAVEGA));
        estrategias.put(CanalVenta.RUBIN, new ReservaMayorista());
    }

    public EstrategiaReserva para(CanalVenta canal) {
        if (canal == null) {
            throw new ReglaDeNegocioException("El canal de venta es obligatorio para realizar una reserva.");
        }
        EstrategiaReserva estrategia = estrategias.get(canal);
        if (estrategia == null) {
            throw new ReglaDeNegocioException("No existe estrategia de reserva configurada para el canal: " + canal);
        }
        return estrategia;
    }
}
