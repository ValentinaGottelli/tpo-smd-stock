package com.smd.inventario.domain.model;

/**
 * Ciclo de vida de un ítem de inventario (patrón State, apartado 2 del TP).
 *
 * <pre>
 * PRODUCIDO --ingreso--> EN_DEPOSITO --reserva--> RESERVADO --despacho--> DESPACHADO
 *                              ^                       |
 *                              +---- cancelación ------+
 * </pre>
 */
public enum EstadoProducto {
    PRODUCIDO,
    EN_DEPOSITO,
    RESERVADO,
    DESPACHADO
}
