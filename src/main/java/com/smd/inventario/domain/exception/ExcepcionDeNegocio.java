package com.smd.inventario.domain.exception;

/**
 * Excepción base de las reglas de negocio del dominio.
 *
 * <p>La capa de presentación la traduce a un código HTTP 409.</p>
 */
public class ExcepcionDeNegocio extends RuntimeException {

    public ExcepcionDeNegocio(String mensaje) {
        super(mensaje);
    }
}
