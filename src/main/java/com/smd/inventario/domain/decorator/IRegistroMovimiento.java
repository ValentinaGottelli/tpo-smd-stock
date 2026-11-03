package com.smd.inventario.domain.decorator;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.Movimiento;

/**
 * Componente del patrón Decorator: registra un movimiento de stock.
 *
 * <p>Recibe también la solicitud original porque algunos controles (embalaje)
 * necesitan datos que no forman parte del {@link Movimiento}.</p>
 *
 * <p>Si un control rechaza el movimiento lanza
 * {@link com.smd.inventario.domain.exception.ReglaDeNegocioException} y no se
 * registra nada.</p>
 */
public interface IRegistroMovimiento {

    void registrar(EscaneoDTO solicitud, Movimiento movimiento);
}
