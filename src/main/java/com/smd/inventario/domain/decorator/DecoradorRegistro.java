package com.smd.inventario.domain.decorator;

import java.util.Objects;

/**
 * Base de los decoradores: guarda el registro que envuelve.
 *
 * <p>Cada subclase agrega un control antes o después de delegar en
 * {@code siguiente}.</p>
 */
public abstract class DecoradorRegistro implements IRegistroMovimiento {

    protected final IRegistroMovimiento siguiente;

    protected DecoradorRegistro(IRegistroMovimiento siguiente) {
        this.siguiente = Objects.requireNonNull(siguiente, "El registro a decorar es obligatorio");
    }
}
