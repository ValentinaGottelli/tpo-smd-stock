package com.smd.inventario.domain.service;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Punto único del dominio para ejecutar operaciones de stock.
 *
 * <p>Es Java puro: no conoce Spring ni HTTP. Se registra como un bean único en
 * {@code config.DomainConfig}, por lo que Spring garantiza una sola instancia
 * durante toda la vida de la aplicación (Singleton).</p>
 *
 * <p>Cada operación delega la transición en el patrón State del ítem, persiste
 * el nuevo estado y registra el movimiento de auditoría.</p>
 */
public class GestorDeStock {

    private final RepositorioItems repositorioItems;
    private final RepositorioMovimientos repositorioMovimientos;

    public GestorDeStock(RepositorioItems repositorioItems,
                         RepositorioMovimientos repositorioMovimientos) {
        this.repositorioItems = repositorioItems;
        this.repositorioMovimientos = repositorioMovimientos;
    }

    public RespuestaEscaneoDTO ingresar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.INGRESO, "Ingreso registrado", ItemInventario::ingresar);
    }

    public RespuestaEscaneoDTO reservar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.RESERVA, "Reserva registrada", ItemInventario::reservar);
    }

    public RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.CANCELACION, "Cancelacion registrada",
                ItemInventario::cancelarReserva);
    }

    public RespuestaEscaneoDTO despachar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.DESPACHO, "Despacho registrado", ItemInventario::despachar);
    }

    /**
     * Da de alta un colchón suelto en el inventario.
     *
     * @param estado inicial; si es {@code null} arranca en {@code PRODUCIDO}
     */
    public Colchon registrarColchon(String codigo, EstadoProducto estado, LocalDate fechaProduccion) {
        validarCodigoNuevo(codigo);
        EstadoProducto inicial = estadoInicial(estado);
        Colchon colchon = new Colchon(codigo, inicial, fecha(fechaProduccion));
        repositorioItems.guardar(colchon);
        return colchon;
    }

    /**
     * Da de alta un lote y genera sus colchones hijos.
     *
     * @param cantidad número de colchones del lote; debe ser mayor a cero
     */
    public Lote registrarLote(String codigo, EstadoProducto estado, LocalDate fechaProduccion, int cantidad) {
        validarCodigoNuevo(codigo);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de colchones debe ser mayor a cero");
        }

        EstadoProducto inicial = estadoInicial(estado);
        LocalDate fecha = fecha(fechaProduccion);
        Lote lote = new Lote(codigo, inicial, fecha);

        for (int i = 1; i <= cantidad; i++) {
            String codigoHijo = "%s-%02d".formatted(codigo, i);
            validarCodigoNuevo(codigoHijo);
            lote.agregar(new Colchon(codigoHijo, inicial, fecha));
        }

        repositorioItems.guardar(lote);
        return lote;
    }

    private RespuestaEscaneoDTO ejecutar(EscaneoDTO solicitud,
                                         TipoMovimiento tipo,
                                         String mensaje,
                                         Consumer<ItemInventario> accion) {
        validarSolicitud(solicitud);

        ItemInventario item = repositorioItems.buscarPorCodigo(solicitud.codigo())
                .orElseThrow(() -> new RecursoNoEncontradoException(solicitud.codigo()));

        EstadoProducto estadoAnterior = item.getEstado();
        accion.accept(item);
        EstadoProducto estadoNuevo = item.getEstado();

        repositorioItems.guardar(item);

        LocalDateTime fechaHora = LocalDateTime.now();
        Movimiento movimiento = new Movimiento(
                item.getCodigo(),
                tipo,
                solicitud.canal(),
                solicitud.usuario(),
                fechaHora,
                estadoAnterior,
                estadoNuevo,
                item.getCantidad());
        repositorioMovimientos.guardar(movimiento);

        return new RespuestaEscaneoDTO(
                item.getCodigo(),
                estadoAnterior,
                estadoNuevo,
                item.getCantidad(),
                mensaje + " para " + item.getCodigo(),
                fechaHora);
    }

    private void validarSolicitud(EscaneoDTO solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud de escaneo es obligatoria");
        }
        if (solicitud.codigo() == null || solicitud.codigo().isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
    }

    private void validarCodigoNuevo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
        if (repositorioItems.buscarPorCodigo(codigo).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe un item con codigo " + codigo);
        }
    }

    private EstadoProducto estadoInicial(EstadoProducto estado) {
        if (estado == null) {
            return EstadoProducto.PRODUCIDO;
        }
        if (estado != EstadoProducto.PRODUCIDO && estado != EstadoProducto.EN_DEPOSITO) {
            throw new ReglaDeNegocioException(
                    "Un item nuevo solo puede estar en PRODUCIDO o EN_DEPOSITO");
        }
        return estado;
    }

    private LocalDate fecha(LocalDate fechaProduccion) {
        return fechaProduccion == null ? LocalDate.now() : fechaProduccion;
    }
}
