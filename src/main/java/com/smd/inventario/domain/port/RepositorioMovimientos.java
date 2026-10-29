package com.smd.inventario.domain.port;

import com.smd.inventario.domain.model.Movimiento;

import java.util.List;

/**
 * Puerto de salida del dominio hacia la auditoría de movimientos.
 */
public interface RepositorioMovimientos {

    void guardar(Movimiento movimiento);

    List<Movimiento> ultimos(int limite);
}
