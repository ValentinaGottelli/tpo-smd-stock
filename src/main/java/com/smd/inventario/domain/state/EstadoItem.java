package com.smd.inventario.domain.state;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * Estado de un ítem dentro del patrón State.
 *
 * <p>Cada implementación sabe qué operaciones acepta y a qué estado lleva.
 * Cuando la operación no corresponde, lanza
 * {@link com.smd.inventario.domain.exception.TransicionInvalidaException}.</p>
 */
public interface EstadoItem {

    EstadoProducto valor();

    void ingresar(ItemInventario item);

    void reservar(ItemInventario item);

    void cancelarReserva(ItemInventario item);

    void despachar(ItemInventario item);
}
