# Rol D — Persistencia y base de datos

**Carga:** ~16 % · **Tecnologías:** Spring Data JPA, Hibernate, H2
**Paquetes:** `persistence/entity`, `persistence/repository`, `persistence/mapper`

---

## Mensaje para el grupo

> Me encargo de que todo se guarde bien: entidades JPA, repositorios, mappers y
> datos de demo. Implemento los puertos que define A (`RepositorioItems`,
> `RepositorioMovimientos`). No escribo lógica de negocio.

---

## Idea del módulo

El dominio no conoce JPA: trabaja contra dos puertos. Vos los implementás y
mapeás entre el modelo rico (`Colchon`, `Lote`, `Movimiento`) y las tablas.

```
dominio ──> RepositorioItems / RepositorioMovimientos   (interfaces)
                        ▲
                        │ implementa
persistencia ──> JpaRepositorioItems / JpaRepositorioMovimientos
                        │
                        └──> ItemInventarioRepository (Spring Data)
                             ItemInventarioEntity + MovimientoEntity
```

La base es **H2** y el esquema lo genera Hibernate (`ddl-auto=update`). No hay
que escribir SQL a mano ni levantar ningún motor.

---

## Tareas

### D1 — Entidades, repositorios y mappers (S0–S1)

- `ItemInventarioEntity` (ajustar la existente): `id`, `codigo` único, `tipo`,
  `estado`, `cantidad`, `fechaProduccion`, `fechaAlta` y auto-referencia
  `lote` (`@ManyToOne` a sí misma) para los hijos de un lote.
- `MovimientoEntity`: `id`, `codigoItem`, `tipo`, `canal`, `usuario`,
  `fechaHora`, `estadoAnterior`, `estadoNuevo`, `cantidad`.
- `ItemInventarioRepository`, `MovimientoRepository` (Spring Data).
- `ItemInventarioMapper`, `MovimientoMapper`: entidad ↔ dominio.

Enums con `@Enumerated(EnumType.STRING)`.

**Criterio:** `@DataJpaTest` verde contra H2: guardar y recuperar un colchón,
código duplicado falla, guardar un lote con hijos y recuperar la jerarquía.

### D2 — Puertos y consultas (S1–S2)

- `JpaRepositorioItems implements RepositorioItems`.
- `JpaRepositorioMovimientos implements RepositorioMovimientos`.

Consultas necesarias:

- `findByCodigo`
- `findByEstado`
- `findTopByEstadoOrderByFechaProduccionAsc` (FIFO)
- `findByEstadoOrderByFechaProduccionAsc`
- `findAllByOrderByFechaHoraDesc(Pageable)` (auditoría)

Cuidar que `stockDisponible()` sume solo `EN_DEPOSITO` y evitar
`LazyInitializationException` al cargar los hijos del lote (usar `join fetch` o
`EAGER` justificado).

**Tests:** `buscarMasAntiguoEnDeposito` respeta la fecha; auditoría ordenada
descendente; el stock suma solo lo disponible.

### D3 — Datos de demo y ajustes (S2)

- Datos coherentes para la demo: 2 colchones, 1 lote de 5, un ítem reservado.
- Acordar con A dónde va `@Transactional` (recomendado: `GestorDeStock`).
- Probar con A y C el flujo completo sobre el perfil `devfile` y confirmar que
  los datos sobreviven al reinicio.

**Criterio:** la app arranca limpia con `dev` y con `devfile`, sin duplicar
datos.

### D4 — Cierre (S3)

- Diagrama entidad-relación y justificación del modelo relacional.
- Sección del informe: mapeo objeto-relacional y transacciones.
- Demo: mostrar las tablas en la consola H2 antes y después de un despacho.

---

## Qué NO hacés

- No escribís reglas de negocio ni transiciones de estado.
- No escribís controllers ni DTOs HTTP.
- No cambiás los puertos de A sin PR de contrato.
- No agregás Docker, MySQL ni scripts de esquema: H2 lo genera.

---

## Decisiones para cerrar con A

| Tema | Recomendación |
|---|---|
| Representación del lote | auto-referencia `lote_id` en la misma tabla |
| FK del movimiento | `codigoItem` (desnormalizado, sirve para auditoría) |
| Transacción | en `GestorDeStock` (un caso de uso = una transacción) |
| Borrado | no borrar movimientos; los ítems cambian de estado |

---

## Checklist

- [ ] Entidades y repositorios con tests `@DataJpaTest`.
- [ ] Mappers y puertos implementados.
- [ ] Consulta FIFO funcionando.
- [ ] Datos de demo y reinicio con `devfile` verificado.
- [ ] Diagrama ER y sección del informe.
- [ ] Demo de la base para el oral.
