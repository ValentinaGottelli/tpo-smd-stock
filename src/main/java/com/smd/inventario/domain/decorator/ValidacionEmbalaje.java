package com.smd.inventario.domain.decorator;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.Movimiento;

/**
 * Rechaza el ingreso si el embalaje llegó dañado.
 *
 * <p>Solo se rechaza con {@code embalajeOk == false}; {@code null} significa
 * "no validado" y se acepta.</p>
 */
public final class ValidacionEmbalaje extends DecoradorRegistro {

    public ValidacionEmbalaje(IRegistroMovimiento siguiente) {
        super(siguiente);
    }

    @Override
    public void registrar(EscaneoDTO solicitud, Movimiento movimiento) {
        if (Boolean.FALSE.equals(solicitud.embalajeOk())) {
            throw new ReglaDeNegocioException("Embalaje dañado: no se puede ingresar el ítem");
        }
        siguiente.registrar(solicitud, movimiento);
    }
}
