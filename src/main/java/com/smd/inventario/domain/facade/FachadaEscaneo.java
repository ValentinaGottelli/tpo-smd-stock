package com.smd.inventario.domain.facade;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;

/**
 * Puerta de entrada del dominio para las operaciones de escaneo.
 */
public interface FachadaEscaneo {

    RespuestaEscaneoDTO registrarIngreso(EscaneoDTO solicitud);

    RespuestaEscaneoDTO reservar(EscaneoDTO solicitud);

    RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud);

    RespuestaEscaneoDTO despachar(EscaneoDTO solicitud);
}
