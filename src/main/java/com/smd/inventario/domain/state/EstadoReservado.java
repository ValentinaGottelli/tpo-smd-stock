package com.smd.inventario.domain.state;

import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

/**
 * El ítem está reservado por un canal de venta.
 *
 * <p>Operaciones válidas: cancelar la reserva (vuelve a EN_DEPOSITO) o
 * despachar (pasa a DESPACHADO).</p>
 */
public final class EstadoReservado implements EstadoItem {

    @Override
    public EstadoProducto valor() {
        return EstadoProducto.RESERVADO;
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
        item.cambiarEstado(EstadoProducto.EN_DEPOSITO);
    }

    @Override
    public void despachar(ItemInventario item) {
        item.cambiarEstado(EstadoProducto.DESPACHADO);
    }

    private TransicionInvalidaException rechazo(ItemInventario item, String operacion) {
        return new TransicionInvalidaException(item.getCodigo(), valor(), operacion);
    }
}
