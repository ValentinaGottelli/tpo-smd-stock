package com.smd.inventario.domain.exception;

/**
 * Se lanza cuando una regla de negocio rechaza la operación: por ejemplo, un
 * embalaje dañado o una reserva mayorista por debajo del mínimo.
 */
public class ReglaDeNegocioException extends ExcepcionDeNegocio {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
