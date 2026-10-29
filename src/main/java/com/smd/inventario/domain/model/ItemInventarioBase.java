package com.smd.inventario.domain.model;

import com.smd.inventario.domain.state.EstadoItem;
import com.smd.inventario.domain.state.Estados;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Base común de los ítems del inventario.
 *
 * <p>Implementa la parte del patrón <b>State</b> que no depende del estado
 * concreto: guarda el estado actual y delega en él cada operación. Las
 * subclases ({@link com.smd.inventario.domain.composite.Colchon} y
 * {@link com.smd.inventario.domain.composite.Lote}) solo aportan su
 * comportamiento propio del patrón <b>Composite</b>.</p>
 */
public abstract class ItemInventarioBase implements ItemInventario {

    private final String codigo;
    private final LocalDate fechaProduccion;

    private EstadoProducto estado;
    private EstadoItem estadoItem;

    protected ItemInventarioBase(String codigo, EstadoProducto estadoInicial, LocalDate fechaProduccion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo del item no puede estar vacio");
        }
        this.codigo = codigo;
        this.estado = Objects.requireNonNull(estadoInicial, "El estado inicial es obligatorio");
        this.fechaProduccion = Objects.requireNonNull(fechaProduccion, "La fecha de produccion es obligatoria");
        this.estadoItem = Estados.desde(estadoInicial);
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public LocalDate getFechaProduccion() {
        return fechaProduccion;
    }

    @Override
    public EstadoProducto getEstado() {
        return estado;
    }

    @Override
    public void ingresar() {
        estadoItem.ingresar(this);
    }

    @Override
    public void reservar() {
        estadoItem.reservar(this);
    }

    @Override
    public void cancelarReserva() {
        estadoItem.cancelarReserva(this);
    }

    @Override
    public void despachar() {
        estadoItem.despachar(this);
    }

    @Override
    public void cambiarEstado(EstadoProducto nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio");
        this.estadoItem = Estados.desde(nuevoEstado);
    }

    @Override
    public String toString() {
        return codigo + " [" + estado + "]";
    }
}
