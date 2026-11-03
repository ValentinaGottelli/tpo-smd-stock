package com.smd.inventario.domain.factory;

import com.smd.inventario.domain.decorator.AlertaStockBajo;
import com.smd.inventario.domain.decorator.IRegistroMovimiento;
import com.smd.inventario.domain.decorator.RegistroMovimientoBase;
import com.smd.inventario.domain.decorator.ValidacionEmbalaje;
import com.smd.inventario.domain.decorator.ValidacionFIFO;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;

/**
 * Simple Factory: arma la cadena de decoradores que corresponde a cada tipo de
 * movimiento.
 *
 * <pre>
 * INGRESO                         -> ValidacionEmbalaje -> base
 * DESPACHO                        -> ValidacionFIFO -> AlertaStockBajo -> base
 * RESERVA / CANCELACION / AJUSTE  -> base
 * </pre>
 */
public final class FabricaMovimiento {

    private final RepositorioMovimientos repositorioMovimientos;
    private final RepositorioItems repositorioItems;
    private final int umbralMinimo;

    public FabricaMovimiento(RepositorioMovimientos repositorioMovimientos,
                             RepositorioItems repositorioItems,
                             int umbralMinimo) {
        this.repositorioMovimientos = repositorioMovimientos;
        this.repositorioItems = repositorioItems;
        this.umbralMinimo = umbralMinimo;
    }

    public IRegistroMovimiento crear(TipoMovimiento tipo) {
        IRegistroMovimiento base = new RegistroMovimientoBase(repositorioMovimientos);
        return switch (tipo) {
            case INGRESO -> new ValidacionEmbalaje(base);
            case DESPACHO -> new ValidacionFIFO(
                    new AlertaStockBajo(base, repositorioItems, umbralMinimo), repositorioItems);
            case RESERVA, CANCELACION, AJUSTE -> base;
        };
    }
}
