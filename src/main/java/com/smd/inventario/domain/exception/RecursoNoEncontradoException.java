package com.smd.inventario.domain.exception;

/**
 * Se lanza cuando se busca un ítem que no existe en el inventario.
 *
 * <p>La capa de presentación la traduce a un código HTTP 404.</p>
 */
public class RecursoNoEncontradoException extends ExcepcionDeNegocio {

    public RecursoNoEncontradoException(String codigo) {
        super("No se encontro un item con codigo " + codigo);
    }
}
