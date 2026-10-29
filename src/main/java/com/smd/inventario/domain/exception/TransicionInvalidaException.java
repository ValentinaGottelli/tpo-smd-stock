package com.smd.inventario.domain.exception;

import com.smd.inventario.domain.model.EstadoProducto;

/**
 * Se lanza cuando el patrón State recibe una operación que no corresponde al
 * estado actual del ítem.
 *
 * <p>El ítem no se modifica cuando esto ocurre.</p>
 */
public class TransicionInvalidaException extends ExcepcionDeNegocio {

    public TransicionInvalidaException(String codigo, EstadoProducto estado, String operacion) {
        super("No se puede " + operacion + " el item " + codigo
                + " porque esta en estado " + estado);
    }
}
