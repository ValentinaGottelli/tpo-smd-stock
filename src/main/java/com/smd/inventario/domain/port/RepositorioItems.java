package com.smd.inventario.domain.port;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio hacia la persistencia de ítems.
 *
 * <p>El dominio solo conoce esta interfaz. La implementación con JPA vive en
 * {@code persistence.repository} y se inyecta al arrancar la aplicación.</p>
 */
public interface RepositorioItems {

    Optional<ItemInventario> buscarPorCodigo(String codigo);

    List<ItemInventario> listarPorEstado(EstadoProducto estado);

    /**
     * @return el ítem en depósito con la fecha de producción más antigua
     */
    Optional<ItemInventario> buscarMasAntiguoEnDeposito();

    List<ItemInventario> listarTodos();

    /**
     * Inserta o actualiza el ítem. Si es un lote, guarda también sus hijos.
     */
    void guardar(ItemInventario item);

    default int stockDisponible() {
        return listarPorEstado(EstadoProducto.EN_DEPOSITO).stream()
                .mapToInt(ItemInventario::getCantidad)
                .sum();
    }
}
