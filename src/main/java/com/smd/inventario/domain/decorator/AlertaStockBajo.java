package com.smd.inventario.domain.decorator;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Avisa (log WARN) si, después de registrar el movimiento, el stock en depósito
 * queda por debajo del umbral. Nunca aborta la operación.
 *
 * <p>shortcut: solo loguea; cuando exista el Observer de C, publicar el aviso ahí.</p>
 */
public final class AlertaStockBajo extends DecoradorRegistro {

    private static final Logger log = LoggerFactory.getLogger(AlertaStockBajo.class);

    private final RepositorioItems repositorioItems;
    private final int umbralMinimo;

    public AlertaStockBajo(IRegistroMovimiento siguiente, RepositorioItems repositorioItems, int umbralMinimo) {
        super(siguiente);
        this.repositorioItems = repositorioItems;
        this.umbralMinimo = umbralMinimo;
    }

    @Override
    public void registrar(EscaneoDTO solicitud, Movimiento movimiento) {
        siguiente.registrar(solicitud, movimiento);
        int stock = repositorioItems.stockDisponible();
        if (stock < umbralMinimo) {
            log.warn("Stock bajo: {} unidades en deposito (minimo {}) tras {} de {}",
                    stock, umbralMinimo, movimiento.tipo(), movimiento.codigoItem());
        }
    }
}
