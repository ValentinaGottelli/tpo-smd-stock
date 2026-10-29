# Rol A — Núcleo de dominio e integración

**Dueño:** vos · **Carga:** ~30 % · **Patrones:** State, Composite, Singleton, Facade

**Paquetes:** `domain/model`, `domain/dto`, `domain/state`, `domain/composite`,
`domain/service` (solo `GestorDeStock`), `domain/facade`, `domain/port`,
`domain/exception`

---

## Mensaje para el grupo

> Me encargo del núcleo: modelo, State, Composite, GestorDeStock y Fachada, más
> la integración final. En los primeros días dejo mergeadas todas las interfaces
> para que puedan arrancar sin esperarme. No toco sus paquetes, y cualquier
> cambio de interfaz va por PR `contrato/*`.

---

## Por qué te toca

Es la parte con más decisiones de diseño: define el modelo, el ciclo de vida, la
composición colchón/lote, el orquestador y la única puerta de entrada al
dominio. Además concentra los cuatro patrones que cruzan todo el TP.

---

## Tareas

### A1 — Contratos (S0)

Mergear las interfaces, DTOs y excepciones de `CONTRATOS.md`, sin
implementación.

- `domain/model`: `ItemInventario`, `Movimiento`, enums
- `domain/dto`: `EscaneoDTO`, `RespuestaEscaneoDTO`
- `domain/state`: `EstadoItem`
- `domain/port`: `RepositorioItems`, `RepositorioMovimientos`
- `domain/strategy`, `domain/observer`, `domain/decorator`, `domain/factory`,
  `domain/facade`: solo las interfaces
- `domain/exception`: las cuatro excepciones

**Criterio:** `mvnw test` verde y B, C, D y E confirman que pueden implementar
contra esas firmas. **Esta tarea desbloquea a todos: hacerla primero.**

### A2 — Composite (S1)

- `Colchon`: cantidad fija 1, código y fecha de producción.
- `Lote`: lista de hijos, `getCantidad()` suma, `cambiarEstado()` en cascada.
- Validar código no vacío, fecha no nula, no agregarse a sí mismo.

**Tests:** colchón devuelve 1; lote de 3 + sub-lote de 2 devuelve 5; despachar
el lote despacha todos los hijos; agregarse a sí mismo lanza excepción.

### A3 — State (S1)

- `EstadoProducido`, `EstadoEnDeposito`, `EstadoReservado`, `EstadoDespachado`.
- Implementar la matriz de transiciones de `CONTRATOS.md`.
- La clase concreta de ítem delega cada operación en su estado actual.

**Tests:** tabla de 16 combinaciones (4 estados × 4 operaciones); el caso
inválido lanza excepción **y no cambia el estado**.

### A4 — GestorDeStock (S2)

Orquesta el flujo:

```
buscar ítem → validar transición → (Strategy si es reserva)
→ fabricar registro con decoradores → aplicar cambio de estado
→ notificar observadores → persistir → devolver resultado
```

- Recibe por constructor: repositorios, `SelectorEstrategia`,
  `FabricaMovimiento` y `List<ObservadorStock>`.
- `@Service` (singleton de Spring). Explicar en el Javadoc por qué cumple
  Singleton.
- Errores: código inexistente → `RecursoNoEncontradoException`; transición
  inválida → `TransicionInvalidaException`; reglas → las de Strategy/Decorator.
- La operación debe ser atómica: si falla, no queda estado cambiado ni
  movimiento guardado (`@Transactional`).

**Tests (Mockito):** ingreso, reserva minorista, reserva mayorista rechazada,
despacho inválido, cancelación, notificación única y que no se notifique si
falla la persistencia.

### A5 — Fachada y wiring (S2)

- `FachadaEscaneo` implementa los 4 métodos y delega en `GestorDeStock`.
- **Sin reglas propias**: solo arma la solicitud y devuelve el DTO.
- Cablear los beans que falten sin invadir paquetes de B, C y D.
- Es la única clase que presentación puede usar del dominio.

**Criterio:** los 8 escenarios de la demo pasan en H2.

### A6 — Cierre (S3)

- Diagrama de clases y diagrama de secuencia del escaneo.
- Sección de arquitectura y de State/Composite/Singleton/Facade para el informe.
- Guion técnico de la demo y tag `v1.0`.

---

## Qué NO hacés

- No implementás reservas por canal (C) ni validaciones de movimiento (B).
- No escribís entidades JPA ni mappers (D).
- No escribís controllers ni adapters (E).
- No cambiás `pom.xml` por pedidos ajenos sin revisar el impacto.

---

## Cómo probar sin el resto

- A2 y A3: tests puros de Java, sin Spring.
- A4: Mockito para puertos, estrategia, fábrica y observadores.
- A5: `GestorDeStock` mockeado.

---

## Checklist

- [ ] Contratos mergeados antes del Sprint 1.
- [ ] 16 tests de State + tests de Composite.
- [ ] `GestorDeStock` con los 7 tests de flujo.
- [ ] `FachadaEscaneo` sin reglas propias.
- [ ] 8 escenarios funcionando en H2.
- [ ] Diagramas y secciones del informe.
- [ ] Demo ensayada y tag `v1.0`.
