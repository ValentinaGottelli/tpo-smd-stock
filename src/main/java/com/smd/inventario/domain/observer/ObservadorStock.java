package com.smd.inventario.domain.observer;

/**
 * Interfaz para los observadores del inventario (Patron Observer).
 */
public interface ObservadorStock {

    /**
     * Notificacion invocada cuando se produce un movimiento en el stock.
     *
     * @param evento datos del evento de stock
     */
    void alOcurrirMovimiento(EventoStock evento);
}
