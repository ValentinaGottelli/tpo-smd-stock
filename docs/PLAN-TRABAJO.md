# Plan de trabajo — TPO SMD Stock

**Materia:** Proceso de Desarrollo de Software
**Grupo:** 01
**Proyecto:** control de stock con escaneo QR — SMD Colchón Modular S.A.
**Stack:** Spring Boot 3.5 + JPA/Hibernate + H2/MySQL
**Integrantes:** 5

> Este documento es el plan maestro. Los planes individuales, listos para
> reenviar a cada compañero, están en `docs/equipo/`.
>
> Las duraciones son en **días de trabajo efectivo** (4–6 h). Ajustar las
> semanas a la fecha real de entrega: si quedan menos semanas, recortar el
> Sprint 4 (buffer) primero y el alcance de la demo después, nunca los
> contratos ni los tests.

---

## 1. Objetivo del reparto

Llegar a una demo que recorra el flujo completo del TP:

```
escaneo → validación → cambio de estado → notificación → persistencia
```

Con **5 personas trabajando en paralelo sin pisarse**, cada una dueña de un
conjunto de paquetes y con criterios de aceptación claros.

El reparto es por **módulos verticales del dominio**, no por “capas” sueltas:
cada persona entrega algo que se puede probar solo, y la integración se hace
semana a semana.

---

## 2. Criterio del reparto

Vos tomás el **núcleo de dominio**: modelo, State, Composite, GestorDeStock,
Facade y la integración técnica. Es la parte más extensa y la que más depende
de decisiones de diseño, por eso queda en una sola persona (evita idas y
vueltas de contratos).

El resto se reparte para que cada uno tenga **al menos un patrón del TP** y una
responsabilidad técnica clara:

| Rol | Alcance | Patrones / entregables | Carga | Peso |
|---|---|---|---|---|
| **A — vos** | Modelo + núcleo de dominio + integración | State, Composite, Singleton, Facade, modelo, contratos | 12 d | ~30 % |
| **B** | Movimientos y validaciones | Decorator, Simple Factory | 8 d | ~20 % |
| **C** | Reservas y notificaciones | Strategy, Observer | 7 d | ~17 % |
| **D** | Persistencia y base de datos | JPA, mappers, esquema MySQL, docker | 7 d | ~17 % |
| **E** | API REST y pruebas | DTOs, Adapter, controllers, OpenAPI, e2e | 7 d | ~15 % |

Completar la columna de nombres al repartir:

| Rol | Integrante |
|---|---|
| A | *(vos)* |
| B | |
| C | |
| D | |
| E | |

---

## 3. Dependencias entre roles

```
                    ┌────────────────────────────┐
                    │ SPRINT 0: CONTRATOS (A)    │
                    │ interfaces + DTOs + errores│
                    └─────────────┬──────────────┘
                                  │
        ┌─────────────┬───────────┼─────────────┬─────────────┐
        ▼             ▼           ▼             ▼             ▼
   B: Decorator  C: Strategy   D: Puerto     E: API       A: Modelo
      + Factory     + Observer    JPA           REST         + State
        │             │           │             │          + Composite
        └─────────────┴─────┬─────┴─────────────┘             │
                            ▼                                ▼
                  ┌──────────────────────────────────────────────┐
                  │ A: GestorDeStock + FachadaEscaneo + wiring   │
                  └───────────────────┬──────────────────────────┘
                                      ▼
                         Integración end-to-end + demo
```

**Regla de oro:** en el Sprint 0 se congelan las firmas de las interfaces. Si
alguien necesita cambiarlas después, abre un PR `contrato/<tema>`, avisa en el
grupo y lo revisa A junto con el dueño afectado. Sin esto, el paralelismo se
rompe en la semana 2.

---

## 4. Cronograma

| Sprint | Duración | Meta | Hito verificable |
|---|---|---|---|
| **S0** | 3 días | Contratos, git, esquema, esqueleto REST | `main` compila con interfaces y DTOs |
| **S1** | 1 semana | Cada módulo base con tests unitarios | `mvnw test` verde por módulo |
| **S2** | 1 semana | Patrones completos y cableados | Flujo completo en H2 por consola/servicio |
| **S3** | 1 semana | Integración, MySQL, e2e, informe | Demo end-to-end + esquema validado |
| **S4** | 3–4 días | Buffer, ensayo y presentación | Tag `v1.0` y guion de demo |

Cada semana: **una reunión de 30 min** (lunes) para revisar tablero y **un
merge a `main` el viernes** con demo de 5 min de lo hecho.

---

## 5. Tareas por sprint

### Sprint 0 — Contratos y puesta en marcha

| ID | Dueño | Tarea | Entregable |
|---|---|---|---|
| A1 | A | Congelar interfaces y excepciones (ver `CONTRATOS.md`) | PR `feat/contratos-dominio` mergeado |
| D1 | D | Revisar/ampliar esquema: `item_inventario` (auto-referencia lote), `movimiento`, índices | `db/01-schema.sql` + `db/02-data.sql` |
| E1 | E | DTOs + esqueleto de `EscaneoController` + colección Postman | PR `feat/api-base` |
| B1 | B | Casos de aceptación de movimientos y validaciones (documento) | `docs/equipo/casos-B.md` |
| C1 | C | Casos de aceptación de reservas y notificaciones | `docs/equipo/casos-C.md` |
| Todos | — | `git init`, rama `main`, tablero con estas tareas, acordar horarios | Repo + tablero |

**Salida del S0:** `main` compila, los contratos están mergeados y cada uno
sabe qué archivos son suyos.

---

### Sprint 1 — Base de cada módulo

| ID | Dueño | Tarea | Criterio de aceptación |
|---|---|---|---|
| A2 | A | `Colchon` y `Lote` (Composite): cantidad, fecha de producción, cambio de estado en cascada | Test: lote de 5 colchones devuelve 5 y cambia los 5 estados |
| A3 | A | State: 4 estados y matriz de transiciones | Test: 16 combinaciones, válidas e inválidas |
| B2 | B | `RegistroMovimientoBase` + `DecoradorRegistro` + `FabricaMovimiento` (sin validaciones) | Test: cada `TipoMovimiento` devuelve una cadena no nula |
| C2 | C | `EstrategiaReserva`, `ReservaMinorista`, `ReservaMayorista`, selector por canal | Test: Rubín con 4 rechaza, con 5 acepta; dreamö con 1 acepta |
| D2 | D | Entidades `ItemInventarioEntity` y `MovimientoEntity`, repositorios, mappers, implementación de puertos | `@DataJpaTest` verde contra H2 |
| E2 | E | `EscaneoController` (4 endpoints) + validación + `@RestControllerAdvice` | MockMvc verde con fachada mockeada |

---

### Sprint 2 — Patrones completos

| ID | Dueño | Tarea | Criterio de aceptación |
|---|---|---|---|
| A4 | A | `GestorDeStock` (Singleton): orquesta estado, estrategia, fábrica y observadores; persiste | Tests con fakes: flujo ingreso/reserva/despacho/cancelación |
| A5 | A | `FachadaEscaneo`: única puerta de entrada, sin reglas propias | Test: delega y devuelve `RespuestaEscaneoDTO` |
| B3 | B | `ValidacionEmbalaje`, `ValidacionFIFO`, `AlertaStockBajo` + armado de cadenas en la fábrica | Tests: embalaje dañado rechaza; despacho no FIFO rechaza; alerta al bajar del umbral |
| C3 | C | `ObservadorStock`, `ModuloVentas`, `DashboardAuditoria` y suscripción en `GestorDeStock` | Test: una transición notifica a ambos observadores |
| D3 | D | Transacciones, bloqueo optimista, consulta FIFO, consulta de auditoría | Tests de persistencia y de orden por fecha |
| E3 | E | Adapter: `LectorCodigo`, `AdaptadorCamara`, `AdaptadorPistolaUSB`, lector simulado | Test: los tres lectores devuelven el mismo tipo de resultado |

---

### Sprint 3 — Integración, MySQL e informe

| ID | Dueño | Tarea | Criterio de aceptación |
|---|---|---|---|
| A6 | A | Integración final, wiring de beans, revisión de PRs de dominio | Los 8 escenarios de la sección 7 pasan en H2 |
| A7 | A | Diagramas de clases y secuencia + sección de arquitectura del informe | Diagramas exportados en `docs/diagramas/` |
| B4 | B | Tests de combinaciones de decoradores + sección del informe | Cobertura de los 3 decoradores y sus combinaciones |
| C4 | C | Auditoría persistida + consulta de stock disponible + sección del informe | Reiniciar la app y la auditoría sigue estando |
| D4 | D | Validar `ddl-auto=validate` contra MySQL, índices y datos de demo | App levanta con perfil `mysql` sin errores |
| E4 | E | OpenAPI + e2e MockMvc + Postman contra MySQL | Colección ejecutable de punta a punta |

---

### Sprint 4 — Cierre

| ID | Dueño | Tarea |
|---|---|---|
| A8 | A | Guion técnico de la demo y tag `v1.0` |
| B5 | B | Repaso de validaciones para preguntas del docente |
| C5 | C | Repaso de reservas y observadores |
| D5 | D | Demo de MySQL y diagrama entidad-relación |
| E5 | E | Demo de API/Postman y README final |

---

## 6. Reglas de trabajo

### Archivos y propiedad

| Área | Paquetes / archivos | Dueño |
|---|---|---|
| Modelo, State, Composite, Gestor, Facade, excepciones | `domain/model`, `domain/state`, `domain/composite`, `domain/service` (solo `GestorDeStock`), `domain/facade`, `domain/exception`, `domain/port` | A |
| Consultas de lectura | `domain/service/ServicioConsultas.java` | C |
| Decorator y Factory | `domain/decorator`, `domain/factory` | B |
| Strategy y Observer | `domain/strategy`, `domain/observer` | C |
| Persistencia | `persistence/**`, `src/main/resources/db/**`, `docker-compose.yml` | D |
| Presentación | `presentation/**`, `README.md`, `docs/http/**` | E |
| `pom.xml` | Solo A lo modifica (pedidos por PR/issue) | A |

> Regla: **no se toca un archivo de otro sin avisar en el grupo**. Si hace
> falta, se pide en el chat y se coordina para no generar conflictos.

### Git

- `main` siempre compila y con tests verdes.
- Ramas: `feat/state`, `feat/decorator`, `feat/persistencia`, `feat/api`…
- Un PR = una tarea del plan. Título `A3: implementar State`.
- Cada PR lo revisa **una persona distinta del autor** (rotativo).
- Merge con squash, mensaje `feat(dominio): implementar State`.
- No se mergea nada sin `.\mvnw.cmd test` verde.

### Comunicación

- Tablero (GitHub Projects o Trello) con las columnas: *Pendiente / En curso / En revisión / Hecho*.
- Si alguien está bloqueado **más de 2 horas**, avisa en el grupo; no se queda trabado.
- Reporte semanal corto (plantilla en la sección 10).

---

## 7. Escenarios de aceptación (guion de demo)

Estos 8 casos son el criterio de “el TP funciona”. Se prueban en H2 y en MySQL.

1. **Ingreso**: escanear `COLCHON-QR-0001` en estado `PRODUCIDO` → pasa a `EN_DEPOSITO` y queda un movimiento de ingreso con usuario y fecha.
2. **Reserva minorista** (dreamö): 1 colchón `EN_DEPOSITO` → `RESERVADO`.
3. **Reserva mayorista** (Rubín) con 4 unidades → rechazada con error de negocio.
4. **Reserva mayorista** con 5 unidades → aceptada.
5. **Cancelación**: un `RESERVADO` vuelve a `EN_DEPOSITO`.
6. **Despacho FIFO**: se despacha el lote más antiguo; un lote posterior es rechazado.
7. **Composite**: despachar un lote de 5 colchones deja los 5 en `DESPACHADO` con un solo escaneo.
8. **Persistencia + auditoría**: reiniciar la app (perfil `devfile` o MySQL) y los estados y la auditoría se conservan.

---

## 8. Definition of Done (por tarea)

- [ ] Compila: `.\mvnw.cmd -q test` sin errores.
- [ ] Tests unitarios del módulo (caso feliz + al menos un caso de error).
- [ ] Javadoc en clases y métodos públicos.
- [ ] Sin `TODO` sueltos: si queda algo, va como issue en el tablero.
- [ ] PR revisado y mergeado a `main`.
- [ ] Funciona con perfil `dev` y no rompe `devfile` ni `mysql`.
- [ ] Sección corta para el informe (qué patrón se usó, por qué y cómo se probó).

---

## 9. Riesgos y mitigación

| Riesgo | Impacto | Mitigación |
|---|---|---|
| A se vuelve cuello de botella | Alto | Contratos cerrados en S0; interfaces mergeadas antes de que B/C/E empiecen |
| Cambios de interfaz a mitad de sprint | Alto | PR `contrato/*` + revisión obligatoria de A y del dueño afectado |
| Integración tardía | Alto | Merge semanal a `main` + demo de 5 min todos los viernes |
| MySQL recién se prueba al final | Medio | D valida `ddl-auto=validate` en S3 temprano; E corre Postman contra MySQL en S3 |
| Choque de archivos | Medio | Tabla de propiedad de paquetes + PRs chicos |
| Alcance se infla (login, frontend, microservicios) | Medio | Fuera de alcance explícito (sección 11); todo lo extra va al backlog, no al TP |
| Nadie sabe explicar una parte en el oral | Alto | Cada dueño documenta y expone su patrón en la demo |

---

## 10. Plantilla de reporte semanal

```
## Semana N — <nombre>
Hecho:
- ...
En curso:
- ...
Bloqueado por:
- ...
PRs abiertos:
- ...
Riesgos:
- ...
```

---

## 11. Fuera de alcance

- Frontend / app móvil real y escaneo con cámara real (el Adapter se prueba con
  un lector simulado).
- Autenticación y roles (el usuario viaja en el DTO).
- Microservicios, colas, mensajería.
- Despliegue en producción.

Si el docente pide algo de esto, se evalúa como extra **después** de tener los
8 escenarios funcionando.

---

## 12. Archivos para repartir

| Archivo | Para quién |
|---|---|
| `docs/equipo/CONTRATOS.md` | Todos (leer antes de codificar) |
| `docs/equipo/ROL-A-nucleo-dominio.md` | Rol A (vos) |
| `docs/equipo/ROL-B-decorator-factory.md` | Rol B |
| `docs/equipo/ROL-C-strategy-observer.md` | Rol C |
| `docs/equipo/ROL-D-persistencia.md` | Rol D |
| `docs/equipo/ROL-E-presentacion-pruebas.md` | Rol E |

Cada plan individual es autocontenido: se puede copiar y mandar por WhatsApp o
subir al Drive del grupo sin depender de este documento.
