package com.smd.inventario.config;

import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.dto.RespuestaEscaneoDTO;
import com.smd.inventario.domain.facade.FachadaEscaneo;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.TipoMovimiento;
import com.smd.inventario.domain.service.GestorDeStock;
import com.smd.inventario.domain.service.ServicioConsultas;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Menú por consola para demostrar la app. TEMPORAL: solo para el video.
 *
 * <p>Se activa con {@code app.consola=true} (por ejemplo
 * {@code $env:APP_CONSOLA="true"}); sin eso no hace nada. Usa los mismos
 * servicios que la API REST. Para quitarlo, borrar este archivo.</p>
 */
@Component
@ConditionalOnProperty(name = "app.consola", havingValue = "true")
public class ConsolaTemporal implements ApplicationRunner {

    private static final String MENU = """

            ===== SMD STOCK =====
             1) Ver inventario
             2) Ver un item
             3) Alta de colchon
             4) Alta de lote
             5) Ingreso    (PRODUCIDO -> EN_DEPOSITO)
             6) Reserva    (EN_DEPOSITO -> RESERVADO)
             7) Cancelar reserva
             8) Despacho   (RESERVADO -> DESPACHADO)
             9) Auditoria (ultimos 10)
             0) Salir
            """;

    private final FachadaEscaneo fachada;
    private final GestorDeStock gestor;
    private final ServicioConsultas consultas;
    private final ConfigurableApplicationContext contexto;

    public ConsolaTemporal(FachadaEscaneo fachada, GestorDeStock gestor,
                           ServicioConsultas consultas, ConfigurableApplicationContext contexto) {
        this.fachada = fachada;
        this.gestor = gestor;
        this.consultas = consultas;
        this.contexto = contexto;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread hilo = new Thread(this::menu, "consola");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void menu() {
        Scanner in = new Scanner(System.in);
        try {
            while (true) {
                System.out.println(MENU);
                String opcion = leer(in, "Opcion").trim();
                if (opcion.equals("0")) {
                    System.exit(SpringApplication.exit(contexto));
                }
                try {
                    ejecutar(opcion, in);
                } catch (NoSuchElementException e) {
                    throw e;
                } catch (RuntimeException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            // fin de la entrada estandar: la consola se cierra, la API sigue
        }
    }

    private void ejecutar(String opcion, Scanner in) {
        switch (opcion) {
            case "1" -> consultas.listar(null).forEach(i -> imprimir(i, ""));
            case "2" -> imprimir(consultas.buscar(leer(in, "Codigo QR")), "");
            case "3" -> {
                String codigo = leer(in, "Codigo nuevo");
                gestor.registrarColchon(codigo, null, fecha(in));
                System.out.println("OK: colchon " + codigo + " creado en PRODUCIDO");
            }
            case "4" -> {
                String codigo = leer(in, "Codigo nuevo");
                LocalDate fecha = fecha(in);
                String cantidad = leer(in, "Cantidad de colchones (Enter = 5)").trim();
                gestor.registrarLote(codigo, null, fecha, cantidad.isEmpty() ? 5 : Integer.parseInt(cantidad));
                System.out.println("OK: lote " + codigo + " creado en PRODUCIDO");
            }
            case "5" -> escanear(in, TipoMovimiento.INGRESO);
            case "6" -> escanear(in, TipoMovimiento.RESERVA);
            case "7" -> escanear(in, TipoMovimiento.CANCELACION);
            case "8" -> escanear(in, TipoMovimiento.DESPACHO);
            case "9" -> consultas.ultimosMovimientos(10).forEach(m -> System.out.printf(
                    "%s  %-11s %-18s %s -> %s  (%s)%n", m.fechaHora().withNano(0), m.tipo(),
                    m.codigoItem(), m.estadoAnterior(), m.estadoNuevo(), m.usuario()));
            default -> System.out.println("Opcion invalida");
        }
    }

    private void escanear(Scanner in, TipoMovimiento tipo) {
        String codigo = leer(in, "Codigo QR");
        Boolean embalajeOk = tipo == TipoMovimiento.INGRESO
                ? !leer(in, "Embalaje OK? (s/n, Enter = s)").trim().equalsIgnoreCase("n")
                : null;
        EscaneoDTO dto = new EscaneoDTO(codigo, tipo, null, null, embalajeOk);
        RespuestaEscaneoDTO r = switch (tipo) {
            case INGRESO -> fachada.registrarIngreso(dto);
            case RESERVA -> fachada.reservar(dto);
            case CANCELACION -> fachada.cancelarReserva(dto);
            default -> fachada.despachar(dto);
        };
        System.out.println("OK: " + r.mensaje() + "  (" + r.estadoAnterior() + " -> "
                + r.estadoNuevo() + ", " + r.cantidad() + " unidad/es)");
    }

    private void imprimir(ItemInventario item, String sangria) {
        System.out.println(sangria + item.getCodigo() + " [" + item.getEstado() + "] x"
                + item.getCantidad() + "  producido " + item.getFechaProduccion());
        if (item instanceof Lote lote) {
            lote.getItems().forEach(hijo -> imprimir(hijo, sangria + "    "));
        }
    }

    private LocalDate fecha(Scanner in) {
        String texto = leer(in, "Fecha de produccion AAAA-MM-DD (Enter = hoy)").trim();
        return texto.isEmpty() ? null : LocalDate.parse(texto);
    }

    private String leer(Scanner in, String pregunta) {
        System.out.print(pregunta + ": ");
        return in.nextLine();
    }
}
