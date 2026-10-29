package com.smd.inventario.persistence.entity;

import com.smd.inventario.domain.model.CanalVenta;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro persistente de una operación de stock, usado por la auditoría.
 */
@Entity
@Table(name = "movimiento")
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_item", nullable = false, length = 64)
    private String codigoItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", length = 20)
    private CanalVenta canal;

    @Column(name = "usuario", nullable = false, length = 64)
    private String usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 20)
    private EstadoProducto estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoProducto estadoNuevo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    /** Constructor requerido por JPA. */
    protected MovimientoEntity() {
    }

    public MovimientoEntity(String codigoItem,
                            TipoMovimiento tipo,
                            CanalVenta canal,
                            String usuario,
                            LocalDateTime fechaHora,
                            EstadoProducto estadoAnterior,
                            EstadoProducto estadoNuevo,
                            int cantidad) {
        this.codigoItem = codigoItem;
        this.tipo = tipo;
        this.canal = canal;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public CanalVenta getCanal() {
        return canal;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoProducto getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoProducto getEstadoNuevo() {
        return estadoNuevo;
    }

    public int getCantidad() {
        return cantidad;
    }
}
