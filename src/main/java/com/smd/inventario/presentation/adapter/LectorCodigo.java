package com.smd.inventario.presentation.adapter;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.TipoMovimiento;

/**
 * Interfaz comun para lectores de codigo/dispositivos (Patron Adapter).
 */
public interface LectorCodigo {

    /**
     * Lee o procesa la captura del dispositivo y genera el DTO de dominio normalizado.
     *
     * @param tipoMovimiento operacion que se esta realizando en el lector
     * @param canal          canal de venta (si aplica)
     * @param usuario        usuario que opera el lector
     * @return EscaneoDTO normalizado para el sistema
     */
    EscaneoDTO leer(TipoMovimiento tipoMovimiento, CanalVenta canal, String usuario);
}
