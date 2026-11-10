package com.smd.inventario.domain.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observador que registra los movimientos para auditoria y dashboard en tiempo real.
 */
public class DashboardAuditoria implements ObservadorStock {

    private static final Logger log = LoggerFactory.getLogger(DashboardAuditoria.class);

    @Override
    public void alOcurrirMovimiento(EventoStock evento) {
        log.info("[DashboardAuditoria] Auditoria en vivo - Codigo: {}, Tipo: {}, Canal: {}, Usuario: {}, FechaHora: {}",
                evento.movimiento().codigoItem(),
                evento.movimiento().tipo(),
                evento.movimiento().canal(),
                evento.movimiento().usuario(),
                evento.movimiento().fechaHora());
    }
}
