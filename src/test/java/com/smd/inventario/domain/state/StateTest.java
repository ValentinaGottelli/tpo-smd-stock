package com.smd.inventario.domain.state;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica la matriz completa de transiciones del patrón State: 4 estados por
 * 4 operaciones.
 */
@DisplayName("State: matriz de transiciones")
class StateTest {

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @MethodSource("casos")
    void aplicaLaMatrizDeTransiciones(EstadoProducto inicial, Operacion operacion, EstadoProducto esperado) {
        ItemInventario item = itemEn(inicial);

        if (esperado == null) {
            assertThatThrownBy(() -> ejecutar(item, operacion))
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining(item.getCodigo())
                    .hasMessageContaining(inicial.name());
            assertThat(item.getEstado())
                    .as("un intento invalido no debe cambiar el estado")
                    .isEqualTo(inicial);
        } else {
            ejecutar(item, operacion);
            assertThat(item.getEstado()).isEqualTo(esperado);
        }
    }

    private static Stream<Arguments> casos() {
        return Stream.of(
                Arguments.of(EstadoProducto.PRODUCIDO, Operacion.INGRESAR, EstadoProducto.EN_DEPOSITO),
                Arguments.of(EstadoProducto.PRODUCIDO, Operacion.RESERVAR, null),
                Arguments.of(EstadoProducto.PRODUCIDO, Operacion.CANCELAR, null),
                Arguments.of(EstadoProducto.PRODUCIDO, Operacion.DESPACHAR, null),

                Arguments.of(EstadoProducto.EN_DEPOSITO, Operacion.INGRESAR, null),
                Arguments.of(EstadoProducto.EN_DEPOSITO, Operacion.RESERVAR, EstadoProducto.RESERVADO),
                Arguments.of(EstadoProducto.EN_DEPOSITO, Operacion.CANCELAR, null),
                Arguments.of(EstadoProducto.EN_DEPOSITO, Operacion.DESPACHAR, null),

                Arguments.of(EstadoProducto.RESERVADO, Operacion.INGRESAR, null),
                Arguments.of(EstadoProducto.RESERVADO, Operacion.RESERVAR, null),
                Arguments.of(EstadoProducto.RESERVADO, Operacion.CANCELAR, EstadoProducto.EN_DEPOSITO),
                Arguments.of(EstadoProducto.RESERVADO, Operacion.DESPACHAR, EstadoProducto.DESPACHADO),

                Arguments.of(EstadoProducto.DESPACHADO, Operacion.INGRESAR, null),
                Arguments.of(EstadoProducto.DESPACHADO, Operacion.RESERVAR, null),
                Arguments.of(EstadoProducto.DESPACHADO, Operacion.CANCELAR, null),
                Arguments.of(EstadoProducto.DESPACHADO, Operacion.DESPACHAR, null)
        );
    }

    private static ItemInventario itemEn(EstadoProducto estado) {
        return new Colchon("COLCHON-TEST", estado, LocalDate.of(2026, 1, 1));
    }

    private static void ejecutar(ItemInventario item, Operacion operacion) {
        switch (operacion) {
            case INGRESAR -> item.ingresar();
            case RESERVAR -> item.reservar();
            case CANCELAR -> item.cancelarReserva();
            case DESPACHAR -> item.despachar();
        }
    }

    private enum Operacion {
        INGRESAR,
        RESERVAR,
        CANCELAR,
        DESPACHAR
    }
}
