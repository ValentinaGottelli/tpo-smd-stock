package com.smd.inventario.domain.composite;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventarioBase;

import java.time.LocalDate;

/**
 * Hoja del patrón Composite: representa un colchón físico.
 *
 * <p>Siempre aporta una unidad al inventario, sin importar su estado.</p>
 */
public final class Colchon extends ItemInventarioBase {

    public Colchon(String codigo, EstadoProducto estado, LocalDate fechaProduccion) {
        super(codigo, estado, fechaProduccion);
    }

    @Override
    public int getCantidad() {
        return 1;
    }
}
