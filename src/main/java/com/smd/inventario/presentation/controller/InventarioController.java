package com.smd.inventario.presentation.controller;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.service.GestorDeStock;
import com.smd.inventario.domain.service.ServicioConsultas;
import com.smd.inventario.presentation.dto.ItemInventarioResponse;
import com.smd.inventario.presentation.dto.RegistrarColchonRequest;
import com.smd.inventario.presentation.dto.RegistrarLoteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta y alta de inventario.
 *
 * <p>Las lecturas usan {@link ServicioConsultas}; las altas usan
 * {@link GestorDeStock}. Ninguno depende de Spring.</p>
 */
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final ServicioConsultas consultas;
    private final GestorDeStock gestor;

    public InventarioController(ServicioConsultas consultas, GestorDeStock gestor) {
        this.consultas = consultas;
        this.gestor = gestor;
    }

    @GetMapping
    public List<ItemInventarioResponse> listar(
            @RequestParam(required = false) EstadoProducto estado) {
        return consultas.listar(estado).stream()
                .map(ItemInventarioResponse::desde)
                .toList();
    }

    @GetMapping("/{codigo}")
    public ItemInventarioResponse buscar(@PathVariable String codigo) {
        return ItemInventarioResponse.desde(consultas.buscar(codigo));
    }

    @PostMapping("/colchones")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemInventarioResponse registrarColchon(
            @Valid @RequestBody RegistrarColchonRequest request) {
        return ItemInventarioResponse.desde(gestor.registrarColchon(
                request.codigo(),
                request.estado(),
                request.fechaProduccion()));
    }

    @PostMapping("/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemInventarioResponse registrarLote(
            @Valid @RequestBody RegistrarLoteRequest request) {
        int cantidad = request.cantidad() == null ? 5 : request.cantidad();
        return ItemInventarioResponse.desde(gestor.registrarLote(
                request.codigo(),
                request.estado(),
                request.fechaProduccion(),
                cantidad));
    }
}
