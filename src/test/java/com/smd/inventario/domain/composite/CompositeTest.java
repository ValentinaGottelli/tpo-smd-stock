package com.smd.inventario.domain.composite;

import com.smd.inventario.domain.model.EstadoProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica el patrón Composite: cantidad recursiva y propagación de estado en
 * cascada.
 */
@DisplayName("Composite: colchones y lotes")
class CompositeTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 1);

    @Test
    void unColchonSiempreAportaUnaUnidad() {
        Colchon colchon = colchon("COLCHON-1");

        assertThat(colchon.getCantidad()).isEqualTo(1);
    }

    @Test
    void unLoteSumaSusHijosYSubLotes() {
        Lote lote = new Lote("LOTE-1", EstadoProducto.PRODUCIDO, FECHA);
        lote.agregar(colchon("COLCHON-1"));
        lote.agregar(colchon("COLCHON-2"));
        lote.agregar(colchon("COLCHON-3"));

        Lote subLote = new Lote("LOTE-2", EstadoProducto.PRODUCIDO, FECHA);
        subLote.agregar(colchon("COLCHON-4"));
        subLote.agregar(colchon("COLCHON-5"));
        lote.agregar(subLote);

        assertThat(lote.getCantidad()).isEqualTo(5);
    }

    @Test
    void despacharElLoteCambiaElEstadoDeTodosLosHijos() {
        Lote lote = loteDeCinco();

        lote.ingresar();
        lote.reservar();
        lote.despachar();

        assertThat(lote.getEstado()).isEqualTo(EstadoProducto.DESPACHADO);
        assertThat(lote.getItems())
                .allMatch(item -> item.getEstado() == EstadoProducto.DESPACHADO);
    }

    @Test
    void unLoteNoPuedeContenerseASiMismo() {
        Lote lote = new Lote("LOTE-1", EstadoProducto.PRODUCIDO, FECHA);

        assertThatThrownBy(() -> lote.agregar(lote))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contenerse");
    }

    @Test
    void noSePuedeAgregarUnItemYaDespachado() {
        Colchon despachado = colchon("COLCHON-9");
        despachado.ingresar();
        despachado.reservar();
        despachado.despachar();

        Lote lote = new Lote("LOTE-1", EstadoProducto.PRODUCIDO, FECHA);

        assertThatThrownBy(() -> lote.agregar(despachado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("despachado");
    }

    private static Lote loteDeCinco() {
        Lote lote = new Lote("LOTE-1", EstadoProducto.PRODUCIDO, FECHA);
        for (int i = 1; i <= 5; i++) {
            lote.agregar(colchon("COLCHON-" + i));
        }
        return lote;
    }

    private static Colchon colchon(String codigo) {
        return new Colchon(codigo, EstadoProducto.PRODUCIDO, FECHA);
    }
}
