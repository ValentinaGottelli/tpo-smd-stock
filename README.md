# SMD Stock

TPO de **Proceso de Desarrollo de Software** — sistema de control de stock con
escaneo de códigos QR para **SMD Colchón Modular S.A.**

Es una **simulación académica**: no se despliega, no hay usuarios reales y el
objetivo es demostrar **diseño orientado a objetos con patrones**. Por eso el
proyecto es un monolito Spring Boot chico, con una sola base **H2** y sin
infraestructura extra.

Esta entrega deja el **esqueleto funcionando**: proyecto que arranca, base
relacional conectada y un paquete por cada patrón. La lógica de negocio se
implementa en los sprints siguientes (ver `docs/PLAN-TRABAJO.md`).

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

No se usa Docker, MySQL, Lombok ni servicios externos.

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

Arranca en <http://localhost:8080> con el perfil `dev` (H2 en memoria) y tres
ítems de ejemplo.

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

El seeder carga los datos de ejemplo **solo si la base está vacía**. Para
empezar de cero, detener la app y borrar `data/`.

---

## Para la demo de mañana

La guía paso a paso para explicar **sin ejecutar nada** está en
[`DEMO.md`](DEMO.md): qué archivo abrir, qué decir y qué preguntas pueden
aparecer.

Lo que se muestra es el núcleo del dominio:

- **State**: `domain/state` (4 estados + matriz de transiciones)
- **Composite**: `domain/composite` (`Colchon` y `Lote`)
- **Tests**: `StateTest` (16 combinaciones) y `CompositeTest`

Si te piden ejecutar algo: `.\mvnw.cmd test`.

---

## Estructura

```
Entrega3/
├── DEMO.md                            # guía para mostrar en clase
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/            # Maven Wrapper
├── src/main/java/com/smd/inventario/
│   ├── InventarioApplication.java
│   ├── config/                        # seeder de datos de ejemplo
│   ├── domain/
│   │   ├── model/                     # ItemInventario, base, Movimiento, enums
│   │   ├── state/                     # State: los 4 estados
│   │   ├── composite/                 # Composite: Colchon y Lote
│   │   └── exception/                 # errores de negocio
│   ├── persistence/
│   │   ├── entity/                    # entidad JPA
│   │   └── repository/                # Spring Data
│   └── presentation/
│       ├── controller/                # health
│       └── dto/                       # respuesta del health
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties     # H2 memoria
│   ├── application-devfile.properties # H2 archivo
│   └── data.sql                       # datos de ejemplo (dev)
├── data/                              # base H2 (generada, no se versiona)
└── src/test/java/...                  # tests de State y Composite
```

> Las carpetas de los patrones que faltan (`decorator`, `strategy`, `observer`,
> `facade`, etc.) se crean en los próximos sprints. No están vacías en el repo
> para no agregar ruido.

---

## Patrones y ubicación

| Patrón | Paquete | Dueño | Estado |
|---|---|---|---|
| **State** | `domain/state` | A | **Implementado** |
| **Composite** | `domain/composite` | A | **Implementado** |
| Singleton | `domain/service` | A | Pendiente |
| Facade | `domain/facade` | E | Pendiente |
| Decorator, Factory | `domain/decorator`, `domain/factory` | B | Pendiente |
| Strategy, Observer | `domain/strategy`, `domain/observer` | C | Pendiente |
| Repository / JPA | `persistence/**` | D | Base creada |
| Adapter | `presentation/adapter` | E | Pendiente |

---

## Documentación del grupo

- `DEMO.md` — guía para mostrar State + Composite en clase
- `docs/PLAN-TRABAJO.md` — reparto, sprints y reglas de trabajo
- `docs/equipo/CONTRATOS.md` — interfaces acordadas (**leer antes de codificar**)
- `docs/equipo/ROL-*.md` — plan individual de cada integrante

---

## Comandos

```bash
./mvnw test                 # tests
./mvnw clean package        # genera el jar en target/
java -jar target/smd-stock-0.0.1-SNAPSHOT.jar
```
