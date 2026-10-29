package com.smd.inventario.domain.model;

import java.time.LocalDate;

/**
 * Ítem que puede escanearse en el depósito (patrón Composite).
 *
 * <p>La implementación concreta es {@code Colchon} (hoja) o {@code Lote}
 * (compuesto). El ciclo de vida se resuelve con el patrón State: cada operación
 * delega en el estado actual del ítem.</p>
 */
public interface ItemInventario {

    String getCodigo();

    /**
     * Cantidad de unidades físicas que representa el ítem. Un colchón siempre
     * devuelve 1; un lote devuelve la suma de sus hijos.
     */
    int getCantidad();

    LocalDate getFechaProduccion();

    EstadoProducto getEstado();

    void ingresar();

    void reservar();

    void cancelarReserva();

    void despachar();

    /**
     * Cambia el estado sin validar la transición. Solo lo usan las clases de
     * estado y el composite al propagar el cambio a sus hijos.
     */
    void cambiarEstado(EstadoProducto nuevoEstado);
}
