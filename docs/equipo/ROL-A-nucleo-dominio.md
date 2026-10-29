# Rol A — Núcleo de dominio e integración

**Dueño:** vos
**Carga estimada:** 12 días de trabajo efectivo (~30 % del total)
**Patrones a cargo:** State, Composite, Singleton, Facade
**Paquetes:** `domain/model`, `domain/dto`, `domain/state`, `domain/composite`,
`domain/service`, `domain/facade`, `domain/port`, `domain/exception`

---

## Mensaje para pasar al grupo

> Me encargo del núcleo: modelo, State, Composite, GestorDeStock y Fachada, más
> la integración final. En los primeros 3 días dejo mergeadas todas las
> interfaces (contratos) para que ustedes puedan arrancar sin esperarme. No
> toco sus paquetes salvo que lo coordinemos, y cualquier cambio de interfaz va
> por PR `contrato/*`.

---

## Por qué te toca esta parte

Es la porción con más decisiones de diseño y la que más código de integración
tiene: define el modelo, el ciclo de vida, la composición colchón/lote, el
orquestador central y la única puerta de entrada al dominio. Además concentra
los cuatro patrones que cruzan todo el TP.

---

## Orden de trabajo

```
A1 contratos ──> A2 Composite ──> A3 State ──> A4 GestorDeStock ──> A5 Fachada
                                                   │
                                                   └──> A6 integración / wiring
                                                              │
                                                              └──> A7 informe / A8 demo
```

---

## Tareas

### A1 — Congelar contratos (Sprint 0, 1 día)

**Entregable:** PR `feat/contratos-dominio` con interfaces y excepciones sin
implementación.

Archivos:

- `domain/model/ItemInventario.java`, `Movimiento.java`, `TipoMovimiento.java`, `CanalVenta.java`
- `domain/dto/EscaneoDTO.java`, `RespuestaEscaneoDTO.java`
- `domain/state/EstadoItem.java`
- `domain/port/RepositorioItems.java`, `RepositorioMovimientos.java`
- `domain/strategy/EstrategiaReserva.java`, `SelectorEstrategia.java`
- `domain/observer/ObservadorStock.java`, `EventoStock.java`
- `domain/decorator/IRegistroMovimiento.java`
- `domain/factory/FabricaMovimiento.java`
- `domain/facade/FachadaEscaneo.java`
- `domain/exception/ExcepcionDeNegocio.java`, `TransicionInvalidaException.java`, `ReglaDeNegocioException.java`, `RecursoNoEncontradoException.java`

**Criterio de aceptación:** `mvnw test` verde; B, C, D y E confirman por el chat
que pueden implementar contra esas firmas.

> Esta tarea es la que habilita el paralelismo. Hacerla primero, sí o sí.

---

### A2 — Composite: `Colchon` y `Lote` (Sprint 1, 2 días)

- `Colchon`: cantidad fija 1, código propio, fecha de producción.
- `Lote`: lista de hijos, `getCantidad()` suma, `cambiarEstado()` en cascada.
- Validaciones: código no vacío, fecha no nula, no agregar un ítem ya
  despachado, no agregar un lote dentro de sí mismo.

**Tests (JUnit 5 + AssertJ):**

1. Colchón devuelve cantidad 1.
2. Lote con 3 colchones + 1 lote hijo de 2 devuelve 5 (composición recursiva).
3. `lote.despachar()` deja a todos los hijos en `DESPACHADO`.
4. Agregar el mismo lote a sí mismo lanza excepción.

**Criterio:** los 4 tests verdes y sin dependencias de Spring.

---

### A3 — State: los cuatro estados (Sprint 1, 2 días)

- `EstadoProducido`, `EstadoEnDeposito`, `EstadoReservado`, `EstadoDespachado`.
- Implementar la matriz de transiciones de `CONTRATOS.md` (sección 8).
- `ItemInventarioBase` (o la implementación que elijas) delega cada operación
  en su `EstadoItem` actual.

**Tests:** tabla de 16 combinaciones (4 estados × 4 operaciones), verificando
transición o excepción. Incluir que un error **no** cambia el estado.

**Criterio:** 16 casos verdes; `TransicionInvalidaException` con mensaje claro
(qué estado, qué operación).

---

### A4 — `GestorDeStock` (Singleton) (Sprint 2, 2 días)

Orquesta el flujo completo:

```
buscar ítem → validar transición → (Strategy si es reserva)
→ Fabricar registro con decoradores → aplicar cambio de estado
→ notificar observadores → persistir → devolver resultado
```

- Recibe por constructor: `RepositorioItems`, `RepositorioMovimientos`,
  `SelectorEstrategia`, `FabricaMovimiento`, `List<ObservadorStock>`.
- `@Service` (singleton de Spring). Documentar en el Javadoc por qué cumple el
  patrón Singleton y qué pasa si se levantan dos instancias de la app (mismo
  límite que menciona el informe).
- Errores: código inexistente → `RecursoNoEncontradoException`; transición
  inválida → `TransicionInvalidaException`; regla de negocio → la que lance la
  estrategia o el decorador.
- La operación debe ser **atómica**: si algo falla, no queda estado cambiado ni
  movimiento persistido (coordinar `@Transactional` con D).

**Tests (Mockito):**

1. Ingreso de un colchón `PRODUCIDO` → `EN_DEPOSITO` y movimiento guardado.
2. Reserva minorista de 1 unidad → `RESERVADO`.
3. Reserva mayorista de 4 unidades → excepción y estado sin cambios.
4. Despacho de un `EN_DEPOSITO` → excepción (solo desde `RESERVADO`).
5. Cancelación → vuelve a `EN_DEPOSITO`.
6. Se notifica a los dos observadores una sola vez por movimiento.
7. Si el repositorio falla, no se notifica (orden transaccional).

---

### A5 — `FachadaEscaneo` (Sprint 2, 1 día)

- Implementa los 4 métodos del contrato y delega en `GestorDeStock`.
- **Sin reglas de negocio**: solo arma la solicitud (fecha/hora si viene null,
  embalajeOk por defecto) y devuelve `RespuestaEscaneoDTO`.
- Es la única clase que la capa de presentación puede usar del dominio.

**Tests:** verificar delegación y el DTO de respuesta; que no haya lógica
duplicada.

---

### A6 — Integración y wiring (Sprint 3, 2 días)

- Configurar beans que falten (`FabricaMovimiento`, `SelectorEstrategia`,
  observadores, puertos) sin invadir los paquetes de B, C y D.
- Correr los 8 escenarios de aceptación del plan maestro.
- Revisar PRs de dominio y cerrar deuda técnica (nombres, Javadoc, excepciones).
- Ajustar lo que aparezca en la integración con MySQL.

---

### A7 — Informe y diagramas (Sprint 3, 1 día)

- Diagrama de clases final (con los paquetes por capa).
- Diagrama de secuencia del flujo de escaneo (apartado 5 del TP).
- Sección de arquitectura en capas: por qué monolito, por qué tres capas.
- Sección de State, Composite, Singleton y Facade: problema, solución,
  integración y prueba.

---

### A8 — Demo (Sprint 4, 1 día)

- Guion técnico: qué se muestra, en qué orden, quién habla.
- Tag `v1.0` y verificación de que `main` levanta con `dev` y `devfile`.
- Plan B si falla MySQL: demo con `devfile` y aclarar que el esquema es el
  mismo (`db/01-schema.sql`).

---

## Qué NO hacés

- No implementás reservas por canal (C) ni validaciones de movimiento (B).
- No escribís entidades JPA ni mappers (D).
- No escribís controllers, adapters ni Postman (E).
- No cambiás `pom.xml` por pedidos ajenos sin revisar el impacto.

---

## Cómo probar tu parte sin el resto

- A2 y A3: tests puros de Java, sin Spring.
- A4: Mockito para puertos, estrategia, fábrica y observadores.
- A5: `GestorDeStock` mockeado.
- Solo en A6 necesitás las implementaciones reales de B, C, D y E.

---

## Checklist de entrega

- [ ] Contratos mergeados antes del Sprint 1.
- [ ] 16 tests de State + tests de Composite.
- [ ] `GestorDeStock` con 7 tests de flujo.
- [ ] `FachadaEscaneo` sin reglas propias.
- [ ] 8 escenarios de aceptación funcionando en H2.
- [ ] Diagramas y secciones del informe.
- [ ] Demo ensayada y tag `v1.0`.
