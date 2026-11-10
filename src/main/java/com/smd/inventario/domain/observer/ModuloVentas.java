package com.smd.inventario.domain.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observador que mantiene el estado y disponibilidad para el modulo de ventas.
 */
public class ModuloVentas implements ObservadorStock {

    private static final Logger log = LoggerFactory.getLogger(ModuloVentas.class);
    private int ultimoStockDisponible;

    @Override
    public void alOcurrirMovimiento(EventoStock evento) {
        this.ultimoStockDisponible = evento.stockDisponible();
        log.info("[ModuloVentas] Stock disponible actualizado: {} unidades tras movimiento {}",
                ultimoStockDisponible, evento.movimiento().tipo());
    }

    public int getUltimoStockDisponible() {
        return ultimoStockDisponible;
    }
}
