# Rol E — API REST, adapter y pruebas

**Carga:** ~16 % · **Patrones:** Adapter
**Paquetes:** `presentation/controller`, `presentation/dto`,
`presentation/adapter`, `src/test/.../presentation`, `docs/http`

---

## Mensaje para el grupo

> Me encargo de la cara visible del sistema: los endpoints REST, los DTOs de la
> API, el manejo de errores, los adapters de lectores QR (cámara y pistola) y
> las pruebas de punta a punta. También dejo el archivo `.http` y el README de
> la API para la demo.

---

## Idea del módulo

La app de escaneo no habla con el dominio directamente:

```
LectorCodigo (Adapter) → arma EscaneoDTO
    → FachadaEscaneo (Facade) → GestorDeStock → ...
```

La capa REST expone ese flujo por HTTP y traduce excepciones de negocio a
códigos claros.

---

## Tareas

### E1 — DTOs y esqueleto (S0)

- `EscaneoRequest` con Bean Validation: `@NotBlank codigo`, `@NotNull canal`,
  `@NotBlank usuario`, `Boolean embalajeOk`.
- `ItemInventarioResponse`, `MovimientoResponse`, `ErrorDTO`.
- Esqueleto de `EscaneoController` con los 4 POST y los GET de inventario y
  auditoría (pueden devolver `501` al principio).
- `docs/http/escaneos.http` con ejemplos de todos los endpoints.

**Criterio:** compila y el `.http` está versionado.

### E2 — Controllers y errores (S1)

- `EscaneoController`: mapea `EscaneoRequest → EscaneoDTO` y llama a
  `FachadaEscaneo`.
- `InventarioController` y `AuditoriaController`: usan `ServicioConsultas` (C).
- `@RestControllerAdvice` que traduce:

| Excepción | HTTP |
|---|---|
| `RecursoNoEncontradoException` | 404 |
| `TransicionInvalidaException` | 409 |
| `ReglaDeNegocioException` | 409 |
| `MethodArgumentNotValidException` | 400 con detalle por campo |
| cualquier otra | 500 sin stacktrace |

Respuesta uniforme:

```json
{
  "codigo": "TRANSICION_INVALIDA",
  "mensaje": "El ítem COLCHON-QR-0001 está en estado RESERVADO",
  "fechaHora": "2026-10-28T10:15:30",
  "path": "/api/escaneos/ingreso"
}
```

**Tests MockMvc (fachada mockeada):** POST válido → 200; body sin `codigo` →
400; código inexistente → 404; transición inválida → 409; estructura del JSON de
error.

### E3 — Adapter y e2e (S2)

- `LectorCodigo` con `String leer()`.
- `AdaptadorCamara` y `AdaptadorPistolaUSB` traducen dos APIs distintas
  (simuladas dentro de `presentation/adapter`).
- `LectorCodigoSimulado` para tests y demo.
- Endpoint de demo opcional: `POST /api/lectores/{tipo}/leer`.

**Tests:** los tres lectores devuelven `String`; agregar un lector nuevo no
obliga a tocar el resto; las clases simuladas de SDK no salen de los adapters.

Después, `@SpringBootTest` con la app completa recorriendo los 8 escenarios del
plan maestro en perfil `dev`.

**Criterio:** suite e2e verde.

### E4 — Cierre (S3)

- README de la API: cómo levantar, tabla de endpoints, ejemplos y códigos de
  error.
- Guion de demo con `.http`: ingreso, reserva minorista, mayorista rechazada,
  mayorista OK, despacho y auditoría.
- Repaso del patrón Adapter para el oral.

---

## Qué NO hacés

- No implementás reglas de negocio ni transiciones (A).
- No definís reservas ni observadores (C).
- No escribís entidades ni consultas JPA (D).
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

## Checklist

- [ ] DTOs con validación y esqueleto de API.
- [ ] 4 endpoints de escaneo + 3 de consulta.
- [ ] Manejo de errores uniforme con 5 tests MockMvc.
- [ ] Adapter de cámara y pistola + simulado, con tests.
- [ ] Suite e2e de los 8 escenarios.
- [ ] `.http` y README de API.
- [ ] Guion de demo de la capa REST.
