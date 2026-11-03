package com.smd.inventario.domain.decorator;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.port.RepositorioMovimientos;

/**
 * Componente concreto del Decorator: persiste el movimiento para la auditoría.
 */
public final class RegistroMovimientoBase implements IRegistroMovimiento {

    private final RepositorioMovimientos repositorioMovimientos;

    public RegistroMovimientoBase(RepositorioMovimientos repositorioMovimientos) {
        this.repositorioMovimientos = repositorioMovimientos;
    }

    @Override
    public void registrar(EscaneoDTO solicitud, Movimiento movimiento) {
        repositorioMovimientos.guardar(movimiento);
    }
}
