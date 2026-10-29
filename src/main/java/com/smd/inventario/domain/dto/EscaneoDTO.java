package com.smd.inventario.domain.dto;

import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.TipoMovimiento;

/**
 * Datos de una operación de escaneo, ya validados por la capa REST.
 *
 * <p>Vive en el dominio para que ni {@code GestorDeStock} ni la fachada
 * dependan de las clases de Spring MVC.</p>
 *
 * @param codigo     código QR escaneado
 * @param tipo       operación solicitada
 * @param canal      canal de venta; {@code null} en ingreso y ajuste
 * @param usuario    operario que realiza el escaneo
 * @param embalajeOk resultado de la validación de embalaje; {@code null} = no validado
 */
public record EscaneoDTO(
        String codigo,
        TipoMovimiento tipo,
        CanalVenta canal,
        String usuario,
        Boolean embalajeOk
) { }
