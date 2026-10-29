package com.smd.inventario.presentation.demo;

import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de demo para mostrar State + Composite desde Postman.
 *
 * <p><b>No es la API definitiva del TP.</b> Es una herramienta para la clase:
 * crea ítems en memoria y permite ejecutar las cuatro operaciones del ciclo de
 * vida. La API real (con fachada, persistencia y validación) es la tarea del
 * rol E.</p>
 *
 * <p><b>¿Por qué el dominio no tiene anotaciones?</b> Porque el núcleo se
 * mantiene independiente del framework: las clases de {@code domain} son Java
 * puro y se pueden probar sin levantar Spring. Las anotaciones viven en esta
 * capa de presentación y en {@code config}.</p>
 *
 * <p><b>¿Por qué el constructor no lleva {@code @Autowired}?</b> Porque la
 * clase tiene un único constructor y Spring lo usa automáticamente desde la
 * versión 4.3. La inyección por constructor es la recomendada; el
 * {@code @Autowired} solo hace falta si hay más de un constructor.</p>
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final InventarioDemo inventario;

    public DemoController(InventarioDemo inventario) {
        this.inventario = inventario;
    }

    @PostMapping("/colchones")
    public ItemDemoResponse crearColchon(@RequestBody CrearColchonRequest request) {
        EstadoProducto estado = request.estado() == null ? EstadoProducto.PRODUCIDO : request.estado();
        return ItemDemoResponse.desde(inventario.crearColchon(request.codigo(), estado));
    }

    @PostMapping("/lotes")
    public ItemDemoResponse crearLote(@RequestBody CrearLoteRequest request) {
        EstadoProducto estado = request.estado() == null ? EstadoProducto.PRODUCIDO : request.estado();
        int cantidad = request.cantidad() == null ? 5 : request.cantidad();
        return ItemDemoResponse.desde(inventario.crearLote(request.codigo(), estado, cantidad));
    }

    @PostMapping("/items/{codigo}/ingresar")
    public AccionDemoResponse ingresar(@PathVariable String codigo) {
        return ejecutar(codigo, ItemInventario::ingresar, "ingreso");
    }

    @PostMapping("/items/{codigo}/reservar")
    public AccionDemoResponse reservar(@PathVariable String codigo) {
        return ejecutar(codigo, ItemInventario::reservar, "reserva");
    }

    @PostMapping("/items/{codigo}/cancelar")
    public AccionDemoResponse cancelar(@PathVariable String codigo) {
        return ejecutar(codigo, ItemInventario::cancelarReserva, "cancelacion");
    }

    @PostMapping("/items/{codigo}/despachar")
    public AccionDemoResponse despachar(@PathVariable String codigo) {
        return ejecutar(codigo, ItemInventario::despachar, "despacho");
    }

    @GetMapping("/items")
    public List<ItemDemoResponse> listar() {
        return inventario.listar().stream().map(ItemDemoResponse::desde).toList();
    }

    @PostMapping("/reset")
    public MensajeResponse reiniciar() {
        inventario.reiniciar();
        return new MensajeResponse("Inventario de demo reiniciado");
    }

    private AccionDemoResponse ejecutar(String codigo, Consumer<ItemInventario> accion, String operacion) {
        ItemInventario item = inventario.buscar(codigo);
        accion.accept(item);
        return new AccionDemoResponse(
                item.getCodigo(), item.getEstado(), item.getCantidad(), operacion + " registrado");
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDemoResponse transicionInvalida(TransicionInvalidaException e, HttpServletRequest request) {
        return new ErrorDemoResponse("TRANSICION_INVALIDA", e.getMessage(),
                LocalDateTime.now(), request.getRequestURI());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDemoResponse noEncontrado(RecursoNoEncontradoException e, HttpServletRequest request) {
        return new ErrorDemoResponse("RECURSO_NO_ENCONTRADO", e.getMessage(),
                LocalDateTime.now(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDemoResponse solicitudInvalida(IllegalArgumentException e, HttpServletRequest request) {
        return new ErrorDemoResponse("SOLICITUD_INVALIDA", e.getMessage(),
                LocalDateTime.now(), request.getRequestURI());
    }

    // --- DTOs de la demo -------------------------------------------------

    public record CrearColchonRequest(String codigo, EstadoProducto estado) { }

    public record CrearLoteRequest(String codigo, EstadoProducto estado, Integer cantidad) { }

    public record ItemDemoResponse(
            String codigo,
            String tipo,
            EstadoProducto estado,
            int cantidad,
            List<ItemDemoResponse> hijos
    ) {
        static ItemDemoResponse desde(ItemInventario item) {
            List<ItemDemoResponse> hijos = item instanceof Lote lote
                    ? lote.getItems().stream().map(ItemDemoResponse::desde).toList()
                    : List.of();
            return new ItemDemoResponse(
                    item.getCodigo(),
                    item instanceof Lote ? "LOTE" : "COLCHON",
                    item.getEstado(),
                    item.getCantidad(),
                    hijos);
        }
    }

    public record AccionDemoResponse(String codigo, EstadoProducto estado, int cantidad, String mensaje) { }

    public record MensajeResponse(String mensaje) { }

    public record ErrorDemoResponse(String codigo, String mensaje, LocalDateTime fechaHora, String path) { }
}
