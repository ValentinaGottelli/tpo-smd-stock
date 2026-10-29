package com.smd.inventario.demo;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Demo de consola para mostrar en clase los patrones State y Composite sin
 * levantar Spring ni la base de datos.
 *
 * <p>Se ejecuta con:</p>
 *
 * <pre>
 * mvnw.cmd -q compile
 * java -cp target/classes com.smd.inventario.demo.DemoPatrones
 * </pre>
 */
public final class DemoPatrones {

    private DemoPatrones() {
    }

    public static void main(String[] args) {
        titulo("DEMO TPO - State + Composite");

        cicloDeVidaDeUnColchon();
        loteDeCincoColchones();
        transicionesRechazadas();

        System.out.println();
        System.out.println("Fin de la demo.");
    }

    private static void cicloDeVidaDeUnColchon() {
        titulo("1) Ciclo de vida de un colchon");
        ItemInventario colchon = new Colchon(
                "COLCHON-QR-0001", EstadoProducto.PRODUCIDO, LocalDate.of(2026, 9, 1));

        paso("estado inicial: " + colchon.getEstado());
        colchon.ingresar();
        paso("ingresar()  -> " + colchon.getEstado());
        colchon.reservar();
        paso("reservar()  -> " + colchon.getEstado());
        colchon.despachar();
        paso("despachar() -> " + colchon.getEstado());
    }

    private static void loteDeCincoColchones() {
        titulo("2) Lote de 5 colchones (Composite)");
        Lote lote = new Lote("LOTE-QR-0001", EstadoProducto.PRODUCIDO, LocalDate.of(2026, 9, 10));
        for (int i = 1; i <= 5; i++) {
            lote.agregar(new Colchon(
                    "COLCHON-QR-10%02d".formatted(i),
                    EstadoProducto.PRODUCIDO,
                    LocalDate.of(2026, 9, 10)));
        }

        paso("cantidad del lote: " + lote.getCantidad() + " (hijos: " + lote.getItems().size() + ")");
        paso("estados iniciales: " + estadosDe(lote));
        lote.ingresar();
        paso("ingresar()  -> " + estadosDe(lote));
        lote.reservar();
        paso("reservar()  -> " + estadosDe(lote));
        lote.despachar();
        paso("despachar() -> " + estadosDe(lote));
    }

    private static void transicionesRechazadas() {
        titulo("3) Transiciones rechazadas por State");
        ItemInventario colchon = new Colchon(
                "COLCHON-QR-0002", EstadoProducto.EN_DEPOSITO, LocalDate.of(2026, 9, 5));

        probar(colchon, "ingresar()", colchon::ingresar);
        probar(colchon, "despachar()", colchon::despachar);

        Lote loteVacio = new Lote("LOTE-VACIO", EstadoProducto.PRODUCIDO, LocalDate.of(2026, 9, 1));
        probar(loteVacio, "reservar()", loteVacio::reservar);
    }

    private static String estadosDe(Lote lote) {
        return lote.getItems().stream()
                .map(ItemInventario::getEstado)
                .distinct()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private static void probar(ItemInventario item, String operacion, Runnable accion) {
        try {
            accion.run();
            paso(operacion + " -> " + item.getEstado() + " (no deberia pasar)");
        } catch (TransicionInvalidaException e) {
            paso(operacion + " -> rechazado: " + e.getMessage());
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    private static void paso(String texto) {
        System.out.println("   " + texto);
    }
}
