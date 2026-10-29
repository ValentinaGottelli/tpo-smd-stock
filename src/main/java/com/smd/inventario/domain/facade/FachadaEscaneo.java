package com.smd.inventario.domain.facade;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;

/**
 * Puerta de entrada del dominio para las operaciones de escaneo.
 *
 * <p>La capa REST solo conoce esta interfaz. La implementación delega en
 * {@link com.smd.inventario.domain.service.GestorDeStock} y completa el usuario
 * por defecto cuando la solicitud no lo trae.</p>
 */
public interface FachadaEscaneo {

    RespuestaEscaneoDTO registrarIngreso(EscaneoDTO solicitud);

    RespuestaEscaneoDTO reservar(EscaneoDTO solicitud);

    RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud);

    RespuestaEscaneoDTO despachar(EscaneoDTO solicitud);
}
