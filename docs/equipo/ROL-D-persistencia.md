# Rol D — Persistencia y base de datos

**Carga estimada:** 7 días de trabajo efectivo (~17 % del total)
**Tecnologías:** Spring Data JPA, Hibernate, H2, MySQL 8, Docker
**Paquetes:** `persistence/entity`, `persistence/repository`, `persistence/mapper`,
`src/main/resources/db`, `docker-compose.yml`

---

## Mensaje para pasar al grupo

> Me encargo de que todo se guarde bien: entidades JPA, repositorios, mappers,
> esquema MySQL, docker y datos de demo. Implemento los puertos que define A
> (`RepositorioItems`, `RepositorioMovimientos`) y valido que el esquema real
> coincida con las entidades. No escribo lógica de negocio.

---

## Idea del módulo

El dominio no conoce JPA: trabaja contra dos puertos. Vos los implementás en la
capa de persistencia y hacés el mapeo entre el modelo rico (`Colchon`, `Lote`,
`Movimiento`) y las tablas.

```
dominio ──> RepositorioItems / RepositorioMovimientos   (interfaces)
                        ▲
                        │ implementa
persistencia ──> JpaRepositorioItems / JpaRepositorioMovimientos
                        │
                        └──> ItemInventarioRepository (Spring Data)
                             ItemInventarioEntity + MovimientoEntity
```

---

## Orden de trabajo

```
D1 esquema ──> D2 entidades + repos + mappers + puertos
                            │
                            ├──> D3 transacciones y consultas FIFO
                            ├──> D4 docker + MySQL validado
                            └──> D5/D6 índices, informe y demo
```

---

## Tareas

### D1 — Esquema y modelo relacional (Sprint 0, 1 día)

Tablas mínimas:

**`item_inventario`** (ya existe, ampliar si hace falta)

- `id`, `codigo` (único), `tipo`, `estado`, `cantidad`, `fecha_produccion`,
  `fecha_alta`, `version_registro`.
- Auto-referencia `lote_id` (FK a `item_inventario.id`, nullable) para
  representar los hijos de un lote. Un colchón tiene `lote_id` nulo o apunta a
  su lote.
- Índice `(estado, fecha_produccion)` para la consulta FIFO.

**`movimiento`**

- `id`, `codigo_item`, `tipo`, `canal` (nullable), `usuario`, `fecha_hora`,
  `estado_anterior`, `estado_nuevo`, `cantidad`.
- FK lógica por `codigo_item` (o `item_id`, definir y documentar).
- Índice `fecha_hora DESC` para auditoría.

Entregable: `src/main/resources/db/01-schema.sql` y `02-data.sql` actualizados
+ diagrama entidad-relación en `docs/diagramas/`.

**Criterio:** el esquema corre en MySQL 8 sin errores y `ddl-auto=validate`
levanta la app.

---

### D2 — Entidades, repositorios, mappers y puertos (Sprint 1, 2 días)

- `ItemInventarioEntity` (ajustar la existente con `lote_id`).
- `MovimientoEntity`.
- `ItemInventarioRepository`, `MovimientoRepository` (Spring Data).
- `ItemInventarioMapper`, `MovimientoMapper`: entidad ↔ dominio.
- `JpaRepositorioItems implements RepositorioItems`.
- `JpaRepositorioMovimientos implements RepositorioMovimientos`.

Consultas necesarias:

- `findByCodigo`.
- `findByEstadoOrderByFechaProduccionAsc` (FIFO).
- `findByEstado` (listados).
- `findTopByEstadoOrderByFechaProduccionAsc`.
- `findAllByOrderByFechaHoraDesc(Pageable)` (auditoría).

**Tests (`@DataJpaTest` con H2):**

1. Guardar y recuperar un colchón.
2. Código duplicado → error de constraint.
3. Guardar un lote con hijos y recuperarlo con la jerarquía.
4. `buscarMasAntiguoEnDeposito` respeta `fecha_produccion`.
5. Auditoría ordenada por fecha descendente.

---

### D3 — Transacciones, concurrencia y consultas (Sprint 2, 2 días)

- Definir con A dónde va `@Transactional` (recomendado: `GestorDeStock` o la
  fachada; la persistencia no abre transacciones por su cuenta).
- `@Version` en `ItemInventarioEntity` para bloqueo optimista; test de dos
  actualizaciones concurrentes.
- Mapear `Boolean`/enums correctamente (`@Enumerated(EnumType.STRING)`).
- Evitar `LazyInitializationException` en la carga de hijos del lote
  (`@OneToMany` con `fetch = EAGER` justificado o consulta con `join fetch`).
- Cuidar el stock: `stockDisponible()` debe sumar solo `EN_DEPOSITO`.

**Criterio:** tests de integración verdes; sin `SELECT N+1` evidente en el
listado de lotes.

---

### D4 — Docker y MySQL real (Sprint 2, 1 día)

- `docker-compose.yml` funcionando (`mysql:8.4`, scripts de `db/` como initdb).
- Verificar que el volumen se recrea (`docker compose down -v`) y los scripts
  corren de nuevo.
- Documentar credenciales y variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.

**Criterio:** `docker compose up -d` + perfil `mysql` levantan sin errores y
`/api/health` responde `"baseDeDatos": "MySQL"`.

---

### D5 — Índices y datos de demo (Sprint 3, 1,5 días)

- Índices finales según las consultas reales.
- Datos de demo coherentes: 2 colchones, 1 lote de 5, un ítem reservado.
- Script de reseteo para la demo (`docker compose down -v` + levantar).
- Probar el flujo completo contra MySQL con E.

---

### D6 — Informe y demo (Sprint 3–4, 1 día)

- Diagrama entidad-relación y justificación del modelo relacional.
- Sección sobre mapeo objeto-relacional, transacciones ACID y bloqueo optimista.
- Demo: mostrar las tablas con MySQL Workbench o consola, antes y después de un
  despacho.

---

## Qué NO hacés

- No escribís reglas de negocio ni transiciones de estado.
- No escribís controllers ni DTOs HTTP.
- No cambiás los puertos de A sin PR de contrato.
- No usás `ddl-auto=update` en MySQL: el esquema se versiona en SQL.

---

## Decisiones que tenés que cerrar con A

| Tema | Opciones | Recomendación |
|---|---|---|
| Representación de lote | `lote_id` auto-referencia vs tabla `lote_colchon` | `lote_id` (más simple, un colchón pertenece a un lote) |
| FK de movimiento | `item_id` vs `codigo_item` | `item_id` + índice, `codigo_item` desnormalizado para auditoría |
| Transacción | en Gestor o en fachada | en `GestorDeStock` (un caso de uso = una transacción) |
| Borrado | lógico vs físico | no borrar movimientos (auditoría); ítems dados de baja con estado |

---

## Checklist de entrega

- [ ] Esquema MySQL + diagrama ER.
- [ ] Entidades y repositorios con 5 tests `@DataJpaTest`.
- [ ] Puertos implementados y usados por A/B/C.
- [ ] Transacciones y bloqueo optimista probados.
- [ ] Docker + perfil `mysql` funcionando.
- [ ] Datos de demo y script de reseteo.
- [ ] Sección del informe y demo de base de datos.
