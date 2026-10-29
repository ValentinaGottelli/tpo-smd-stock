package com.smd.inventario.domain.model;

/**
 * Canal de venta desde el que llega una reserva.
 *
 * <p>Los canales {@code DREAMO_*} usan la estrategia minorista; {@code RUBIN},
 * la mayorista con mínimo de 5 unidades.</p>
 */
public enum CanalVenta {
    DREAMO_LOCAL,
    DREAMO_MELI,
    DREAMO_FRAVEGA,
    RUBIN
}
