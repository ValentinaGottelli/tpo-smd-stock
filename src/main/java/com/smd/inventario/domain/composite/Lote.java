package com.smd.inventario.domain.composite;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.ItemInventarioBase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compuesto del patrón Composite: agrupa colchones (u otros lotes) y se
 * comporta como un ítem más.
 *
 * <p>La cantidad es la suma de sus hijos y cualquier cambio de estado se
 * propaga en cascada. El lote debe armarse antes de cambiar de estado: si se
 * agregan hijos después, quedan con el estado con el que fueron creados.</p>
 */
public final class Lote extends ItemInventarioBase {

    private final List<ItemInventario> items = new ArrayList<>();

    public Lote(String codigo, EstadoProducto estado, LocalDate fechaProduccion) {
        super(codigo, estado, fechaProduccion);
    }

    /**
     * Agrega un ítem al lote.
     *
     * @throws IllegalArgumentException si el ítem es nulo, es el mismo lote o ya
     *                                  fue despachado
     */
    public void agregar(ItemInventario item) {
        Objects.requireNonNull(item, "El item no puede ser nulo");
        if (item == this) {
            throw new IllegalArgumentException("Un lote no puede contenerse a si mismo");
        }
        if (item.getEstado() == EstadoProducto.DESPACHADO) {
            throw new IllegalArgumentException(
                    "No se puede agregar al lote " + getCodigo() + " un item ya despachado");
        }
        items.add(item);
    }

    /**
     * Reconstruye un lote que ya estaba persistido.
     *
     * <p>No aplica las validaciones de {@link #agregar(ItemInventario)} porque
     * un lote despachado tiene hijos despachados y debe poder leerse desde la
     * base sin errores.</p>
     */
    public static Lote reconstruir(String codigo,
                                   EstadoProducto estado,
                                   LocalDate fechaProduccion,
                                   List<ItemInventario> hijos) {
        Lote lote = new Lote(codigo, estado, fechaProduccion);
        lote.items.addAll(Objects.requireNonNull(hijos, "Los hijos son obligatorios"));
        return lote;
    }

    /**
     * @return copia inmutable de los hijos, para no exponer la lista interna
     */
    public List<ItemInventario> getItems() {
        return List.copyOf(items);
    }

    @Override
    public int getCantidad() {
        return items.stream().mapToInt(ItemInventario::getCantidad).sum();
    }

    /**
     * Cambia el estado del lote y lo propaga a todos sus hijos, incluso a los
     * sub-lotes.
     */
    @Override
    public void cambiarEstado(EstadoProducto nuevoEstado) {
        super.cambiarEstado(nuevoEstado);
        items.forEach(item -> item.cambiarEstado(nuevoEstado));
    }
}
