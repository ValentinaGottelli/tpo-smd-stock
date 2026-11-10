package com.smd.inventario.presentation.adapter;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.TipoMovimiento;

/**
 * Adaptador para una pistola de escaneo QR conectada por USB (Patron Adapter).
 * La pistola USB emula teclado y envia una cadena cruda terminada en retorno de carro.
 */
public class AdaptadorPistolaUSB implements LectorCodigo {

    private final String bufferTeclado;
    private final Boolean embalajeOk;

    public AdaptadorPistolaUSB(String bufferTeclado, Boolean embalajeOk) {
        this.bufferTeclado = bufferTeclado;
        this.embalajeOk = embalajeOk;
    }

    @Override
    public EscaneoDTO leer(TipoMovimiento tipoMovimiento, CanalVenta canal, String usuario) {
        // La pistola envia el codigo con eventual salto de linea o espacios
        String codigoLimpio = bufferTeclado != null ? bufferTeclado.trim() : "";
        return new EscaneoDTO(codigoLimpio, tipoMovimiento, canal, usuario, embalajeOk != null ? embalajeOk : true);
    }
}
