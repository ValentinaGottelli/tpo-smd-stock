# Rol E — API REST, adapters y pruebas end-to-end

**Carga estimada:** 7 días de trabajo efectivo (~17 % del total)
**Patrones a cargo:** Adapter (+ DTOs y validación)
**Paquetes:** `presentation/controller`, `presentation/dto`, `presentation/adapter`,
`src/test/.../presentation`, `docs/http`

---

## Mensaje para pasar al grupo

> Me encargo de la cara visible del sistema: los endpoints REST, los DTOs de la
> API, el manejo de errores, los adapters de lectores QR (cámara y pistola) y
> las pruebas de punta a punta. También dejo la colección Postman y el README de
> la API para la demo.

---

## Idea del módulo

La app de escaneo no habla con el dominio directamente:

```
AppEscaneo → LectorCodigo (Adapter) → arma EscaneoDTO
           → FachadaEscaneo (Facade) → GestorDeStock → ...
```

La capa REST es la que expone ese flujo por HTTP y traduce excepciones de
negocio a códigos HTTP claros.

---

## Orden de trabajo

```
E1 DTOs + esqueleto API ──> E2 controllers + errores
                                   │
                                   ├──> E3 Adapter de lectores
                                   ├──> E4 OpenAPI
                                   └──> E5 e2e + Postman + README
```

---

## Tareas

### E1 — DTOs y esqueleto (Sprint 0, 1 día)

- `presentation/dto/EscaneoRequest` con Bean Validation:
  `@NotBlank codigo`, `@NotNull canal`, `@NotBlank usuario`,
  `Boolean embalajeOk`, `LocalDateTime fechaHora`.
- `presentation/dto/ItemInventarioResponse`,
  `MovimientoResponse`, `ErrorDTO`.
- Esqueleto de `EscaneoController` con los 4 POST y los GET de inventario y
  auditoría, todos devolviendo `501 Not Implemented` o mockeado.
- `docs/http/escaneos.http` + colección Postman (`docs/http/SMD-Stock.postman_collection.json`).

**Criterio:** `mvnw test` compila; la colección Postman está versionada.

---

### E2 — Controllers, validación y errores (Sprint 1, 2 días)

- `EscaneoController`: mapea `EscaneoRequest → EscaneoDTO` y llama a
  `FachadaEscaneo`.
- `InventarioController` y `AuditoriaController`: usan `ServicioConsultas` (C).
- `@RestControllerAdvice` (`ManejadorErrores`) que traduce:

| Excepción | HTTP |
|---|---|
| `RecursoNoEncontradoException` | 404 |
| `TransicionInvalidaException` | 409 |
| `ReglaDeNegocioException` | 409 |
| `MethodArgumentNotValidException` | 400 con detalle por campo |
| cualquier otra | 500 sin filtrar stacktrace |

- Respuesta de error uniforme:

```json
{
  "codigo": "TRANSICION_INVALIDA",
  "mensaje": "El ítem COLCHON-QR-0001 está en estado RESERVADO",
  "fechaHora": "2026-10-28T10:15:30",
  "path": "/api/escaneos/ingreso"
}
```

**Tests MockMvc (fachada mockeada):**

1. POST válido → 200 con `RespuestaEscaneoDTO`.
2. Body sin `codigo` → 400 con detalle.
3. Código inexistente → 404.
4. Transición inválida → 409.
5. JSON de error con la estructura acordada.

---

### E3 — Adapter de lectores (Sprint 2, 1,5 días)

- `LectorCodigo` con `String leer()`.
- SDKs externos simulados: `CamaraCelularSDK` (devuelve bytes/decodifica) y
  `PistolaUSB` (se comporta como teclado).
- `AdaptadorCamara` y `AdaptadorPistolaUSB` implementan `LectorCodigo`
  traduciendo cada API.
- `LectorCodigoSimulado` para tests y demo (código configurable).
- Endpoint opcional de demo: `POST /api/lectores/{tipo}/leer` que devuelve el
  código leído; útil para mostrar el patrón sin hardware.

**Tests:**

- Los tres lectores devuelven `String`.
- Un lector nuevo se agrega sin tocar el resto (demostrar con
  `LectorCodigoSimulado`).
- La aplicación no importa las clases de los SDK fuera de los adaptadores.

---

### E4 — OpenAPI (Sprint 2, 1 día)

- Agregar `springdoc-openapi-starter-webmvc-ui` (pedir el cambio de `pom.xml` a A).
- Swagger UI en `/swagger-ui.html` con descripción de los 4 escaneos.
- Documentar códigos 200/400/404/409.

**Criterio:** Swagger levanta y muestra todos los endpoints con ejemplos.

---

### E5 — e2e y Postman (Sprint 3, 2 días)

- Tests `@SpringBootTest` + `MockMvc` con la aplicación completa (perfil `dev`)
  recorriendo los 8 escenarios del plan maestro.
- Correr la colección Postman contra MySQL con datos de demo.
- Verificar que reiniciar la app conserva estados y auditoría (`devfile`).

**Criterio:** suite e2e verde y colección Postman ejecutable sin pasos manuales
(runner de Postman o `.http`).

---

### E6 — README y demo (Sprint 3–4, 1 día)

- README: cómo levantar en `dev`, `devfile` y `mysql`; tabla de endpoints;
  ejemplos de request/response; códigos de error.
- Guion de demo de la API (Postman): ingreso, reserva minorista, mayorista
  rechazada, mayorista OK, despacho, auditoría.
- Repaso del patrón Adapter para el oral.

---

## Qué NO hacés

- No implementás reglas de negocio ni transiciones (A).
- No definís reservas ni observadores (C).
- No escribís entidades ni SQL (D).
- No cambiás `domain/**` sin PR de contrato.

---

## Interfaces que consumís

| Interfaz | Dueño | Uso |
|---|---|---|
| `FachadaEscaneo` | A | 4 operaciones de escaneo |
| `EscaneoDTO`, `RespuestaEscaneoDTO` | A | entrada/salida del dominio |
| `ServicioConsultas` | C | GET inventario y auditoría |
| `ItemInventario`, `Movimiento` | A | respuestas |
| Excepciones de negocio | A | mapeo a HTTP |

---

## Checklist de entrega

- [ ] DTOs con validación y esqueleto de API.
- [ ] 4 endpoints de escaneo + 3 de consulta.
- [ ] Manejo de errores uniforme con 5 tests MockMvc.
- [ ] Adapter de cámara y pistola + simulado, con tests.
- [ ] Swagger UI funcionando.
- [ ] Suite e2e de los 8 escenarios.
- [ ] Colección Postman y README de API.
- [ ] Guion de demo de la capa REST.
