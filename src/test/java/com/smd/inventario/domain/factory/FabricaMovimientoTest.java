package com.smd.inventario.domain.factory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;
import com.smd.inventario.domain.service.GestorDeStock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la cadena Decorator + Factory del rol B con repositorios en memoria (sin Spring). */
class FabricaMovimientoTest {

    private final Map<String, ItemInventario> items = new LinkedHashMap<>();
    private final List<Movimiento> movimientos = new ArrayList<>();
    private FabricaMovimiento fabrica;

    private final RepositorioItems repoItems = new RepositorioItems() {
        public Optional<ItemInventario> buscarPorCodigo(String codigo) {
            // copia: como la base real, cada lectura devuelve un objeto nuevo
            return Optional.ofNullable(items.get(codigo))
                    .map(i -> new Colchon(i.getCodigo(), i.getEstado(), i.getFechaProduccion()));
        }
        public List<ItemInventario> listarPorEstado(EstadoProducto estado) {
            return items.values().stream().filter(i -> i.getEstado() == estado).toList();
        }
        public Optional<ItemInventario> buscarMasAntiguoEnDeposito() {
            return listarPorEstado(EstadoProducto.EN_DEPOSITO).stream()
                    .min(Comparator.comparing(ItemInventario::getFechaProduccion));
        }
        public List<ItemInventario> listarTodos() {
            return List.copyOf(items.values());
        }
        public void guardar(ItemInventario item) {
            items.put(item.getCodigo(), item);
        }
    };

    private final RepositorioMovimientos repoMovimientos = new RepositorioMovimientos() {
        public void guardar(Movimiento movimiento) {
            movimientos.add(movimiento);
        }
        public List<Movimiento> ultimos(int limite) {
            return movimientos;
        }
    };

    @BeforeEach
    void preparar() {
        fabrica = new FabricaMovimiento(repoMovimientos, repoItems, 10);
    }

    private void agregar(String codigo, EstadoProducto estado, int dia) {
        repoItems.guardar(new Colchon(codigo, estado, LocalDate.of(2026, 9, dia)));
    }

    private Movimiento movimiento(String codigo, TipoMovimiento tipo) {
        return new Movimiento(codigo, tipo, null, "op", LocalDateTime.now(),
                EstadoProducto.PRODUCIDO, EstadoProducto.EN_DEPOSITO, 1);
    }

    private EscaneoDTO solicitud(String codigo, TipoMovimiento tipo, Boolean embalajeOk) {
        return new EscaneoDTO(codigo, tipo, null, "op", embalajeOk);
    }

    private void registrar(TipoMovimiento tipo, String codigo, Boolean embalajeOk) {
        fabrica.crear(tipo).registrar(solicitud(codigo, tipo, embalajeOk), movimiento(codigo, tipo));
    }

    @Test
    void ingresoConEmbalajeOkRegistra() {
        registrar(TipoMovimiento.INGRESO, "C1", true);
        assertEquals(1, movimientos.size());
    }

    @Test
    void ingresoSinValidarEmbalajeRegistra() {
        registrar(TipoMovimiento.INGRESO, "C1", null);
        assertEquals(1, movimientos.size());
    }

    @Test
    void ingresoConEmbalajeDanadoRechazaYNoRegistra() {
        assertThrows(ReglaDeNegocioException.class, () -> registrar(TipoMovimiento.INGRESO, "C1", false));
        assertEquals(0, movimientos.size());
    }

    @Test
    void reservaNoEvaluaEmbalaje() {
        registrar(TipoMovimiento.RESERVA, "C1", false);
        assertEquals(1, movimientos.size());
    }

    @Test
    void reservaEsLibreAunqueHayaUnoMasAntiguoEnDeposito() {
        agregar("VIEJO", EstadoProducto.EN_DEPOSITO, 1);
        agregar("NUEVO", EstadoProducto.EN_DEPOSITO, 5);
        registrar(TipoMovimiento.RESERVA, "NUEVO", null);
        assertEquals(1, movimientos.size());
    }

    @Test
    void despachoDelReservadoMasAntiguoRegistra() {
        agregar("VIEJO", EstadoProducto.RESERVADO, 1);
        agregar("NUEVO", EstadoProducto.RESERVADO, 5);
        registrar(TipoMovimiento.DESPACHO, "VIEJO", null);
        assertEquals(1, movimientos.size());
    }

    @Test
    void despachoConReservadoMasAntiguoRechaza() {
        agregar("VIEJO", EstadoProducto.RESERVADO, 1);
        agregar("NUEVO", EstadoProducto.RESERVADO, 5);
        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class,
                () -> registrar(TipoMovimiento.DESPACHO, "NUEVO", null));
        assertEquals(true, e.getMessage().contains("VIEJO"));
        assertEquals(0, movimientos.size());
    }

    @Test
    void despachoConEmpateDeFechasAcepta() {
        agregar("A", EstadoProducto.RESERVADO, 3);
        agregar("B", EstadoProducto.RESERVADO, 3);
        registrar(TipoMovimiento.DESPACHO, "B", null);
        assertEquals(1, movimientos.size());
    }

    @Test
    void despachoIgnoraLosMasAntiguosQueSoloEstanEnDeposito() {
        agregar("VIEJO", EstadoProducto.EN_DEPOSITO, 1);
        agregar("NUEVO", EstadoProducto.RESERVADO, 5);
        registrar(TipoMovimiento.DESPACHO, "NUEVO", null);
        assertEquals(1, movimientos.size());
    }

    @Test
    void despachoConStockBajoRegistraSinAbortar() {
        agregar("A", EstadoProducto.RESERVADO, 1); // stock en deposito = 0 < 10
        assertDoesNotThrow(() -> registrar(TipoMovimiento.DESPACHO, "A", null));
        assertEquals(1, movimientos.size());
    }

    @Test
    void gestorNoGuardaElItemSiUnControlRechaza() {
        agregar("C1", EstadoProducto.PRODUCIDO, 1);
        GestorDeStock gestor = new GestorDeStock(repoItems, fabrica);

        assertThrows(ReglaDeNegocioException.class,
                () -> gestor.ingresar(solicitud("C1", TipoMovimiento.INGRESO, false)));
        assertEquals(0, movimientos.size());

        gestor.ingresar(solicitud("C1", TipoMovimiento.INGRESO, true));
        assertEquals(1, movimientos.size());
        assertEquals(EstadoProducto.EN_DEPOSITO, items.get("C1").getEstado());
    }
}
