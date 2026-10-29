# Rol B — Movimientos, validaciones y fábrica

**Carga estimada:** 8 días de trabajo efectivo (~20 % del total)
**Patrones a cargo:** Decorator, Simple Factory
**Paquetes:** `domain/decorator`, `domain/factory`

---

## Mensaje para pasar al grupo

> Me encargo de cómo se registra un movimiento y de los controles que se le
> agregan: validación de embalaje, FIFO y alerta de stock bajo, más la fábrica
> que arma la combinación según el tipo de movimiento. Dependo de las
> interfaces que deja A en el Sprint 0 y del repositorio que implementa D.

---

## Idea del módulo

Registrar un movimiento no es siempre igual:

- **Ingreso**: hay que validar que el embalaje no esté dañado.
- **Despacho**: hay que validar FIFO (el más antiguo primero) y avisar si el
  stock cae por debajo del umbral.
- **Reserva / cancelación / ajuste**: solo el registro base.

En vez de llenar `GestorDeStock` de `if`, cada control es un **decorador** que
envuelve al registro base, y una **fábrica** decide qué cadena armar.

```
RegistroMovimientoBase
        ▲
   DecoradorRegistro
        ▲
 ┌──────┴────────┬──────────────────┐
ValidacionEmbalaje  ValidacionFIFO  AlertaStockBajo
```

---

## Orden de trabajo

```
B1 casos de aceptación ──> B2 base + decorador + fábrica
                                   │
                                   ├──> B3 validaciones
                                   ├──> B4 alerta de stock
                                   └──> B5 tests + informe
```

---

## Tareas

### B1 — Casos de aceptación (Sprint 0, 1 día)

Documento `docs/equipo/casos-B.md` con los casos que tiene que cumplir el
módulo (entrada, resultado esperado, excepción). Sirve como base de los tests y
para la defensa oral.

Casos mínimos:

1. Ingreso con embalaje OK → registra.
2. Ingreso con embalaje dañado → `ReglaDeNegocioException`, no registra.
3. Despacho del ítem más antiguo → registra.
4. Despacho de un ítem más nuevo existiendo uno más antiguo → rechaza.
5. Despacho que deja el stock bajo el umbral → registra y emite alerta.
6. Ingreso que no toca el stock → no evalúa FIFO ni alerta.

**Criterio:** A, C y E lo leyeron y están de acuerdo con los mensajes de error.

---

### B2 — Base, decorador y fábrica (Sprint 1, 2 días)

- `RegistroMovimientoBase`: persiste con `RepositorioMovimientos` (puerto de A).
- `DecoradorRegistro`: clase abstracta con `siguiente` protegido.
- `FabricaMovimiento`: recibe `TipoMovimiento` y devuelve la cadena.

Combinaciones acordadas:

| Tipo | Cadena |
|---|---|
| `INGRESO` | base + `ValidacionEmbalaje` |
| `RESERVA` | base |
| `CANCELACION` | base |
| `DESPACHO` | base + `ValidacionFIFO` + `AlertaStockBajo` |
| `AJUSTE` | base |

**Tests:** cada tipo devuelve una cadena no nula; `INGRESO` rechaza embalaje
dañado; las cadenas se pueden componer en cualquier orden sin romper el
contrato.

**Criterio:** sin dependencias de Spring en las clases; construibles con `new`.

---

### B3 — Validaciones (Sprint 2, 2 días)

**`ValidacionEmbalaje`**

- Lee `Movimiento`/`EscaneoDTO.embalajeOk`; si es `false` lanza
  `ReglaDeNegocioException("Embalaje dañado: no se puede ingresar el ítem")`.
- Solo aplica a `INGRESO`.

**`ValidacionFIFO`**

- Pide a `RepositorioItems.buscarMasAntiguoEnDeposito()`.
- Si el ítem que se despacha no es el más antiguo → `ReglaDeNegocioException`
  indicando cuál corresponde.
- Empate de fechas: aceptar cualquiera de los más antiguos (documentarlo).
- Si no hay ítems disponibles → excepción clara.

**Criterio:** tests con un repositorio fake (sin base de datos) para los 4 casos
de FIFO.

---

### B4 — Alerta de stock bajo (Sprint 2, 1 día)

- Umbral configurable: `app.stock.umbral-minimo` (por defecto 10) en
  `application.properties`; se inyecta con `@Value`.
- Se evalúa **después** de aplicar el movimiento; si `stockDisponible()` queda
  por debajo, emite la alerta.
- Salida: log `WARN` y notificación a un `ObservadorStock` de producción (o al
  mismo bus de observadores que usa C). Definir con C para no duplicar.
- **No debe abortar** el despacho: la alerta es informativa.

**Criterio:** test con stock 12 → despacho de 3 → alerta emitida; stock 20 →
sin alerta.

---

### B5 — Tests de combinación e informe (Sprint 3, 1 día)

- Tests de cadenas completas (base + 2 decoradores).
- Verificar que el registro base se llama exactamente una vez por movimiento.
- Sección del informe: problema, estructura del Decorator, rol de la Factory,
  por qué no se usaron condicionales, pruebas.

---

## Qué NO hacés

- No cambiás las transiciones de estado (A).
- No definís reservas por canal (C).
- No escribís entidades ni consultas JPA (D).
- No escribís controllers (E).

---

## Interfaces que consumís

| Interfaz | Dueño | Uso |
|---|---|---|
| `IRegistroMovimiento` | compartida | contrato del decorador |
| `RepositorioMovimientos` | A / implementa D | persistir el movimiento |
| `RepositorioItems` | A / implementa D | FIFO y stock disponible |
| `ReglaDeNegocioException` | A | rechazos |
| `Movimiento`, `TipoMovimiento` | A | datos |

---

## Checklist de entrega

- [ ] Casos de aceptación documentados y consensuados.
- [ ] Base + decorador abstracto + fábrica con tests.
- [ ] Tres validaciones implementadas y probadas con fakes.
- [ ] Umbral configurable y alerta sin abortar el flujo.
- [ ] Test de cadena completa.
- [ ] Sección del informe escrita.
