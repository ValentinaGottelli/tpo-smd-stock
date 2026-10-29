# Rol B — Movimientos, validaciones y fábrica

**Carga:** ~20 % · **Patrones:** Decorator, Simple Factory
**Paquetes:** `domain/decorator`, `domain/factory`

---

## Mensaje para el grupo

> Me encargo de cómo se registra un movimiento y de los controles que se le
> agregan: validación de embalaje, FIFO y alerta de stock bajo, más la fábrica
> que arma la combinación según el tipo de movimiento. Dependo de los contratos
> de A y del repositorio que implementa D.

---

## Idea del módulo

Registrar un movimiento no es siempre igual: un ingreso valida el embalaje, un
despacho valida FIFO y avisa si el stock queda bajo. En vez de llenar el gestor
de `if`, cada control es un **decorador** que envuelve al registro base, y una
**fábrica** decide qué cadena armar.

```
RegistroMovimientoBase
        ▲
   DecoradorRegistro
        ▲
 ┌──────┴────────┬──────────────────┐
ValidacionEmbalaje  ValidacionFIFO  AlertaStockBajo
```

---

## Tareas

### B1 — Casos de aceptación (S0)

Documento `docs/equipo/casos-B.md` con entrada, resultado esperado y excepción.
Casos mínimos:

1. Ingreso con embalaje OK → registra.
2. Ingreso con embalaje dañado → `ReglaDeNegocioException`, no registra.
3. Despacho del ítem más antiguo → registra.
4. Despacho de uno más nuevo existiendo uno más antiguo → rechaza.
5. Despacho que deja el stock bajo el umbral → registra y emite alerta.
6. Ingreso → no evalúa FIFO ni alerta.

**Criterio:** A, C y E leyeron y acuerdan los mensajes de error.

### B2 — Base, decorador y fábrica (S1)

- `RegistroMovimientoBase` persiste con `RepositorioMovimientos`.
- `DecoradorRegistro` abstracto, con el siguiente registro protegido.
- `FabricaMovimiento` devuelve la cadena según el tipo:

| Tipo | Cadena |
|---|---|
| `INGRESO` | base + `ValidacionEmbalaje` |
| `RESERVA` | base |
| `CANCELACION` | base |
| `DESPACHO` | base + `ValidacionFIFO` + `AlertaStockBajo` |
| `AJUSTE` | base |

**Tests:** cada tipo devuelve una cadena no nula; las cadenas se pueden componer
en cualquier orden. Sin dependencias de Spring: se construyen con `new`.

### B3 — Validaciones (S2)

**`ValidacionEmbalaje`**: si `embalajeOk == false`, lanza
`ReglaDeNegocioException("Embalaje dañado: no se puede ingresar el ítem")`.
Solo aplica a `INGRESO`.

**`ValidacionFIFO`**: pide `buscarMasAntiguoEnDeposito()` y rechaza si el ítem
despachado no es el más antiguo, indicando cuál corresponde. Empate de fechas:
aceptar cualquiera de los más antiguos (documentarlo).

**`AlertaStockBajo`**: umbral `app.stock.umbral-minimo` (10 por defecto). Se
evalúa después del movimiento; si el stock queda por debajo, loguea `WARN` y
avisa al observador. **No aborta** el despacho.

**Tests:** embalaje dañado rechaza; FIFO con repositorio fake (4 casos); stock
12 → despacho de 3 → alerta; stock 20 → sin alerta.

### B4 — Cierre (S3)

- Test de cadena completa (base + dos decoradores) y que el registro base se
  llama exactamente una vez.
- Sección del informe: problema, estructura del Decorator, rol de la Factory y
  pruebas.
- Repaso del módulo para el oral.

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
| `IRegistroMovimiento` | contrato de A | contrato del decorador |
| `RepositorioMovimientos` | A / implementa D | persistir el movimiento |
| `RepositorioItems` | A / implementa D | FIFO y stock disponible |
| `ReglaDeNegocioException` | A | rechazos |
| `Movimiento`, `TipoMovimiento` | A | datos |

---

## Checklist

- [ ] Casos de aceptación documentados y consensuados.
- [ ] Base + decorador abstracto + fábrica con tests.
- [ ] Tres validaciones probadas con fakes.
- [ ] Umbral configurable y alerta sin abortar el flujo.
- [ ] Test de cadena completa.
- [ ] Sección del informe escrita.
