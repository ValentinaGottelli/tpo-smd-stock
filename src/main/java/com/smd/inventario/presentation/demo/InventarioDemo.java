package com.smd.inventario.presentation.demo;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Almacén en memoria para la demo de State + Composite.
 *
 * <p>No usa la base de datos: guarda los objetos de dominio en un mapa. Es
 * descartable y solo existe para poder mostrar el comportamiento desde
 * Postman. La persistencia real es responsabilidad del rol D.</p>
 */
@Service
public class InventarioDemo {

    private final Map<String, ItemInventario> items = new LinkedHashMap<>();

    public synchronized ItemInventario crearColchon(String codigo, EstadoProducto estado) {
        validarCodigo(codigo);
        if (items.containsKey(codigo)) {
            throw new IllegalArgumentException("Ya existe un item con el codigo " + codigo);
        }
        Colchon colchon = new Colchon(codigo, estado, LocalDate.now());
        items.put(codigo, colchon);
        return colchon;
    }

    public synchronized Lote crearLote(String codigo, EstadoProducto estado, int cantidad) {
        validarCodigo(codigo);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de colchones debe ser mayor a cero");
        }
        if (items.containsKey(codigo)) {
            throw new IllegalArgumentException("Ya existe un item con el codigo " + codigo);
        }

        Lote lote = new Lote(codigo, estado, LocalDate.now());
        for (int i = 1; i <= cantidad; i++) {
            lote.agregar(new Colchon(codigo + "-%02d".formatted(i), estado, LocalDate.now()));
        }
        items.put(codigo, lote);
        return lote;
    }

    public synchronized ItemInventario buscar(String codigo) {
        ItemInventario item = items.get(codigo);
        if (item == null) {
            throw new RecursoNoEncontradoException(codigo);
        }
        return item;
    }

    public synchronized List<ItemInventario> listar() {
        return List.copyOf(items.values());
    }

    public synchronized void reiniciar() {
        items.clear();
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
    }
}
