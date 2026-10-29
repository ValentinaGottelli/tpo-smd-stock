# Contratos del dominio — congelar en Sprint 0

Fija **nombres, paquetes y firmas** para que los cinco programen en paralelo.
Se mergea antes de empezar el Sprint 1.

> **Regla:** si necesitás cambiar una firma, abrí un PR `contrato/<tema>`, avisá
> en el grupo y que lo revisen A y el dueño del módulo afectado.

---

## 1. Paquetes

| Paquete | Contenido | Dueño |
|---|---|---|
| `domain.model` | `ItemInventario`, `Movimiento`, enums | A |
| `domain.dto` | `EscaneoDTO`, `RespuestaEscaneoDTO` | A |
| `domain.state` | `EstadoItem` + 4 estados | A |
| `domain.composite` | `Colchon`, `Lote` | A |
| `domain.service` | `GestorDeStock` (A), `ServicioConsultas` (C) | A / C |
| `domain.facade` | `FachadaEscaneo` (interfaz congelada por A) | E |
| `domain.port` | `RepositorioItems`, `RepositorioMovimientos` | A (los implementa D) |
| `domain.exception` | Excepciones de negocio | A |
| `domain.decorator` | `IRegistroMovimiento` y decoradores | B |
| `domain.factory` | `FabricaMovimiento` | B |
| `domain.strategy` | `EstrategiaReserva`, `SelectorEstrategia` | C |
| `domain.observer` | `ObservadorStock`, `EventoStock` | C |
| `persistence.**` | Entidades JPA, repositorios, mappers | D |
| `presentation.**` | Controllers, DTOs HTTP, adapters | E |

Los DTOs del dominio viven en `domain.dto` para que el dominio **no dependa** de
la capa web. La capa REST tiene su `EscaneoRequest` con Bean Validation y lo
convierte.

---

## 2. Enums (`domain.model`)

```java
public enum EstadoProducto { PRODUCIDO, EN_DEPOSITO, RESERVADO, DESPACHADO }

public enum TipoItem { COLCHON, LOTE }

public enum TipoMovimiento { INGRESO, RESERVA, CANCELACION, DESPACHO, AJUSTE }

public enum CanalVenta { DREAMO_LOCAL, DREAMO_MELI, DREAMO_FRAVEGA, RUBIN }
```

Los tres `DREAMO_*` usan reserva minorista; `RUBIN`, mayorista.

---

## 3. Modelo

```java
public interface ItemInventario {
    String getCodigo();
    int getCantidad();
    LocalDate getFechaProduccion();
    EstadoProducto getEstado();

    void ingresar();
    void reservar();
    void cancelarReserva();
    void despachar();

    /** Solo lo usan las implementaciones de EstadoItem. */
    void cambiarEstado(EstadoProducto nuevoEstado);
}
```

```java
public record Movimiento(
        String codigoItem,
        TipoMovimiento tipo,
        CanalVenta canal,               // null en INGRESO y AJUSTE
        String usuario,
        LocalDateTime fechaHora,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad
) { }
```

```java
public final class Colchon implements ItemInventario {
    public Colchon(String codigo, EstadoProducto estado, LocalDate fechaProduccion);
}

public final class Lote implements ItemInventario {
    public Lote(String codigo, EstadoProducto estado, LocalDate fechaProduccion);
    public void agregar(ItemInventario item);
    public List<ItemInventario> getItems();
}
```

Invariantes:

- `Colchon.getCantidad()` devuelve siempre `1`.
- `Lote.getCantidad()` suma sus hijos (0 si está vacío).
- `Lote.cambiarEstado(estado)` aplica el cambio a todos los hijos.
- Un ítem `DESPACHADO` no admite más transiciones.

---

## 4. State (`domain.state`)

```java
public interface EstadoItem {
    EstadoProducto valor();
    void ingresar(ItemInventario item);
    void reservar(ItemInventario item);
    void cancelarReserva(ItemInventario item);
    void despachar(ItemInventario item);
}
```

Implementaciones: `EstadoProducido`, `EstadoEnDeposito`, `EstadoReservado`,
`EstadoDespachado`. Transición inválida → `TransicionInvalidaException`.

---

## 5. Puertos (`domain.port`)

```java
public interface RepositorioItems {
    Optional<ItemInventario> buscarPorCodigo(String codigo);
    List<ItemInventario> listarPorEstado(EstadoProducto estado);
    Optional<ItemInventario> buscarMasAntiguoEnDeposito();   // FIFO
    List<ItemInventario> listarTodos();
    void guardar(ItemInventario item);

    default int stockDisponible() {
        return listarPorEstado(EstadoProducto.EN_DEPOSITO).stream()
                .mapToInt(ItemInventario::getCantidad)
                .sum();
    }
}
```

```java
public interface RepositorioMovimientos {
    void guardar(Movimiento movimiento);
    List<Movimiento> ultimos(int limite);
}
```

Son el **único** acceso del dominio a la base. D los implementa con JPA.

---

## 6. Strategy y Observer

```java
public interface EstrategiaReserva {
    CanalVenta canal();
    void validar(ItemInventario item);   // lanza ReglaDeNegocioException
}

public interface SelectorEstrategia {
    EstrategiaReserva para(CanalVenta canal);
}
```

```java
public record EventoStock(Movimiento movimiento, int stockDisponible) { }

public interface ObservadorStock {
    void alOcurrirMovimiento(EventoStock evento);
}
```

---

## 7. Decorator, Factory, Gestor y Fachada

```java
public interface IRegistroMovimiento {
    void registrar(Movimiento movimiento);
}
```

`RegistroMovimientoBase` persiste con `RepositorioMovimientos`.
`DecoradorRegistro` es abstracto y envuelve a otro `IRegistroMovimiento`.
Decoradores: `ValidacionEmbalaje`, `ValidacionFIFO`, `AlertaStockBajo`.

```java
public interface FabricaMovimiento {
    IRegistroMovimiento crear(TipoMovimiento tipo);
}
```

```java
public class GestorDeStock {
    public void ingresar(EscaneoDTO solicitud);
    public void reservar(EscaneoDTO solicitud);
    public void cancelarReserva(EscaneoDTO solicitud);
    public void despachar(EscaneoDTO solicitud);
}
```

```java
public interface FachadaEscaneo {
    RespuestaEscaneoDTO registrarIngreso(EscaneoDTO solicitud);
    RespuestaEscaneoDTO reservar(EscaneoDTO solicitud);
    RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud);
    RespuestaEscaneoDTO despachar(EscaneoDTO solicitud);
}
```

**Facade:** la interfaz la congela A en el Sprint 0; la implementación es de E.
Es la única puerta de entrada que usa la capa REST.

**Singleton:** `GestorDeStock` es `@Service`; Spring garantiza una única
instancia. Si la cátedra pide el `getInstance()` clásico, se agrega un holder
estático aparte sin romper la inyección.

---

## 8. Matriz de transiciones

| Estado actual | `ingresar()` | `reservar()` | `cancelarReserva()` | `despachar()` |
|---|---|---|---|---|
| `PRODUCIDO` | → `EN_DEPOSITO` | error | error | error |
| `EN_DEPOSITO` | error | → `RESERVADO` | error | error |
| `RESERVADO` | error | error | → `EN_DEPOSITO` | → `DESPACHADO` |
| `DESPACHADO` | error | error | error | error |

Cada “error” lanza `TransicionInvalidaException` y **no** modifica nada.

---

## 9. DTOs del dominio

```java
public record EscaneoDTO(
        String codigo,
        TipoMovimiento tipo,
        CanalVenta canal,
        String usuario,
        Boolean embalajeOk      // null = true
) { }
```

```java
public record RespuestaEscaneoDTO(
        String codigo,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad,
        String mensaje,
        LocalDateTime fechaHora
) { }
```

---

## 10. Excepciones

| Excepción | Situación | HTTP |
|---|---|---|
| `ExcepcionDeNegocio` | base de las reglas | 409 |
| `TransicionInvalidaException` | State inválido | 409 |
| `ReglaDeNegocioException` | Strategy / Decorator rechaza | 409 |
| `RecursoNoEncontradoException` | código inexistente | 404 |

E las traduce en un `@RestControllerAdvice` a
`ErrorDTO(codigo, mensaje, fechaHora, path)`.

---

## 11. API acordada

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/escaneos/ingreso` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/reserva` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/cancelacion` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/despacho` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| GET | `/api/inventario?estado=` | — | `List<ItemInventarioResponse>` |
| GET | `/api/inventario/{codigo}` | — | `ItemInventarioResponse` |
| GET | `/api/auditoria?limite=50` | — | `List<MovimientoResponse>` |
| GET | `/api/health` | — | `HealthResponse` (ya existe) |

```json
POST /api/escaneos/reserva
{
  "codigo": "COLCHON-QR-0001",
  "canal": "RUBIN",
  "usuario": "operario01",
  "embalajeOk": true
}
```

---

## 12. Configuración compartida

| Propiedad | Valor | Dueño |
|---|---|---|
| `app.stock.umbral-minimo` | `10` | B (`AlertaStockBajo`) |
| `app.escaneo.usuario-default` | `operario01` | E |

---

## 13. Checklist del Sprint 0

- [ ] Todas las interfaces de este documento están en `main` y compilan.
- [ ] Cada rol confirmó que puede implementar su parte sin tocar interfaces ajenas.
- [ ] D confirmó que los puertos se implementan con JPA sin cambios.
- [ ] E confirmó que los DTOs cubren todos los endpoints.
- [ ] El tablero tiene una tarjeta por tarea del plan maestro.
