package com.smd.inventario.domain.state;

import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * El ítem ya salió del depósito: es un estado final.
 *
 * <p>Ninguna operación es válida.</p>
 */
public final class EstadoDespachado implements EstadoItem {

    @Override
    public EstadoProducto valor() {
        return EstadoProducto.DESPACHADO;
    }

    @Override
    public void ingresar(ItemInventario item) {
        throw rechazo(item, "ingresar");
    }

    @Override
    public void reservar(ItemInventario item) {
        throw rechazo(item, "reservar");
    }

    @Override
    public void cancelarReserva(ItemInventario item) {
        throw rechazo(item, "cancelar la reserva");
    }

    @Override
    public void despachar(ItemInventario item) {
        throw rechazo(item, "despachar");
    }

    private TransicionInvalidaException rechazo(ItemInventario item, String operacion) {
        return new TransicionInvalidaException(item.getCodigo(), valor(), operacion);
    }
}
