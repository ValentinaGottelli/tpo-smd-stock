package com.smd.inventario.presentation.controller;

import com.smd.inventario.domain.service.ServicioConsultas;
import com.smd.inventario.presentation.dto.MovimientoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de los últimos movimientos registrados.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final ServicioConsultas consultas;

    public AuditoriaController(ServicioConsultas consultas) {
        this.consultas = consultas;
    }

    @GetMapping
    public List<MovimientoResponse> ultimos(
            @RequestParam(defaultValue = "50") int limite) {
        return consultas.ultimosMovimientos(limite).stream()
                .map(MovimientoResponse::desde)
                .toList();
    }
}
