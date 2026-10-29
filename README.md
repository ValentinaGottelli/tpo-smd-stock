# SMD Stock

TPO de **Proceso de Desarrollo de Software** — sistema de control de stock con
escaneo de códigos QR para **SMD Colchón Modular S.A.**

Es una **simulación académica**: no se despliega, no hay usuarios reales y el
objetivo es mostrar **diseño orientado a objetos con patrones**. Por eso el
proyecto es un monolito Spring Boot chico, con una sola base **H2**, sin
frontend y sin infraestructura extra.

La entrega tiene el **núcleo del dominio y la API REST funcionando de punta a
punta**: se da de alta un colchón o un lote, se recorre su ciclo de vida por
HTTP, se consulta el inventario y queda la auditoría de cada movimiento.

---

## Stack

| Componente | Elección |
|---|---|
| Java | 17+ |
| Framework | Spring Boot 3.5 |
| Build | Maven Wrapper (no hace falta instalar Maven) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | **H2** en memoria (`dev`) o en archivo (`devfile`) |
| API | Spring Web (REST) + Bean Validation |

No se usa Docker, MySQL, OpenAPI, frontend, Lombok ni servicios externos.

---

## Cómo correrlo

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

Arranca en <http://localhost:8080> con el perfil `dev` (H2 en memoria). La base
se inicializa en cada arranque con 2 colchones sueltos y 1 lote de 5 colchones
(8 filas, 3 ítems raíz).

```bash
curl http://localhost:8080/api/health
```

```json
{
  "estado": "OK",
  "aplicacion": "smd-stock",
  "perfiles": "dev",
  "baseDeDatos": "H2 (memoria)",
  "itemsRegistrados": 3,
  "fechaHora": "2026-10-28T10:15:30.123"
}
```

Consola de H2: <http://localhost:8080/h2-console>
(driver `org.h2.Driver`, URL `jdbc:h2:mem:smd`, usuario `sa`, sin contraseña).

### Datos que sobreviven al reinicio

Con el perfil `devfile` la base se guarda en `./data/smd.mv.db`:

```powershell
$env:SPRING_PROFILES_ACTIVE = "devfile"
.\mvnw.cmd spring-boot:run
```

El seeder carga los datos iniciales **solo si la base está vacía**. Para
empezar de cero, detener la app y borrar `data/`.

---

## API REST

Todas las rutas devuelven JSON. La URL base local es
`http://localhost:8080/api`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| `GET` | `/health` | — | `HealthResponse` |
| `POST` | `/escaneos/ingreso` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| `POST` | `/escaneos/reserva` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| `POST` | `/escaneos/cancelacion` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| `POST` | `/escaneos/despacho` | `EscaneoRequest` | `RespuestaEscaneoDTO` |
| `GET` | `/inventario?estado=` | — | `List<ItemInventarioResponse>` |
| `GET` | `/inventario/{codigo}` | — | `ItemInventarioResponse` |
| `POST` | `/inventario/colchones` | `RegistrarColchonRequest` | `ItemInventarioResponse` (201) |
| `POST` | `/inventario/lotes` | `RegistrarLoteRequest` | `ItemInventarioResponse` (201) |
| `GET` | `/auditoria?limite=50` | — | `List<MovimientoResponse>` |

### Cuerpos de request

```json
// EscaneoRequest — usuario y canal son opcionales
{
  "codigo": "COLCHON-QR-0001",
  "canal": "DREAMO_LOCAL",
  "usuario": "operario01",
  "embalajeOk": true
}
```

```json
// RegistrarColchonRequest — estado y fecha son opcionales
{
  "codigo": "COLCHON-QR-9001",
  "estado": "PRODUCIDO",
  "fechaProduccion": "2026-10-01"
}
```

```json
// RegistrarLoteRequest — cantidad por defecto 5
{
  "codigo": "LOTE-QR-9001",
  "estado": "PRODUCIDO",
  "fechaProduccion": "2026-10-01",
  "cantidad": 5
}
```

- Si se omite `usuario`, la fachada usa `app.escaneo.usuario-default`
  (`operario01`).
- Si se omite `estado` en un alta, arranca en `PRODUCIDO`.
- Un alta solo admite `PRODUCIDO` o `EN_DEPOSITO`; cualquier otro estado
  devuelve `409`.

### Flujo completo

1. `POST /api/inventario/colchones` → crea el colchón en `PRODUCIDO`.
2. `POST /api/escaneos/ingreso` → `PRODUCIDO` a `EN_DEPOSITO`.
3. `POST /api/escaneos/reserva` → `EN_DEPOSITO` a `RESERVADO`.
4. `POST /api/escaneos/despacho` → `RESERVADO` a `DESPACHADO`.
5. `GET /api/inventario/{codigo}` → confirma el estado final.
6. `GET /api/auditoria` → muestra los tres movimientos.

Un lote sigue el mismo flujo con un solo escaneo: el patrón **Composite**
propaga el cambio de estado a sus 5 colchones hijos.

### Respuestas

```json
// RespuestaEscaneoDTO
{
  "codigo": "COLCHON-QR-9001",
  "estadoAnterior": "EN_DEPOSITO",
  "estadoNuevo": "RESERVADO",
  "cantidad": 1,
  "mensaje": "Reserva registrada para COLCHON-QR-9001",
  "fechaHora": "2026-10-28T10:20:00"
}
```

```json
// ItemInventarioResponse — los lotes incluyen el árbol de hijos
{
  "codigo": "LOTE-QR-9001",
  "tipo": "LOTE",
  "estado": "EN_DEPOSITO",
  "cantidad": 5,
  "fechaProduccion": "2026-10-01",
  "hijos": [
    { "codigo": "LOTE-QR-9001-01", "tipo": "COLCHON", "estado": "EN_DEPOSITO", "cantidad": 1, "hijos": [] }
  ]
}
```

### Errores

| Situación | HTTP | `codigo` |
|---|---|---|
| Transición inválida de State | `409` | `TRANSICION_INVALIDA` |
| Regla de negocio (código duplicado, estado inicial inválido) | `409` | `REGLA_DE_NEGOCIO` |
| Código inexistente | `404` | `RECURSO_NO_ENCONTRADO` |
| Body inválido o parámetro con formato incorrecto | `400` | `SOLICITUD_INVALIDA` |

```json
{
  "codigo": "TRANSICION_INVALIDA",
  "mensaje": "No se puede reservar el item COLCHON-QR-0001 porque esta en estado DESPACHADO",
  "fechaHora": "2026-10-28T10:25:00",
  "path": "/api/escaneos/reserva"
}
```

La traducción a HTTP está centralizada en
`presentation/exception/ManejadorGlobalDeErrores`.

### Postman / Apidog

La colección está en
[`docs/http/inventario.postman_collection.json`](docs/http/inventario.postman_collection.json).
Es formato Postman v2.1, así que **Apidog también la puede importar**. Tiene 16
requests numerados: preparación, health, alta y ciclo de un colchón, alta y
ciclo de un lote, consultas, auditoría y dos casos de error. El request `00`
genera códigos únicos para que se pueda correr varias veces.

1. Postman o Apidog → **Import** → elegir el `.json`.
2. Verificar `baseUrl` (por defecto `http://localhost:8080`).
3. Ejecutar en orden o usar **Run collection**.

---

## Arquitectura

El dominio es **Java puro**: no tiene anotaciones de Spring y no depende de HTTP.
Las dependencias apuntan hacia adentro: la infraestructura conoce al dominio,
nunca al revés.

```
presentation (controllers, DTOs HTTP, manejo de errores)
        |
        v
domain.facade (FachadaEscaneo)  <-- única puerta de entrada
        |
        v
domain.service (GestorDeStock, ServicioConsultas)
        |
        v
domain.port (RepositorioItems, RepositorioMovimientos)
        ^
        |  implementados por
persistence (JPA: adaptadores, entidades, mappers)
```

- Los **controllers** solo validan el body y delegan en la fachada o en los
  servicios.
- `GestorDeStock` ejecuta la transición con State, guarda el ítem y registra el
  movimiento.
- `ServicioConsultas` concentra las lecturas.
- Los **puertos** son interfaces del dominio; los adaptadores JPA están en
  `persistence` y traducen con los mappers.
- `config/DomainConfig` construye los servicios puros como beans únicos
  (Singleton) sin ensuciar el dominio con `@Service`.

---

## Estructura

```
Entrega3/
├── GUIA.md                            # guía para explicar el código en clase
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/            # Maven Wrapper
├── docs/
│   ├── PLAN-TRABAJO.md
│   ├── equipo/                        # contratos y planes por rol
│   └── http/                          # colección de Postman/Apidog
├── src/main/java/com/smd/inventario/
│   ├── InventarioApplication.java
│   ├── config/
│   │   ├── DomainConfig.java          # cablea los servicios del dominio
│   │   └── DevFileDataSeeder.java     # datos iniciales para devfile
│   ├── domain/
│   │   ├── model/                     # ItemInventario, base, Movimiento, enums
│   │   ├── dto/                       # EscaneoDTO, RespuestaEscaneoDTO
│   │   ├── state/                     # State: los 4 estados
│   │   ├── composite/                 # Composite: Colchon y Lote
│   │   ├── service/                   # GestorDeStock, ServicioConsultas
│   │   ├── facade/                    # FachadaEscaneo + implementación
│   │   ├── port/                      # puertos de persistencia
│   │   └── exception/                 # errores de negocio
│   ├── persistence/
│   │   ├── entity/                    # entidades JPA
│   │   ├── mapper/                    # entidad <-> dominio
│   │   └── repository/                # Spring Data + adaptadores de puertos
│   └── presentation/
│       ├── controller/                # health, escaneos, inventario, auditoría
│       ├── dto/                       # requests, responses y error
│       └── exception/                 # @RestControllerAdvice
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties     # H2 memoria
│   ├── application-devfile.properties # H2 archivo
│   └── data.sql                       # datos iniciales (dev)
└── data/                              # base H2 (generada, no se versiona)
```

---

## Patrones y ubicación

| Patrón | Paquete | Dueño | Estado |
|---|---|---|---|
| **State** | `domain/state` | A | Implementado |
| **Composite** | `domain/composite` | A | Implementado |
| **Singleton** | `domain/service` + `config/DomainConfig` | A | Implementado (beans únicos) |
| **Facade** | `domain/facade` | E | Implementado |
| **Adapter** | `persistence/repository` | E | Implementado (puertos a JPA) |
| **Repository** | `persistence/repository` | D | Implementado (Spring Data + puerto) |
| **Mapper** | `persistence/mapper` | D | Implementado |
| Decorator, Factory | `domain/decorator`, `domain/factory` | B | Pendiente |
| Strategy, Observer | `domain/strategy`, `domain/observer` | C | Pendiente |

> El contrato de los patrones pendientes ya está acordado en
> `docs/equipo/CONTRATOS.md`. No se crean paquetes vacíos para no agregar
> ruido al repositorio.

---

## Verificación

```powershell
.\mvnw.cmd clean package
```

El flujo completo se verificó contra la API real sobre H2: alta, ingreso,
reserva, despacho, cascada de un lote de 5, auditoría y respuestas 400, 404 y
409. La colección de Postman/Apidog permite repetir ese recorrido.

---

## Documentación del grupo

- [`GUIA.md`](GUIA.md) — recorrido archivo por archivo para explicar en clase
- [`docs/http/inventario.postman_collection.json`](docs/http/inventario.postman_collection.json) — colección para Postman/Apidog
- `docs/PLAN-TRABAJO.md` — reparto, sprints y reglas de trabajo
- `docs/equipo/CONTRATOS.md` — interfaces acordadas (**leer antes de codificar**)
- `docs/equipo/ROL-*.md` — plan individual de cada integrante

---

## Comandos

```bash
./mvnw clean package        # compila y genera el jar en target/
java -jar target/smd-stock-0.0.1-SNAPSHOT.jar
```
