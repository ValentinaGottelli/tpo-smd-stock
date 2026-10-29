package com.smd.inventario.presentation.controller;

import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;
import com.smd.inventario.domain.facade.FachadaEscaneo;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.presentation.dto.EscaneoRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operaciones de escaneo del depósito.
 *
 * <p>Es la única capa que conoce HTTP: valida el body y delega en
 * {@link FachadaEscaneo}. Las reglas de transición viven en el dominio.</p>
 */
@RestController
@RequestMapping("/api/escaneos")
public class EscaneoController {

    private final FachadaEscaneo fachadaEscaneo;

    public EscaneoController(FachadaEscaneo fachadaEscaneo) {
        this.fachadaEscaneo = fachadaEscaneo;
    }

    @PostMapping("/ingreso")
    public RespuestaEscaneoDTO ingresar(@Valid @RequestBody EscaneoRequest request) {
        return fachadaEscaneo.registrarIngreso(request.aDominio(TipoMovimiento.INGRESO));
    }

    @PostMapping("/reserva")
    public RespuestaEscaneoDTO reservar(@Valid @RequestBody EscaneoRequest request) {
        return fachadaEscaneo.reservar(request.aDominio(TipoMovimiento.RESERVA));
    }

    @PostMapping("/cancelacion")
    public RespuestaEscaneoDTO cancelar(@Valid @RequestBody EscaneoRequest request) {
        return fachadaEscaneo.cancelarReserva(request.aDominio(TipoMovimiento.CANCELACION));
    }

    @PostMapping("/despacho")
    public RespuestaEscaneoDTO despachar(@Valid @RequestBody EscaneoRequest request) {
        return fachadaEscaneo.despachar(request.aDominio(TipoMovimiento.DESPACHO));
    }
}
