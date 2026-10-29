# Rol C — Reservas por canal y notificaciones

**Carga estimada:** 7 días de trabajo efectivo (~17 % del total)
**Patrones a cargo:** Strategy, Observer
**Paquetes:** `domain/strategy`, `domain/observer` (+ `ServicioConsultas`)

---

## Mensaje para pasar al grupo

> Me encargo de que cada canal reserve distinto (dreamö minorista, Rubín
> mayorista con mínimo de 5) y de que cada movimiento notifique al módulo de
> ventas y al dashboard de auditoría. También armo las consultas de lectura
> para los endpoints de inventario y auditoría. Dependo de los contratos de A y
> de la persistencia de D.

---

## Idea del módulo

**Strategy.** El canal no se resuelve con `if (canal == RUBIN)` dentro del
gestor: cada canal tiene su propia clase que valida la reserva.

- `DREAMO_LOCAL`, `DREAMO_MELI`, `DREAMO_FRAVEGA` → `ReservaMinorista`
  (por unidad, sin mínimo).
- `RUBIN` → `ReservaMayorista` (mínimo 5 unidades; un lote suma sus colchones).

**Observer.** `GestorDeStock` es el emisor y notifica a los suscriptos después
de cada movimiento:

- `ModuloVentas`: actualiza el stock disponible.
- `DashboardAuditoria`: registra el movimiento con fecha, hora y usuario.
- `AlertaStockBajo` de B también puede suscribirse (coordinar con B).

---

## Orden de trabajo

```
C1 casos de aceptación ──> C2 Strategy ──> C3 Observer
                                              │
                                              └──> C4 consultas + integración
                                                       │
                                                       └──> C5 informe
```

---

## Tareas

### C1 — Casos de aceptación (Sprint 0, 1 día)

Documento `docs/equipo/casos-C.md` con los escenarios de reserva y notificación:

1. dreamö reserva 1 colchón → acepta.
2. Rubín reserva 4 → rechaza con mensaje “mínimo 5 unidades”.
3. Rubín reserva 5 → acepta.
4. Rubín reserva un lote de 5 colchones → acepta (Composite).
5. Canal desconocido / nulo → rechaza con error claro.
6. Una transición notifica a `ModuloVentas` y `DashboardAuditoria` una sola vez.
7. Cancelar una reserva también notifica.
8. Si un observador falla, el movimiento ya persistido no se pierde (definir
   política con A: recomendado loguear el error y continuar).

**Criterio:** consensuado con A y E (mensajes de error y códigos HTTP).

---

### C2 — Strategy (Sprint 1, 2 días)

- `ReservaMinorista`: valida cantidad ≥ 1 y estado disponible.
- `ReservaMayorista`: valida `item.getCantidad() >= 5`; mensaje con la cantidad
  recibida.
- `SelectorEstrategia`: mapa `CanalVenta → EstrategiaReserva`; canal
  desconocido → `ReglaDeNegocioException`.
- Ambas implementaciones sin dependencias de Spring (se pueden instanciar con
  `new`); el selector sí puede ser un `@Component` con las estrategias
  inyectadas.

**Tests:**

- 4 unidades Rubín → excepción; 5 → OK.
- 1 unidad dreamö → OK; 0 → excepción.
- Lote de 5 → OK en Rubín.
- Canal nulo/desconocido → excepción.
- El selector devuelve la implementación correcta para cada canal.

---

### C3 — Observer (Sprint 2, 2 días)

- `ObservadorStock` y `EventoStock` (contrato de A, sección 6 de
  `CONTRATOS.md`).
- `ModuloVentas`: mantiene/expone el stock disponible; puede consultarlo con
  `RepositorioItems.stockDisponible()`.
- `DashboardAuditoria`: registra el movimiento en el repositorio de auditoría.
- Suscripción: `GestorDeStock` recibe `List<ObservadorStock>` por constructor
  (A lo cablea). No agregar auto-registro estático.
- Desuscripción: no es obligatoria para el TP; si se implementa, testearla.

**Tests con un observador fake:**

- Un movimiento notifica exactamente una vez a cada observador.
- El evento lleva estado anterior, estado nuevo, usuario, fecha y cantidad.
- Un observador que lanza excepción no rompe el flujo (según política acordada).

**Criterio:** `GestorDeStock` no conoce las clases concretas; solo la interfaz.

---

### C4 — Consultas e integración (Sprint 3, 1,5 días)

`ServicioConsultas` (en `domain/service`, archivo propio para no chocar con
`GestorDeStock`):

```java
public class ServicioConsultas {
    public List<ItemInventario> listar(EstadoProducto estado);
    public ItemInventario buscarPorCodigo(String codigo);
    public List<Movimiento> ultimosMovimientos(int limite);
}
```

- Delega en los puertos de A, no en repositorios JPA.
- Tests con fakes y un test de integración con la base.
- Verificar que la auditoría sobrevive al reinicio (perfil `devfile`).

---

### C5 — Informe y demo (Sprint 3–4, 1 día)

- Sección de Strategy: canales, mínimo mayorista, por qué no condicionales.
- Sección de Observer: emisor, observadores, integración con State.
- Guion de demo de reservas: minorista OK, mayorista rechazada, mayorista OK,
  cancelación y notificación en el dashboard.

---

## Qué NO hacés

- No tocás la matriz de transiciones (A) ni el registro de movimientos (B).
- No escribís repositorios JPA (D).
- No escribís controllers (E): entregás `ServicioConsultas` y E lo expone.

---

## Interfaces que consumís

| Interfaz | Dueño | Uso |
|---|---|---|
| `ItemInventario` | A | cantidad y estado |
| `EstrategiaReserva`, `SelectorEstrategia` | C (contrato de A) | reservas |
| `ObservadorStock`, `EventoStock` | A (contrato) | notificaciones |
| `RepositorioItems`, `RepositorioMovimientos` | A / D | consultas |
| `ReglaDeNegocioException` | A | rechazos |

---

## Checklist de entrega

- [ ] Casos de aceptación documentados.
- [ ] Strategy minorista/mayorista + selector, 6 tests verdes.
- [ ] Observer con dos observadores y test de notificación única.
- [ ] `ServicioConsultas` + tests.
- [ ] Auditoría persistida y verificada tras reinicio.
- [ ] Secciones del informe y guion de demo.
