package com.smd.inventario.domain.observer;

import com.smd.inventario.domain.model.Movimiento;

/**
 * Evento disparado tras un movimiento exitoso en el inventario.
 *
 * @param movimiento       movimiento registrado
 * @param stockDisponible  cantidad de unidades disponibles (EN_DEPOSITO) al momento del evento
 */
public record EventoStock(Movimiento movimiento, int stockDisponible) {
}
