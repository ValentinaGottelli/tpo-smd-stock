# Rol E — API REST, fachada, adapter y pruebas

**Carga:** ~19 % · **Patrones:** Facade, Adapter
**Paquetes:** `domain/facade`, `presentation/controller`, `presentation/dto`,
`presentation/adapter`, `src/test/.../presentation`, `docs/http`

---

## Mensaje para el grupo

> Me encargo de la cara visible del sistema: los endpoints REST, los DTOs de la
> API, el manejo de errores, la fachada de escaneo que usan los controllers, los
> adapters de lectores QR y las pruebas de punta a punta. También dejo el archivo
> `.http` y el README de la API para la presentación.

---

## Idea del módulo

```
LectorCodigo (Adapter) → arma EscaneoDTO
    → FachadaEscaneo (Facade, mía) → GestorDeStock → ...
```

Tenés los dos patrones de borde: **Facade** simplifica el acceso al dominio para
la capa REST y **Adapter** traduce APIs de lectores distintas a una sola
interfaz. No inventan reglas de negocio: solo adaptan.

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

### E3 — Adapter (S2)

- `LectorCodigo` con `String leer()`.
- `AdaptadorCamara` y `AdaptadorPistolaUSB` traducen dos APIs distintas
  (simuladas dentro de `presentation/adapter`).
- `LectorCodigoSimulado` para tests y presentación.
- Endpoint opcional para la presentación: `POST /api/lectores/{tipo}/leer`.

**Tests:** los tres lectores devuelven `String`; agregar un lector nuevo no
obliga a tocar el resto; las clases simuladas de SDK no salen de los adapters.

### E4 — FachadaEscaneo (S2)

Implementás `FachadaEscaneo` sobre la interfaz que congeló A:

- Los 4 métodos delegan en `GestorDeStock`.
- **Sin reglas propias**: arma el `EscaneoDTO` (usuario por defecto si viene
  vacío) y devuelve `RespuestaEscaneoDTO`.
- Es la única clase del dominio que usan los controllers.

**Tests:** con `GestorDeStock` mockeado, cada método delega una vez y devuelve el
DTO; no hay lógica duplicada.

### E5 — e2e y cierre (S3)

- `@SpringBootTest` con la app completa recorriendo los 8 escenarios del plan
  maestro en perfil `dev`.
- README de la API: cómo levantar, tabla de endpoints, ejemplos y códigos de
  error.
- Guion de presentación con `.http`: ingreso, reserva minorista, mayorista rechazada,
  mayorista OK, despacho y auditoría.
- Repaso de Facade y Adapter para el oral.

**Criterio:** suite e2e verde y colección `.http` ejecutable sin pasos manuales.

---

## Qué NO hacés

- No implementás reglas de negocio ni transiciones (A).
- No definís reservas ni observadores (C).
- No escribís entidades ni consultas JPA (D).
- No cambiás `domain/model`, `domain/state` ni `domain/composite` (A).
- No implementás el `GestorDeStock`: la fachada lo consume, no lo reemplaza.

---

## Interfaces que consumís

| Interfaz | Dueño | Uso |
|---|---|---|
| `FachadaEscaneo` | interfaz de A, implementación tuya | 4 operaciones de escaneo |
| `GestorDeStock` | A | implementar la fachada |
| `EscaneoDTO`, `RespuestaEscaneoDTO` | A | entrada/salida del dominio |
| `ServicioConsultas` | C | GET inventario y auditoría |
| `ItemInventario`, `Movimiento` | A | respuestas |
| Excepciones de negocio | A | mapeo a HTTP |

---

## Checklist

- [ ] DTOs con validación y esqueleto de API.
- [ ] 4 endpoints de escaneo + 3 de consulta.
- [ ] Manejo de errores uniforme con 5 tests MockMvc.
- [ ] `FachadaEscaneo` implementada y probada con el gestor mockeado.
- [ ] Adapter de cámara y pistola + simulado, con tests.
- [ ] Suite e2e de los 8 escenarios.
- [ ] `.http` y README de API.
- [ ] Guion de presentación de la capa REST.
