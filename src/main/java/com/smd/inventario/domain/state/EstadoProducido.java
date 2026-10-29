package com.smd.inventario.domain.state;

import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * El ítem salió de producción y todavía no fue ingresado al depósito.
 *
 * <p>Única operación válida: {@link #ingresar(ItemInventario)}.</p>
 */
public final class EstadoProducido implements EstadoItem {

    @Override
    public EstadoProducto valor() {
        return EstadoProducto.PRODUCIDO;
    }

    @Override
    public void ingresar(ItemInventario item) {
        item.cambiarEstado(EstadoProducto.EN_DEPOSITO);
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
