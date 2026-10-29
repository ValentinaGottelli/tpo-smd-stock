package com.smd.inventario.domain.service;

import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;

import java.util.List;

/**
 * Consultas de solo lectura sobre el inventario y la auditoría.
 *
 * <p>Separar las consultas del {@link GestorDeStock} mantiene las operaciones
 * de escritura pequeñas y deja las lecturas sin efectos secundarios.</p>
 */
public class ServicioConsultas {

    private final RepositorioItems repositorioItems;
    private final RepositorioMovimientos repositorioMovimientos;

    public ServicioConsultas(RepositorioItems repositorioItems,
                             RepositorioMovimientos repositorioMovimientos) {
        this.repositorioItems = repositorioItems;
        this.repositorioMovimientos = repositorioMovimientos;
    }

    /**
     * @param estado filtro opcional; {@code null} devuelve todos los ítems raíz
     */
    public List<ItemInventario> listar(EstadoProducto estado) {
        return estado == null
                ? repositorioItems.listarTodos()
                : repositorioItems.listarPorEstado(estado);
    }

    public ItemInventario buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
        return repositorioItems.buscarPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(codigo));
    }

    public List<Movimiento> ultimosMovimientos(int limite) {
        return repositorioMovimientos.ultimos(limite <= 0 ? 50 : limite);
    }
}
