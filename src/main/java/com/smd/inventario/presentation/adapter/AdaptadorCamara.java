package com.smd.inventario.presentation.adapter;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.TipoMovimiento;

/**
 * Adaptador para escaner por camara / vision artificial (Patron Adapter).
 * Traduce un payload de imagen/camara (ej. formato URI "qr://SMD/COLCHON-QR-0001")
 * al codigo interno del inventario.
 */
public class AdaptadorCamara implements LectorCodigo {

    private final String payloadCamara;
    private final Boolean embalajeDetectadoOk;

    public AdaptadorCamara(String payloadCamara, Boolean embalajeDetectadoOk) {
        this.payloadCamara = payloadCamara;
        this.embalajeDetectadoOk = embalajeDetectadoOk;
    }

    @Override
    public EscaneoDTO leer(TipoMovimiento tipoMovimiento, CanalVenta canal, String usuario) {
        String codigoExtraido = normalizarPayload(payloadCamara);
        return new EscaneoDTO(codigoExtraido, tipoMovimiento, canal, usuario,
                embalajeDetectadoOk != null ? embalajeDetectadoOk : true);
    }

    private String normalizarPayload(String raw) {
        if (raw == null) {
            return "";
        }
        String clean = raw.trim();
        // Simula la extraccion de prefijo de camara ej: "qr://smd/" o "smd:qr:"
        if (clean.toLowerCase().startsWith("qr://smd/")) {
            return clean.substring("qr://smd/".length());
        }
        return clean;
    }
}
