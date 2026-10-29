# Rol C — Reservas por canal y notificaciones

**Carga:** ~18 % · **Patrones:** Strategy, Observer
**Paquetes:** `domain/strategy`, `domain/observer`, `domain/service/ServicioConsultas`

---

## Mensaje para el grupo

> Me encargo de que cada canal reserve distinto (dreamö minorista, Rubín
> mayorista con mínimo de 5) y de que cada movimiento notifique al módulo de
> ventas y al dashboard de auditoría. También armo las consultas de lectura para
> los endpoints de inventario y auditoría. Dependo de los contratos de A y de la
> persistencia de D.

---

## Idea del módulo

**Strategy.** El canal no se resuelve con `if (canal == RUBIN)` dentro del
gestor: cada canal tiene su propia clase que valida la reserva.

- `DREAMO_*` → `ReservaMinorista` (por unidad, sin mínimo).
- `RUBIN` → `ReservaMayorista` (mínimo 5; un lote suma sus colchones).

**Observer.** `GestorDeStock` notifica a los suscriptos después de cada
movimiento: `ModuloVentas` (stock disponible) y `DashboardAuditoria` (registro
con fecha, hora y usuario). `AlertaStockBajo` de B también puede suscribirse.

---

## Tareas

### C1 — Casos de aceptación (S0)

Documento `docs/equipo/casos-C.md`:

1. dreamö reserva 1 colchón → acepta.
2. Rubín reserva 4 → rechaza (“mínimo 5 unidades”).
3. Rubín reserva 5 → acepta.
4. Rubín reserva un lote de 5 → acepta (Composite).
5. Canal nulo o desconocido → error claro.
6. Una transición notifica a los dos observadores una sola vez.
7. Cancelar una reserva también notifica.
8. Si un observador falla, el movimiento ya persistido no se pierde (política:
   loguear y continuar).

**Criterio:** consensuado con A y E (mensajes de error y códigos HTTP).

### C2 — Strategy (S1)

- `ReservaMinorista`: cantidad ≥ 1 y estado disponible.
- `ReservaMayorista`: `cantidad >= 5`; mensaje con la cantidad recibida.
- `SelectorEstrategia`: mapa `CanalVenta → EstrategiaReserva`; canal
  desconocido → `ReglaDeNegocioException`.
- Las estrategias se construyen con `new`; el selector puede ser un `@Component`.

**Tests:** Rubín 4 rechaza / 5 acepta; dreamö 1 acepta / 0 rechaza; lote de 5
acepta en Rubín; canal nulo rechaza; el selector devuelve la implementación
correcta.

### C3 — Observer y consultas (S2)

- `ModuloVentas`: mantiene/expone el stock disponible.
- `DashboardAuditoria`: registra el movimiento para la auditoría.
- Suscripción: `GestorDeStock` recibe `List<ObservadorStock>` por constructor.
  Sin registros estáticos.

`ServicioConsultas` en `domain/service` (archivo propio, no tocar
`GestorDeStock`):

```java
public class ServicioConsultas {
    public List<ItemInventario> listar(EstadoProducto estado);
    public ItemInventario buscarPorCodigo(String codigo);
    public List<Movimiento> ultimosMovimientos(int limite);
}
```

Delega en los puertos de A, no en repositorios JPA.

**Tests:** un movimiento notifica exactamente una vez a cada observador; el
evento lleva estado anterior/nuevo, usuario, fecha y cantidad; un observador que
falla no rompe el flujo.

### C4 — Cierre (S3)

- Sección de Strategy (canales, mínimo mayorista) y de Observer (emisor,
  observadores, integración con State).
- Guion de demo: minorista OK, mayorista rechazada, mayorista OK, cancelación y
  notificación en el dashboard.

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

## Checklist

- [ ] Casos de aceptación documentados.
- [ ] Strategy minorista/mayorista + selector con tests.
- [ ] Observer con dos observadores y test de notificación única.
- [ ] `ServicioConsultas` + tests.
- [ ] Auditoría visible tras reiniciar con `devfile`.
- [ ] Secciones del informe y guion de demo.
