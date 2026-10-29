package com.smd.inventario.domain.state;

import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * El ítem está disponible en el depósito.
 *
 * <p>Única operación válida: {@link #reservar(ItemInventario)}.</p>
 */
public final class EstadoEnDeposito implements EstadoItem {

    @Override
    public EstadoProducto valor() {
        return EstadoProducto.EN_DEPOSITO;
    }

    @Override
    public void ingresar(ItemInventario item) {
        throw rechazo(item, "ingresar");
    }

    @Override
    public void reservar(ItemInventario item) {
        item.cambiarEstado(EstadoProducto.RESERVADO);
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
