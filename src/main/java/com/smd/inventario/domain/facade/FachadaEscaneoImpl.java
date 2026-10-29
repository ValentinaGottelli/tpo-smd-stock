package com.smd.inventario.domain.facade;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;
import com.smd.inventario.domain.service.GestorDeStock;

/**
 * Implementación de {@link FachadaEscaneo}.
 *
 * <p>No contiene reglas de negocio: normaliza el usuario y delega cada
 * operación en el gestor. El usuario por defecto se inyecta desde la
 * configuración de la aplicación.</p>
 */
public final class FachadaEscaneoImpl implements FachadaEscaneo {

    private final GestorDeStock gestor;
    private final String usuarioDefault;

    public FachadaEscaneoImpl(GestorDeStock gestor, String usuarioDefault) {
        this.gestor = gestor;
        this.usuarioDefault = usuarioDefault;
    }

    @Override
    public RespuestaEscaneoDTO registrarIngreso(EscaneoDTO solicitud) {
        return gestor.ingresar(normalizar(solicitud));
    }

    @Override
    public RespuestaEscaneoDTO reservar(EscaneoDTO solicitud) {
        return gestor.reservar(normalizar(solicitud));
    }

    @Override
    public RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud) {
        return gestor.cancelarReserva(normalizar(solicitud));
    }

    @Override
    public RespuestaEscaneoDTO despachar(EscaneoDTO solicitud) {
        return gestor.despachar(normalizar(solicitud));
    }

    private EscaneoDTO normalizar(EscaneoDTO solicitud) {
        String usuario = solicitud.usuario() == null || solicitud.usuario().isBlank()
                ? usuarioDefault
                : solicitud.usuario();

        return new EscaneoDTO(
                solicitud.codigo(),
                solicitud.tipo(),
                solicitud.canal(),
                usuario,
                solicitud.embalajeOk());
    }
}
