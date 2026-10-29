package com.smd.inventario.persistence.entity;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.TipoItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representación persistente de un ítem de inventario (colchón o lote).
 *
 * <p>Es la única entidad del esqueleto inicial y sirve para verificar que la
 * conexión con la base de datos relacional funciona de punta a punta.
 * El modelo de dominio rico (Colchon, Lote, Movimiento) se implementa en la
 * capa {@code domain} y se mapea contra esta tabla mediante un mapper.</p>
 */
@Entity
@Table(
        name = "item_inventario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_item_inventario_codigo",
                columnNames = "codigo"
        )
)
public class ItemInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código QR único impreso en la unidad o en el lote. */
    @Column(name = "codigo", nullable = false, length = 64)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoItem tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoProducto estado;

    /** 1 para un colchón suelto; cantidad de unidades para un lote. */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /** Fecha de producción; insumo del criterio FIFO (apartado 3 del TP). */
    @Column(name = "fecha_produccion")
    private LocalDate fechaProduccion;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    /** Control de concurrencia optimista. */
    @Version
    @Column(name = "version_registro", nullable = false)
    private Long version;

    /** Constructor requerido por JPA. */
    protected ItemInventarioEntity() {
    }

    public ItemInventarioEntity(String codigo,
                                TipoItem tipo,
                                EstadoProducto estado,
                                Integer cantidad,
                                LocalDate fechaProduccion) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.estado = estado;
        this.cantidad = cantidad;
        this.fechaProduccion = fechaProduccion;
        this.fechaAlta = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoItem getTipo() {
        return tipo;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public LocalDate getFechaProduccion() {
        return fechaProduccion;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public Long getVersion() {
        return version;
    }
}
