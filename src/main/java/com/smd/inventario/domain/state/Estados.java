package com.smd.inventario.domain.state;

import com.smd.inventario.domain.model.EstadoProducto;

/**
 * Traduce el enum {@link EstadoProducto} a su objeto de estado.
 *
 * <p>Los estados no guardan datos, así que se reutilizan instancias únicas.</p>
 */
public final class Estados {

    private static final EstadoItem PRODUCIDO = new EstadoProducido();
    private static final EstadoItem EN_DEPOSITO = new EstadoEnDeposito();
    private static final EstadoItem RESERVADO = new EstadoReservado();
    private static final EstadoItem DESPACHADO = new EstadoDespachado();

    private Estados() {
    }

    public static EstadoItem desde(EstadoProducto estado) {
        return switch (estado) {
            case PRODUCIDO -> PRODUCIDO;
            case EN_DEPOSITO -> EN_DEPOSITO;
            case RESERVADO -> RESERVADO;
            case DESPACHADO -> DESPACHADO;
        };
    }
}
