# Plan de trabajo — TPO SMD Stock

**Materia:** Proceso de Desarrollo de Software · **Grupo:** 1
**Proyecto:** control de stock con escaneo QR — SMD Colchón Modular S.A.
**Stack:** Spring Boot + JPA + H2 · **Integrantes:** 5

> Este documento es el plan maestro. Los planes individuales están en
> `docs/equipo/` y se pueden reenviar tal cual.

---

## 1. Alcance

El TP es una **simulación**. Se evalúa el **diseño orientado a objetos**: que
los patrones estén bien aplicados, justificados y probados. No se evalúa
infraestructura.

**Entra:**

- Monolito Spring Boot en tres capas (`presentation`, `domain`, `persistence`).
- Base relacional **H2** (en memoria y en archivo). Sin instalar nada.
- Los 9 patrones del informe: State, Composite, Singleton, Facade, Decorator,
  Simple Factory, Strategy, Observer y Adapter.
- API REST mínima, tests unitarios y presentación de punta a punta.

**No entra** (si el docente lo pide, se evalúa como extra al final):

- Docker, MySQL, despliegue, CI/CD.
- Frontend o app móvil real: el Adapter se prueba con un lector simulado.
- Login, roles, microservicios, colas, mensajería.
- OpenAPI/Swagger: no se usa. La API se prueba con la colección de Postman
  versionada en `docs/http/`.

---

## 2. Reparto

Cada persona tiene **al menos un patrón** (o una responsabilidad técnica
equivalente, como D con Repository/Mapper) y paquetes propios. El criterio es
que cada uno pueda trabajar y probar su parte sin esperar al resto.

| Rol | Alcance | Patrones | Carga |
|---|---|---|---|
| **A** | Modelo, núcleo de dominio, integración | **State, Composite, Singleton** | ~27 % |
| **B** | Registro de movimientos y validaciones | Decorator, Simple Factory | ~20 % |
| **C** | Reservas por canal y notificaciones | Strategy, Observer | ~18 % |
| **D** | Persistencia y base de datos | Repository, Mapper | ~16 % |
| **E** | API REST, fachada, adapters y pruebas | **Facade, Adapter** | ~19 % |

| Rol | Integrante |
|---|---|
| A | *(vos)* |
| B | |
| C | |
| D | |
| E | |

---

## 3. Etapas

| Etapa | Duración | Meta | Hito verificable |
|---|---|---|---|
| **S0 — Contratos** | 2–3 días | Interfaces, DTOs y excepciones congelados | `main` compila con los contratos |
| **S1 — Patrones base** | 1 semana | Cada módulo con su caso feliz y un test | `mvnw test` verde por módulo |
| **S2 — Integración** | 1 semana | Todo cableado y funcionando en H2 | Los 8 escenarios de la presentación pasan |
| **S3 — Cierre** | 2–3 días | Informe, diagramas y ensayo | Tag `v1.0` y presentación ensayada |

Reunión corta los lunes (30 min) y merge a `main` los viernes con presentación de 5 min.

---

## 4. Tareas

### S0 — Contratos

| ID | Dueño | Tarea | Entregable |
|---|---|---|---|
| A1 | A | Interfaces, DTOs y excepciones (ver `CONTRATOS.md`) | PR `feat/contratos` mergeado |
| B1 | B | Casos de aceptación de movimientos y validaciones | `docs/equipo/casos-B.md` |
| C1 | C | Casos de aceptación de reservas y notificaciones | `docs/equipo/casos-C.md` |
| D1 | D | Entidades JPA, repositorios y mappers base | PR `feat/persistencia-base` |
| E1 | E | DTOs HTTP, esqueleto de controllers y `.http` | PR `feat/api-base` |

**Salida:** los contratos están en `main` y cada uno sabe qué archivos son suyos.

### S1 — Patrones base

| ID | Dueño | Tarea | Criterio de aceptación |
|---|---|---|---|
| A2 | A | Composite: `Colchon` y `Lote` | Lote de 5 devuelve 5 y cambia los 5 estados |
| A3 | A | State: 4 estados y matriz de transiciones | 16 combinaciones válidas e inválidas |
| B2 | B | `RegistroMovimientoBase`, `DecoradorRegistro`, `FabricaMovimiento` | Cada tipo devuelve una cadena armada |
| C2 | C | `EstrategiaReserva`, minorista, mayorista y selector | Rubín con 4 rechaza, con 5 acepta |
| D2 | D | Puertos implementados + consultas FIFO | `@DataJpaTest` verde en H2 |
| E2 | E | Controllers reales + `@RestControllerAdvice` | MockMvc verde con fachada mockeada |

### S2 — Integración

| ID | Dueño | Tarea | Criterio de aceptación |
|---|---|---|---|
| A4 | A | `GestorDeStock` (Singleton) orquesta el flujo | Tests de ingreso/reserva/despacho/cancelación |
| A5 | A | Wiring de beans e integración en H2 | La API funciona end-to-end |
| B3 | B | `ValidacionEmbalaje`, `ValidacionFIFO`, `AlertaStockBajo` | Embalaje dañado y despacho no FIFO rechazan |
| C3 | C | Observadores y `ServicioConsultas` | Una transición notifica a los dos observadores |
| D3 | D | Datos de presentación y ajustes de mapeo | La base persiste y se reinicia sin errores |
| E3 | E | Adapter de lectores | Los tres lectores devuelven el mismo tipo |
| E4 | E | `FachadaEscaneo`: única puerta de entrada | Delega en el gestor y devuelve el DTO |

### S3 — Cierre

| ID | Dueño | Tarea |
|---|---|---|
| A6 | A | Integración final, diagramas y sección de arquitectura |
| B4 | B | Tests de combinación de decoradores + sección del informe |
| C4 | C | Sección de Strategy/Observer + guion de presentación de reservas |
| D4 | D | Diagrama entidad-relación + sección de persistencia |
| E5 | E | Suite e2e de los 8 escenarios + README de la API |

---

## 5. Reglas de trabajo

### Propiedad de archivos

| Área | Paquetes | Dueño |
|---|---|---|
| Modelo, State, Composite, Gestor, puertos, excepciones | `domain/model`, `domain/dto`, `domain/state`, `domain/composite`, `domain/service` (Gestor), `domain/port`, `domain/exception` | A |
| Decorator y Factory | `domain/decorator`, `domain/factory` | B |
| Strategy, Observer, consultas | `domain/strategy`, `domain/observer`, `ServicioConsultas` | C |
| Persistencia | `persistence/**` | D |
| Fachada y presentación | `domain/facade`, `presentation/**`, `README.md`, `docs/http/**` | E |
| `pom.xml` | Solo A | A |

**No se toca un archivo de otro sin avisar en el grupo.** Si hace falta, se pide
y se coordina.

### Git

- `main` siempre compila y con tests verdes.
- Una rama por tarea: `feat/state`, `feat/decorator`, `feat/api`…
- Un PR = una tarea. Título `A3: implementar State`.
- Lo revisa alguien distinto del autor y se mergea con squash.
- Nada se mergea sin `.\mvnw.cmd test` verde.

### Comunicación

- Tablero con: *Pendiente / En curso / En revisión / Hecho*.
- Bloqueado más de 2 horas → avisar en el grupo.
- Reporte semanal de 5 líneas: hecho, en curso, bloqueado, PRs, riesgos.

---

## 6. Escenarios de la presentación

Son el criterio de “el TP funciona”.

1. **Ingreso**: escanear `COLCHON-QR-0001` en `PRODUCIDO` → `EN_DEPOSITO` y queda el movimiento.
2. **Reserva minorista** (dreamö): 1 colchón `EN_DEPOSITO` → `RESERVADO`.
3. **Reserva mayorista** (Rubín) con 4 unidades → rechazada.
4. **Reserva mayorista** con 5 unidades → aceptada.
5. **Cancelación**: un `RESERVADO` vuelve a `EN_DEPOSITO`.
6. **Despacho FIFO**: se despacha el más antiguo; uno posterior es rechazado.
7. **Composite**: despachar un lote de 5 deja los 5 en `DESPACHADO` con un escaneo.
8. **Persistencia**: reiniciar con `devfile` y los estados y la auditoría siguen ahí.

---

## 7. Definition of Done

- [ ] `.\\mvnw.cmd test` verde.
- [ ] Test del caso feliz + al menos un caso de error.
- [ ] Javadoc en clases y métodos públicos.
- [ ] Sin `TODO` sueltos (si queda algo, va como issue).
- [ ] PR revisado y mergeado a `main`.
- [ ] Funciona con `dev` y no rompe `devfile`.
- [ ] Un párrafo para el informe: qué patrón, por qué y cómo se probó.

---

## 8. Riesgos

| Riesgo | Mitigación |
|---|---|
| A se vuelve cuello de botella | Contratos cerrados en S0 antes de que el resto arranque |
| Cambios de interfaz a mitad de sprint | PR `contrato/*` + revisión de A y del dueño afectado |
| Integración tardía | Merge semanal + presentación de 5 min todos los viernes |
| Alguien no sabe explicar su parte en el oral | Cada dueño documenta y expone su patrón en la presentación |
| El alcance se infla | Todo lo que no esté en la sección 1 va al backlog |

---

## 9. Archivos para repartir

| Archivo | Para quién |
|---|---|
| `docs/equipo/CONTRATOS.md` | Todos (leer antes de codificar) |
| `docs/equipo/ROL-A-nucleo-dominio.md` | A |
| `docs/equipo/ROL-B-decorator-factory.md` | B |
| `docs/equipo/ROL-C-strategy-observer.md` | C |
| `docs/equipo/ROL-D-persistencia.md` | D |
| `docs/equipo/ROL-E-presentacion-pruebas.md` | E |
