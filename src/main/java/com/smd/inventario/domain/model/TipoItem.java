package com.smd.inventario.domain.model;

/**
 * Tipo de ítem que puede escanearse en el depósito.
 *
 * <p>Se corresponde con la jerarquía del patrón Composite (apartado 4 del TP):
 * un colchón es una unidad física y un lote agrupa varios colchones.</p>
 */
public enum TipoItem {
    COLCHON,
    LOTE
}
