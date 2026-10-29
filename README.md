# SMD Stock — Proyecto base

Esqueleto del sistema de **control de stock con escaneo de códigos QR** para
**SMD Colchón Modular S.A.** (TPO — Proceso de Desarrollo de Software, Grupo 1).

Esta entrega deja armado el proyecto Spring Boot con la **conexión a base de
datos relacional funcionando**, la separación en capas y un paquete por cada
patrón de diseño pedido en el informe. **No incluye la lógica de negocio**: las
clases de dominio y los patrones se implementan en las próximas iteraciones.

---

## Stack

| Componente | Versión / elección |
|---|---|
| Java | 17+ (probado con JDK 17 y 21) |
| Framework | Spring Boot 3.5.16 |
| Build | Maven (incluye wrapper, no hace falta instalar Maven) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos (dev) | H2 en memoria, en modo compatibilidad MySQL |
| Base de datos (dev persistente) | H2 en archivo (`./data/smd.mv.db`) |
| Base de datos (prod) | MySQL 8.4 (docker-compose incluido) |
| API | Spring Web (REST) + Bean Validation |

---

## Inicio rápido (sin instalar nada)

```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

La aplicación arranca en <http://localhost:8080> con el perfil `dev`
(H2 en memoria) y carga tres ítems de ejemplo.

Verificación de que la base responde:

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

Consola web de H2: <http://localhost:8080/h2-console>
(driver `org.h2.Driver`, URL `jdbc:h2:mem:smd`, usuario `sa`, sin contraseña).

> La base en memoria se borra al detener la aplicación. Si querés conservar los
> datos entre reinicios, usá el perfil `devfile` (siguiente sección).

---

## Persistir datos entre reinicios (H2 en archivo)

Con el perfil `devfile` los datos se guardan en `./data/smd.mv.db` y sobreviven
a los reinicios, sin instalar ningún motor de base de datos:

```powershell
# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE = "devfile"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
SPRING_PROFILES_ACTIVE=devfile ./mvnw spring-boot:run
```

- El esquema se sigue generando con Hibernate (`ddl-auto=update`).
- `DevFileDataSeeder` carga los 3 ítems de ejemplo **solo si la base está
  vacía**, así no duplica registros ni pisa cambios en cada arranque.
- La consola H2 sigue disponible en `/h2-console`, pero con la URL
  `jdbc:h2:file:./data/smd` (usuario `sa`, sin contraseña).
- Para empezar de cero: detener la app y borrar la carpeta `data/`.

---

## Ejecutar contra MySQL

1. Levantar el contenedor (crea la base, el usuario y ejecuta los scripts de
   `src/main/resources/db/`):

   ```bash
   docker compose up -d
   ```

2. Arrancar la aplicación con el perfil `mysql`:

   ```powershell
   # Windows (PowerShell)
   $env:SPRING_PROFILES_ACTIVE = "mysql"
   .\mvnw.cmd spring-boot:run
   ```

   ```bash
   # Linux / macOS
   SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
   ```

Credenciales por defecto (se pueden pisar con variables de entorno):

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/smd_stock` |
| `DB_USER` | `smd` |
| `DB_PASSWORD` | `smd` |

En este perfil Hibernate **no crea tablas** (`ddl-auto=validate`): el esquema se
versiona en `src/main/resources/db/01-schema.sql`. Si la base ya existía y no se
ejecutaron los scripts, aplicarlos a mano:

```bash
mysql -u root -p < src/main/resources/db/01-schema.sql
mysql -u root -p smd_stock < src/main/resources/db/02-data.sql
```

> Si se cambia el esquema, hay que recrear el volumen (`docker compose down -v`)
> porque los scripts de initdb solo corren con el volumen vacío.

---

## Estructura del proyecto

```
Entrega3/
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/          # Maven Wrapper
├── docker-compose.yml               # MySQL 8.4
├── src/main/java/com/smd/inventario/
│   ├── InventarioApplication.java   # main
│   ├── config/                      # configuración transversal + seeder devfile
│   ├── presentation/
│   │   ├── controller/              # REST (HealthController)
│   │   ├── dto/                     # objetos de transferencia
│   │   └── adapter/                 # Adapter de lectores QR
│   ├── domain/
│   │   ├── model/                   # Colchón, Lote, Movimiento, enums
│   │   ├── service/                 # GestorDeStock (Singleton)
│   │   ├── state/                   # State
│   │   ├── observer/                # Observer
│   │   ├── strategy/                # Strategy
│   │   ├── decorator/               # Decorator
│   │   ├── composite/               # Composite
│   │   ├── facade/                  # Facade
│   │   └── factory/                 # Simple Factory
│   └── persistence/
│       ├── entity/                  # ItemInventarioEntity
│       ├── repository/              # ItemInventarioRepository
│       └── mapper/                  # entity <-> dominio
├── src/main/resources/
│   ├── application.properties       # común
│   ├── application-dev.properties   # H2 en memoria
│   ├── application-devfile.properties # H2 en archivo
│   ├── application-mysql.properties # MySQL
│   ├── data.sql                     # datos de ejemplo (dev)
│   └── db/                          # esquema y datos para MySQL
├── data/                            # base H2 persistente (generada, no se versiona)
└── src/test/java/...                # test de contexto + health
```

---

## Correspondencia con el informe del TP

| Apartado del TP | Paquete |
|---|---|
| 1.1 Arquitectura en capas | `presentation`, `domain`, `persistence` |
| 1.2 Diagrama de arquitectura | `config` (beans y wiring) |
| 2. State (ciclo de vida) | `domain.state` + `domain.model.EstadoProducto` |
| 2. Observer (notificaciones) | `domain.observer` |
| 3. Strategy (reserva minorista/mayorista) | `domain.strategy` |
| 3. Decorator (validaciones) | `domain.decorator` |
| 4. Composite (colchón / lote) | `domain.composite` |
| 4. Adapter (cámara / pistola USB) | `presentation.adapter` |
| 4. Facade (FachadaEscaneo) | `domain.facade` |
| 5. Simple Factory (FabricaMovimiento) | `domain.factory` |
| 5. DTO | `presentation.dto` |
| 5. Singleton (GestorDeStock) | `domain.service` |
| Persistencia relacional | `persistence.entity`, `persistence.repository`, `persistence.mapper` |

Cada paquete tiene un `package-info.java` que documenta su responsabilidad.

---

## Decisiones de diseño del esqueleto

- **Monolito en capas** (no microservicios), tal como justifica el informe: un
  solo depósito y volumen bajo.
- **Perfiles de Spring** en lugar de cambiar archivos a mano: `dev` (H2 en
  memoria), `devfile` (H2 en archivo, sin instalar nada) y `mysql` (base real).
- **H2 en `MODE=MySQL`** para que el SQL de desarrollo se parezca al de
  producción y no aparezcan sorpresas al migrar.
- **Esquema explícito en MySQL** (`ddl-auto=validate`) y **generación automática
  en dev**, para que el proyecto arranque rápido pero el modelo real quede
  versionado.
- **Sin Lombok** para que el proyecto compile igual en cualquier IDE sin
  plugins adicionales.
- `ItemInventarioEntity` es solo la base de persistencia. El modelo de dominio
  rico (`Colchon`, `Lote`, estados, movimientos) todavía no está implementado.

---

## Qué falta implementar (próximos pasos)

1. **Modelo**: `ItemInventario` (interfaz), `Colchon`, `Lote`, `Movimiento`.
2. **State**: convertir `EstadoProducto` en interfaz con las cuatro clases de
   estado y sus transiciones válidas.
3. **Composite**: `Colchon` como hoja y `Lote` como compuesto (`getCantidad()`,
   `cambiarEstado()` en cascada).
4. **Observer**: `ObservadorStock` con `ModuloVentas` y `DashboardAuditoria`.
5. **Strategy**: `EstrategiaReserva`, `ReservaMinorista` y `ReservaMayorista`
   (mínimo 5 unidades).
6. **Decorator + Factory**: `IRegistroMovimiento`, `RegistroMovimientoBase`,
   `ValidacionEmbalaje`, `ValidacionFIFO`, `AlertaStockBajo` y
   `FabricaMovimiento`.
7. **Adapter**: `LectorCodigo` con `AdaptadorCamara` y `AdaptadorPistolaUSB`.
8. **Facade + Singleton**: `FachadaEscaneo` y `GestorDeStock`.
9. **API**: controladores que reciban `EscaneoDTO` y devuelvan confirmaciones.
10. **Persistencia**: repositorios de movimientos, mappers y transacciones.

---

## Plan de trabajo

La división de tareas entre los 5 integrantes, el cronograma por sprints y los
contratos de interfaces están en:

- `docs/PLAN-TRABAJO.md` — plan maestro y reglas de trabajo
- `docs/equipo/CONTRATOS.md` — interfaces acordadas (leer antes de codificar)
- `docs/equipo/ROL-*.md` — plan individual de cada integrante

## Comandos útiles

```bash
./mvnw test                 # tests
./mvnw clean package        # genera target/smd-stock-0.0.1-SNAPSHOT.jar
java -jar target/smd-stock-0.0.1-SNAPSHOT.jar
docker compose down         # detiene MySQL (conserva datos)
docker compose down -v      # detiene MySQL y borra datos
```

Reiniciar la base local del perfil `devfile` (Windows):

```powershell
Remove-Item -Recurse -Force .\data
```
