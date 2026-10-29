package com.smd.inventario.presentation.dto;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de los endpoints {@code POST /api/escaneos/*}.
 *
 * @param codigo     código QR escaneado
 * @param canal      canal de venta; solo aplica a reservas
 * @param usuario    operario; si se omite se usa el configurado por defecto
 * @param embalajeOk validación de embalaje; {@code null} = no validado
 */
public record EscaneoRequest(
        @NotBlank(message = "El codigo es obligatorio") String codigo,
        CanalVenta canal,
        String usuario,
        Boolean embalajeOk
) {

    public EscaneoDTO aDominio(TipoMovimiento tipo) {
        return new EscaneoDTO(codigo, tipo, canal, usuario, embalajeOk);
    }
}
