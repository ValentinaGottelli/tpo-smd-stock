# Contratos del dominio — congelar en Sprint 0

Este documento fija **nombres, paquetes y firmas** para que las cinco personas
puedan programar en paralelo. Se mergea antes de empezar el Sprint 1.

> **Regla:** si necesitás cambiar una firma, abrí un PR `contrato/<tema>`,
> avisá en el grupo y que lo revise A + el dueño del módulo afectado.
> Un cambio silencioso acá rompe el trabajo de los demás.

---

## 1. Paquetes y responsabilidades

| Paquete | Contenido | Dueño |
|---|---|---|
| `domain.model` | `ItemInventario`, `Movimiento`, enums | A |
| `domain.dto` | `EscaneoDTO`, `RespuestaEscaneoDTO` (contrato con la fachada) | A |
| `domain.state` | `EstadoItem` + 4 estados | A |
| `domain.composite` | `Colchon`, `Lote` | A |
| `domain.service` | `GestorDeStock` (A), `ServicioConsultas` (C) | A / C |
| `domain.facade` | `FachadaEscaneo` | A |
| `domain.port` | `RepositorioItems`, `RepositorioMovimientos` | A (los implementa D) |
| `domain.exception` | Excepciones de negocio | A |
| `domain.decorator` | `IRegistroMovimiento`, decoradores | B |
| `domain.factory` | `FabricaMovimiento` | B |
| `domain.strategy` | `EstrategiaReserva` + implementaciones | C |
| `domain.observer` | `ObservadorStock`, `EventoStock` | C |
| `persistence.**` | Entidades JPA, repositorios, mappers | D |
| `presentation.**` | Controllers, request/response HTTP, adapters | E |

> Los DTOs de entrada/salida del dominio viven en `domain.dto` para que el
> dominio **no dependa** de la capa web (tal como pide el informe). La capa REST
> tiene su propio `EscaneoRequest` con las validaciones de Bean Validation y lo
> convierte a `EscaneoDTO`.

---

## 2. Enums (`domain.model`)

```java
public enum EstadoProducto { PRODUCIDO, EN_DEPOSITO, RESERVADO, DESPACHADO }

public enum TipoItem { COLCHON, LOTE }

public enum TipoMovimiento { INGRESO, RESERVA, CANCELACION, DESPACHO, AJUSTE }

public enum CanalVenta { DREAMO_LOCAL, DREAMO_MELI, DREAMO_FRAVEGA, RUBIN }
```

Mapeo de canales a estrategia: los tres `DREAMO_*` → minorista; `RUBIN` →
mayorista.

---

## 3. Modelo

```java
package com.smd.inventario.domain.model;

public interface ItemInventario {
    String getCodigo();
    int getCantidad();
    LocalDate getFechaProduccion();
    EstadoProducto getEstado();

    /** Transiciones del patrón State (ver matriz en la sección 8). */
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
        CanalVenta canal,              // null en INGRESO y AJUSTE
        String usuario,
        LocalDateTime fechaHora,
        EstadoProducto estadoAnterior,
        EstadoProducto estadoNuevo,
        int cantidad
) { }
```

`Colchon` y `Lote` (en `domain.composite`):

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
- `Lote.getCantidad()` devuelve la suma de sus hijos (0 si está vacío; un lote
  vacío no debe persistirse ni reservarse).
- `Lote.cambiarEstado(estado)` aplica el cambio a todos sus hijos.
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
`EstadoDespachado`. Cada transición inválida lanza
`TransicionInvalidaException`.

---

## 5. Puertos de persistencia (`domain.port`)

```java
public interface RepositorioItems {
    Optional<ItemInventario> buscarPorCodigo(String codigo);
    List<ItemInventario> listarPorEstado(EstadoProducto estado);
    Optional<ItemInventario> buscarMasAntiguoEnDeposito();   // criterio FIFO
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

Estas interfaces son el **único** acceso del dominio a la base. D las implementa
con JPA en `persistence.repository`.

---

## 6. Strategy y Observer

```java
package com.smd.inventario.domain.strategy;

public interface EstrategiaReserva {
    CanalVenta canal();
    void validar(ItemInventario item);   // lanza ReglaDeNegocioException
}

public interface SelectorEstrategia {
    EstrategiaReserva para(CanalVenta canal);
}
```

```java
package com.smd.inventario.domain.observer;

public record EventoStock(Movimiento movimiento, int stockDisponible) { }

public interface ObservadorStock {
    void alOcurrirMovimiento(EventoStock evento);
}
```

---

## 7. Decorator, Factory, Gestor y Fachada

```java
package com.smd.inventario.domain.decorator;

public interface IRegistroMovimiento {
    void registrar(Movimiento movimiento);
}
```

`RegistroMovimientoBase` persiste el movimiento con `RepositorioMovimientos`.
`DecoradorRegistro` es abstracto y envuelve a otro `IRegistroMovimiento`.
Decoradores concretos: `ValidacionEmbalaje`, `ValidacionFIFO`, `AlertaStockBajo`.

```java
package com.smd.inventario.domain.factory;

public interface FabricaMovimiento {
    IRegistroMovimiento crear(TipoMovimiento tipo);
}
```

```java
package com.smd.inventario.domain.service;

public class GestorDeStock {
    public void ingresar(EscaneoDTO solicitud);
    public void reservar(EscaneoDTO solicitud);
    public void cancelarReserva(EscaneoDTO solicitud);
    public void despachar(EscaneoDTO solicitud);
}
```

```java
package com.smd.inventario.domain.facade;

public interface FachadaEscaneo {
    RespuestaEscaneoDTO registrarIngreso(EscaneoDTO solicitud);
    RespuestaEscaneoDTO reservar(EscaneoDTO solicitud);
    RespuestaEscaneoDTO cancelarReserva(EscaneoDTO solicitud);
    RespuestaEscaneoDTO despachar(EscaneoDTO solicitud);
}
```

> **Singleton:** `GestorDeStock` se declara `@Service` y Spring garantiza una
> única instancia (scope singleton). Si la cátedra exige el `getInstance()`
> clásico, se agrega un holder estático en una clase aparte, sin romper la
> inyección. Confirmar con el docente antes de la entrega.

---

## 8. Matriz de transiciones

| Estado actual | `ingresar()` | `reservar()` | `cancelarReserva()` | `despachar()` |
|---|---|---|---|---|
| `PRODUCIDO` | → `EN_DEPOSITO` | error | error | error |
| `EN_DEPOSITO` | error | → `RESERVADO` | error | error |
| `RESERVADO` | error | error | → `EN_DEPOSITO` | → `DESPACHADO` |
| `DESPACHADO` | error | error | error | error |

Toda celda “error” lanza `TransicionInvalidaException` y **no** modifica el ítem
ni persiste nada.

---

## 9. DTOs del dominio (`domain.dto`)

```java
public record EscaneoDTO(
        String codigo,
        TipoMovimiento tipo,
        CanalVenta canal,
        String usuario,
        Boolean embalajeOk,        // null = true; lo usa ValidacionEmbalaje
        LocalDateTime fechaHora    // null = ahora
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

## 10. Excepciones (`domain.exception`)

| Excepción | Situación | HTTP |
|---|---|---|
| `ExcepcionDeNegocio` | base de las reglas | 409 |
| `TransicionInvalidaException` | State inválido | 409 |
| `ReglaDeNegocioException` | Strategy / Decorator rechaza | 409 |
| `RecursoNoEncontradoException` | código inexistente | 404 |

E las traduce en un `@RestControllerAdvice` a un JSON `ErrorDTO(codigo, mensaje,
fechaHora, path)`.

---

## 11. API acordada (la implementa E)

| Método | Ruta | Body / query | Respuesta |
|---|---|---|---|
| POST | `/api/escaneos/ingreso` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/reserva` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/cancelacion` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| POST | `/api/escaneos/despacho` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| GET | `/api/inventario?estado=` | — | `List<ItemInventarioResponse>` |
| GET | `/api/inventario/{codigo}` | — | `ItemInventarioResponse` |
| GET | `/api/auditoria?limite=50` | — | `List<MovimientoResponse>` |
| GET | `/api/health` | — | `HealthResponse` (ya existe) |

`EscaneoRequest`: `codigo`, `canal`, `usuario`, `embalajeOk`, `fechaHora`
(opcional). El tipo de movimiento lo fija el endpoint; el DTO de dominio lo
declara para trazabilidad.

Ejemplo:

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
| `app.stock.umbral-minimo` | `10` | B (la usa `AlertaStockBajo`) |
| `app.escaneo.usuario-default` | `operario01` | E |
| Perfiles | `dev`, `devfile`, `mysql` | ya configurados |

---

## 13. Checklist antes de cerrar el Sprint 0

- [ ] Todas las interfaces de este documento están en `main` y compilan.
- [ ] Cada rol confirmó que puede implementar su parte sin tocar interfaces ajenas.
- [ ] D confirmó que los puertos se pueden implementar con JPA sin cambios.
- [ ] E confirmó que los DTOs cubren todos los endpoints.
- [ ] El tablero tiene una tarjeta por tarea del plan maestro.
