package com.smd.inventario.domain.service;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.factory.FabricaMovimiento;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.domain.observer.EventoStock;
import com.smd.inventario.domain.observer.ObservadorStock;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.strategy.SelectorEstrategia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Punto unico del dominio para ejecutar operaciones de stock.
 *
 * <p>Implementa el patron <b>Singleton clasico (GoF)</b> con constructor privado
 * y metodo estatico {@link #getInstance()}, garantizando una unica instancia
 * en memoria independientemente del framework.</p>
 *
 * <p>Orquesta los patrones del TP:
 * <ul>
 *   <li><b>State:</b> delega las transiciones de ciclo de vida en el item.</li>
 *   <li><b>Composite:</b> opera transparentemente con colchones individuales o lotes.</li>
 *   <li><b>Strategy:</b> valida reglas de reserva segun el canal de venta.</li>
 *   <li><b>Decorator & Factory:</b> ejecuta validaciones encadenadas y alertas.</li>
 *   <li><b>Observer:</b> notifica a los suscriptores tras cada movimiento exitoso.</li>
 * </ul>
 * </p>
 */
public class GestorDeStock {

    private static final Logger log = LoggerFactory.getLogger(GestorDeStock.class);

    // Singleton clasico GoF
    private static volatile GestorDeStock instancia;

    private final RepositorioItems repositorioItems;
    private final FabricaMovimiento fabricaMovimiento;
    private final SelectorEstrategia selectorEstrategia;
    private final List<ObservadorStock> observadores;

    /**
     * Constructor privado para implementar el patron Singleton estricto.
     */
    private GestorDeStock(RepositorioItems repositorioItems,
                          FabricaMovimiento fabricaMovimiento,
                          SelectorEstrategia selectorEstrategia,
                          List<ObservadorStock> observadores) {
        this.repositorioItems = Objects.requireNonNull(repositorioItems, "repositorioItems no puede ser nulo");
        this.fabricaMovimiento = Objects.requireNonNull(fabricaMovimiento, "fabricaMovimiento no puede ser nula");
        this.selectorEstrategia = selectorEstrategia != null ? selectorEstrategia : new SelectorEstrategia();
        this.observadores = observadores != null ? new ArrayList<>(observadores) : new ArrayList<>();
    }

    /**
     * Sobrecarga para inicializaciones simples.
     */
    public static GestorDeStock inicializar(RepositorioItems repositorioItems,
                                            FabricaMovimiento fabricaMovimiento) {
        return inicializar(repositorioItems, fabricaMovimiento, new SelectorEstrategia(), Collections.emptyList());
    }

    /**
     * Inicializa o actualiza la instancia Singleton de GestorDeStock.
     * Metodo sincronizado con doble comprobacion (double-checked locking) para ser seguro en hilos.
     */
    public static GestorDeStock inicializar(RepositorioItems repositorioItems,
                                            FabricaMovimiento fabricaMovimiento,
                                            SelectorEstrategia selectorEstrategia,
                                            List<ObservadorStock> observadores) {
        synchronized (GestorDeStock.class) {
            instancia = new GestorDeStock(repositorioItems, fabricaMovimiento, selectorEstrategia, observadores);
            return instancia;
        }
    }

    /**
     * Acceso clasico GoF al Singleton.
     *
     * @return la unica instancia de GestorDeStock
     * @throws IllegalStateException si no fue inicializada previamente
     */
    public static GestorDeStock getInstance() {
        GestorDeStock ref = instancia;
        if (ref == null) {
            synchronized (GestorDeStock.class) {
                ref = instancia;
                if (ref == null) {
                    throw new IllegalStateException("GestorDeStock no ha sido inicializado aun.");
                }
            }
        }
        return ref;
    }

    public void agregarObservador(ObservadorStock observador) {
        if (observador != null) {
            this.observadores.add(observador);
        }
    }

    public List<ObservadorStock> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }

    public RespuestaEscaneoDTO ingresar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.INGRESO, "Ingreso registrado", null, ItemInventario::ingresar);
    }

    public RespuestaEscaneoDTO reservar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.RESERVA, "Reserva registrada", item -> {
            // Patron Strategy: validar canal de venta antes de cambiar de estado
            selectorEstrategia.para(solicitud.canal()).validar(item);
        }, ItemInventario::reservar);
    }

    public RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.CANCELACION, "Cancelacion registrada", null,
                ItemInventario::cancelarReserva);
    }

    public RespuestaEscaneoDTO despachar(EscaneoDTO solicitud) {
        return ejecutar(solicitud, TipoMovimiento.DESPACHO, "Despacho registrado", null, ItemInventario::despachar);
    }

    /**
     * Da de alta un colchon suelto en el inventario.
     */
    public Colchon registrarColchon(String codigo, EstadoProducto estado, LocalDate fechaProduccion) {
        validarCodigoNuevo(codigo);
        EstadoProducto inicial = estadoInicial(estado);
        Colchon colchon = new Colchon(codigo, inicial, fecha(fechaProduccion));
        repositorioItems.guardar(colchon);
        return colchon;
    }

    /**
     * Da de alta un lote y genera sus colchones hijos (Composite).
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
                                         Consumer<ItemInventario> preValidacion,
                                         Consumer<ItemInventario> accion) {
        validarSolicitud(solicitud);

        ItemInventario item = repositorioItems.buscarPorCodigo(solicitud.codigo())
                .orElseThrow(() -> new RecursoNoEncontradoException(solicitud.codigo()));

        // Pre-validacion (ej. Strategy para reservas)
        if (preValidacion != null) {
            preValidacion.accept(item);
        }

        EstadoProducto estadoAnterior = item.getEstado();
        accion.accept(item);
        EstadoProducto estadoNuevo = item.getEstado();

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

        // Decorator: ejecuta validaciones y persistencia de movimiento
        fabricaMovimiento.crear(tipo).registrar(solicitud, movimiento);
        repositorioItems.guardar(item);

        // Observer: notificar a los suscriptores
        notificarObservadores(movimiento);

        return new RespuestaEscaneoDTO(
                item.getCodigo(),
                estadoAnterior,
                estadoNuevo,
                item.getCantidad(),
                mensaje + " para " + item.getCodigo(),
                fechaHora);
    }

    private void notificarObservadores(Movimiento movimiento) {
        int stockDisponible = repositorioItems.stockDisponible();
        EventoStock evento = new EventoStock(movimiento, stockDisponible);
        for (ObservadorStock observador : observadores) {
            try {
                observador.alOcurrirMovimiento(evento);
            } catch (Exception e) {
                log.error("Error notificando al observador {}: {}", observador.getClass().getSimpleName(), e.getMessage());
            }
        }
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
