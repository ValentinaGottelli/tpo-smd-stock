# Guía para explicar el proyecto en clase

> **No hace falta ejecutar nada para explicar el diseño.** Todo se puede
> mostrar leyendo el código. Tiempo estimado: 8–10 minutos.
>
> Si piden verlo funcionando, la sección **7** explica cómo correr la API y la
> colección de Postman/Apidog. La sección **10** cubre las preguntas sobre anotaciones
> y `@Autowired`.

Las rutas son relativas a `src/main/java/com/smd/inventario/`.

---

## 1. Recorrido sugerido

| Orden | Tema | Archivos |
|---|---|---|
| 1 | El problema: ciclo de vida | `domain/model/EstadoProducto.java` |
| 2 | **State** | `domain/state/**`, `domain/model/ItemInventarioBase.java` |
| 3 | **Composite** | `domain/model/ItemInventario.java`, `domain/composite/**` |
| 4 | **Facade + servicios** | `domain/facade/**`, `domain/service/**` |
| 5 | **Ports + Adapter + Repository + Mapper** | `domain/port/**`, `persistence/**` |
| 6 | API REST y errores | `presentation/**` |
| 7 | Prueba por HTTP | colección de Postman/Apidog |
| 8 | Decisiones de diseño | anotaciones, `@Autowired`, sin Lombok |

---

## 2. El problema (30 segundos)

**Archivo:** `domain/model/EstadoProducto.java`

Decí:

> “Un colchón pasa por cuatro estados: producido, en depósito, reservado y
> despachado. No todas las operaciones valen en todos los estados: no puedo
> reservar algo que ya se despachó.”

Mostrá el enum y el dibujo del Javadoc:

```
PRODUCIDO --ingreso--> EN_DEPOSITO --reserva--> RESERVADO --despacho--> DESPACHADO
                             ^                       |
                             +---- cancelación ------+
```

> “Sin patrón, cada operación terminaría en un `switch` gigante repetido en
> varias clases. Con State, cada estado se lleva su propio comportamiento.”

---

## 3. State: cada estado sabe qué hacer (2–3 minutos)

### 3.1 El contrato

**Archivo:** `domain/state/EstadoItem.java`

Mostrá los cuatro métodos: `ingresar`, `reservar`, `cancelarReserva`,
`despachar`, más `valor()`.

> “Todos los estados implementan las mismas operaciones. Eso permite tratarlos
> de forma polimórfica, sin preguntar de qué estado se trata.”

### 3.2 Un estado concreto

**Archivo:** `domain/state/EstadoProducido.java`

Señalá:

- `ingresar()` → `item.cambiarEstado(EN_DEPOSITO)`.
- `reservar()`, `cancelarReserva()` y `despachar()` → lanzan
  `TransicionInvalidaException`.

> “El estado producido solo permite ingresar. Si intento reservar no hay un
> `if`: el camino directamente no existe, lanza excepción.”

### 3.3 El resto de los estados

| Archivo | Operación válida | Resultado |
|---|---|---|
| `EstadoEnDeposito.java` | `reservar()` | → `RESERVADO` |
| `EstadoReservado.java` | `cancelarReserva()` | → `EN_DEPOSITO` |
| `EstadoReservado.java` | `despachar()` | → `DESPACHADO` |
| `EstadoDespachado.java` | ninguna | estado final |

> “`EstadoReservado` tiene dos salidas válidas y `EstadoDespachado` ninguna.
> La matriz completa está repartida entre las clases.”

### 3.4 Quién delega

**Archivo:** `domain/model/ItemInventarioBase.java`

```java
@Override
public void ingresar() {
    estadoItem.ingresar(this);
}
```

> “El ítem no decide según su estado: le pasa la responsabilidad al objeto de
> estado. Eso es State.”

Y `cambiarEstado`:

```java
@Override
public void cambiarEstado(EstadoProducto nuevoEstado) {
    this.estado = Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio");
    this.estadoItem = Estados.desde(nuevoEstado);
}
```

> “El enum se guarda para persistirlo; el objeto de estado es el que tiene el
> comportamiento. `Estados` traduce uno al otro.”

### 3.5 La excepción

**Archivo:** `domain/exception/TransicionInvalidaException.java`

> “El mensaje incluye el código del ítem, el estado y la operación. Si la
> transición es inválida, el estado no se modifica.”

---

## 4. Composite: un lote es un ítem más (2 minutos)

**Archivo:** `domain/model/ItemInventario.java`

> “Colchón y lote implementan la misma interfaz. Por eso el resto del sistema
> trata a un lote igual que a un colchón.”

**Archivo:** `domain/composite/Colchon.java`

> “La hoja del Composite: siempre aporta una unidad.”

**Archivo:** `domain/composite/Lote.java`

Señalá los cuatro puntos:

1. `agregar()`: valida nulo, que no se agregue a sí mismo y que no sea un ítem
   despachado.
2. `getCantidad()`: suma los hijos, incluso si un hijo es otro lote.
3. `cambiarEstado()`:

```java
@Override
public void cambiarEstado(EstadoProducto nuevoEstado) {
    super.cambiarEstado(nuevoEstado);
    items.forEach(item -> item.cambiarEstado(nuevoEstado));
}
```

4. `reconstruir(...)`: arma un lote que ya estaba en la base sin pasar por las
   validaciones de `agregar`, porque un lote despachado tiene hijos
   despachados y debe poder leerse.

> “Un solo escaneo del lote cambia el estado de los cinco colchones. La
> recursión hace que también funcione con sub-lotes.”

Cerrá con la idea:

> “State resuelve el ciclo de vida; Composite resuelve que un grupo se comporte
> como una unidad. Se combinan: el lote también tiene estado y delega en su
> estado actual.”

---

## 5. La matriz completa

| Estado actual | `ingresar()` | `reservar()` | `cancelar()` | `despachar()` |
|---|---|---|---|---|
| `PRODUCIDO` | → `EN_DEPOSITO` | error | error | error |
| `EN_DEPOSITO` | error | → `RESERVADO` | error | error |
| `RESERVADO` | error | error | → `EN_DEPOSITO` | → `DESPACHADO` |
| `DESPACHADO` | error | error | error | error |

> “Son 16 combinaciones y la matriz está repartida entre las cuatro clases de
> estado.”

---

## 6. Del dominio a la API (3 minutos)

Este es el recorrido de un `POST /api/escaneos/reserva`. Se puede dibujar en el
pizarrón:

```
EscaneoController
      |  valida el body (Bean Validation)
      v
FachadaEscaneo (interfaz)  ->  FachadaEscaneoImpl
      |  completa el usuario por defecto
      v
GestorDeStock
      |  busca el ítem, ejecuta la transición (State), guarda y audita
      v
RepositorioItems (puerto)  ->  JpaRepositorioItems
      |  traduce con ItemInventarioMapper
      v
ItemInventarioEntity  ->  H2
```

### 6.1 Facade

**Archivos:** `domain/facade/FachadaEscaneo.java` y `FachadaEscaneoImpl.java`

> “La capa REST solo conoce esta interfaz. La implementación no tiene reglas de
> negocio: normaliza el usuario y delega en el gestor.”

Mostrá la normalización:

```java
String usuario = solicitud.usuario() == null || solicitud.usuario().isBlank()
        ? usuarioDefault
        : solicitud.usuario();
```

> “El usuario por defecto sale de `app.escaneo.usuario-default` y se inyecta por
> constructor.”

### 6.2 Servicios del dominio

**Archivo:** `domain/service/GestorDeStock.java`

Mostrá `ejecutar(...)`, que es el corazón del flujo:

```java
ItemInventario item = repositorioItems.buscarPorCodigo(solicitud.codigo())
        .orElseThrow(() -> new RecursoNoEncontradoException(solicitud.codigo()));

EstadoProducto estadoAnterior = item.getEstado();
accion.accept(item);                  // State hace la transición
EstadoProducto estadoNuevo = item.getEstado();

repositorioItems.guardar(item);       // persiste el nuevo estado
// ... arma el Movimiento y lo guarda para la auditoría
```

> “El gestor no sabe qué transición corresponde: recibe la operación como
> `Consumer<ItemInventario>` y el polimorfismo de State decide. Después guarda
> el ítem y el movimiento.”

**Archivo:** `domain/service/ServicioConsultas.java`

> “Las lecturas están separadas de las escrituras. Consultar no cambia nada.”

### 6.3 Singleton

**Archivo:** `config/DomainConfig.java`

```java
@Bean
public GestorDeStock gestorDeStock(RepositorioItems items,
                                   RepositorioMovimientos movimientos) {
    return new GestorDeStock(items, movimientos);
}
```

> “El dominio no lleva `@Service`. Esta configuración lo construye como bean y
> Spring garantiza una única instancia durante toda la vida de la aplicación:
> eso es el Singleton. No usamos el `getInstance()` clásico porque la
> inyección por constructor hace lo mismo y se puede instanciar con `new`.”

### 6.4 Puertos, Adapter, Repository y Mapper

**Archivo:** `domain/port/RepositorioItems.java`

> “El dominio declara lo que necesita y no sabe que existe JPA.”

**Archivo:** `persistence/repository/JpaRepositorioItems.java`

- Implementa el puerto (`implements RepositorioItems`).
- Convierte entidades a objetos de dominio.
- Reconstruye el árbol del lote leyendo las filas cuyo `codigo_padre` apunta al
  lote.

**Archivo:** `persistence/mapper/ItemInventarioMapper.java`

> “El mapper traduce entre `ItemInventarioEntity` y `Colchon`/`Lote`. Así el
> dominio nunca ve una entidad JPA.”

**Archivo:** `persistence/entity/ItemInventarioEntity.java`

Mostrá:

- `@Enumerated(EnumType.STRING)` para guardar el estado como texto.
- `codigo_padre` para la relación lote → hijos sin recursión JPA.
- `@Version` para concurrencia optimista.
- `actualizar(...)` en vez de setters públicos.

> “Repository es Spring Data; Adapter es la clase que implementa el puerto;
> Mapper es la traducción. Cada uno tiene una responsabilidad.”

### 6.5 Controllers

**Archivos:** `presentation/controller/*.java`

> “Los controllers solo conocen HTTP: validan el body, llaman al dominio y
> devuelven el DTO. No tienen reglas de negocio.”

---

## 7. La API por HTTP (opcional)

### Levantar la app

```powershell
.\mvnw.cmd spring-boot:run
```

Queda en <http://localhost:8080> con el perfil `dev` (H2 en memoria).

### Endpoints

| Método | Ruta | Para qué |
|---|---|---|
| `GET` | `/api/health` | Verificar app y base |
| `POST` | `/api/escaneos/ingreso` | `PRODUCIDO` → `EN_DEPOSITO` |
| `POST` | `/api/escaneos/reserva` | `EN_DEPOSITO` → `RESERVADO` |
| `POST` | `/api/escaneos/cancelacion` | `RESERVADO` → `EN_DEPOSITO` |
| `POST` | `/api/escaneos/despacho` | `RESERVADO` → `DESPACHADO` |
| `GET` | `/api/inventario?estado=` | Listar ítems raíz |
| `GET` | `/api/inventario/{codigo}` | Ver un ítem y sus hijos |
| `POST` | `/api/inventario/colchones` | Alta de colchón |
| `POST` | `/api/inventario/lotes` | Alta de lote + hijos |
| `GET` | `/api/auditoria?limite=50` | Últimos movimientos |

### Importar la colección

**Archivo:** `docs/http/inventario.postman_collection.json`

Es formato Postman v2.1; **Apidog también lo importa**.

1. Postman o Apidog → **Import** → **File** → elegir el `.json`.
2. Verificar `baseUrl` = `http://localhost:8080`.
3. Ejecutar en orden o usar **Run collection**.

### Recorrido de la colección

| # | Request | Resultado |
|---|---|---|
| 00 | `GET /api/health` | Genera códigos únicos y verifica la app |
| 01 | `GET /api/health` | `OK`, H2 y 3 ítems raíz |
| 02 | `POST /api/inventario/colchones` | Colchón en `PRODUCIDO` |
| 03 | `POST /api/escaneos/ingreso` | → `EN_DEPOSITO` |
| 04 | `POST /api/escaneos/reserva` | → `RESERVADO` |
| 05 | `POST /api/escaneos/despacho` | → `DESPACHADO` |
| 06 | `POST /api/escaneos/reserva` otra vez | **409** `TRANSICION_INVALIDA` |
| 07 | `POST /api/inventario/lotes` | Lote de 5 con 5 hijos |
| 08 | `POST /api/escaneos/ingreso` | Lote **y sus 5 hijos** → `EN_DEPOSITO` |
| 09 | `POST /api/escaneos/reserva` | Todos → `RESERVADO` |
| 10 | `POST /api/escaneos/despacho` | Todos → `DESPACHADO` |
| 11 | `GET /api/inventario/{lote}` | Árbol con el estado de cada hijo |
| 12 | `GET /api/inventario?estado=DESPACHADO` | Filtro por estado |
| 13 | `GET /api/auditoria?limite=10` | 6 movimientos, el último es el despacho |
| 14 | `POST .../reserva` con código inexistente | **404** `RECURSO_NO_ENCONTRADO` |
| 15 | `POST .../ingreso` sin `codigo` | **400** `SOLICITUD_INVALIDA` |

### Cómo se traducen los errores

**Archivo:** `presentation/exception/ManejadorGlobalDeErrores.java`

| Excepción | HTTP | `codigo` |
|---|---|---|
| `TransicionInvalidaException` | 409 | `TRANSICION_INVALIDA` |
| `ReglaDeNegocioException` | 409 | `REGLA_DE_NEGOCIO` |
| `RecursoNoEncontradoException` | 404 | `RECURSO_NO_ENCONTRADO` |
| `MethodArgumentNotValidException` | 400 | `SOLICITUD_INVALIDA` |
| `IllegalArgumentException` | 400 | `SOLICITUD_INVALIDA` |

> “El dominio lanza excepciones de negocio y un único `@RestControllerAdvice`
> las traduce a HTTP. El dominio no sabe que existe HTTP.”

---

## 8. Si preguntan cómo se verificó

La verificación se hizo con la aplicación levantada y la colección de
Postman/Apidog:

- `GET /api/health` → `OK`, H2 y 3 ítems raíz.
- Alta, ingreso, reserva y despacho → `200` y estados correctos.
- Reservar después del despacho → `409 TRANSICION_INVALIDA`.
- Lote de 5 → un solo escaneo cambia el estado de los 5 hijos.
- Código inexistente → `404`; body sin `codigo` → `400`.
- La auditoría muestra un movimiento por operación.

```powershell
.\mvnw.cmd spring-boot:run
```

> “El flujo se probó de punta a punta contra H2; la colección permite repetir
> exactamente el mismo recorrido.”

---

## 9. ¿Por qué no hay anotaciones de Spring en el dominio?

Es **intencional**. El proyecto separa el núcleo del framework:

| Capa | Paquete | Anotaciones |
|---|---|---|
| Dominio | `domain/**` | **ninguna** |
| Persistencia | `persistence/**` | `@Entity`, `@Table`, `@Id`, `@Column`, `@Version` |
| Configuración | `config/**` | `@Configuration`, `@Bean`, `@Component`, `@Profile` |
| Presentación | `presentation/**` | `@RestController`, `@RequestMapping`, `@RestControllerAdvice` |

Ejemplos reales:

```java
@RestController
@RequestMapping("/api/escaneos")
public class EscaneoController { ... }
```

```java
@Configuration
public class DomainConfig {

    @Bean
    public GestorDeStock gestorDeStock(RepositorioItems items,
                                       RepositorioMovimientos movimientos) {
        return new GestorDeStock(items, movimientos);
    }
}
```

```java
@Entity
@Table(name = "item_inventario")
public class ItemInventarioEntity { ... }
```

### Por qué el dominio queda limpio

- No depende de Jakarta ni de Spring: si cambia el framework, el dominio no se
  toca.
- El foco del TP son los patrones, no las anotaciones.

> “Spring se queda en el borde: recibe el request, llama al dominio y traduce
> la respuesta. El dominio no sabe que hay un servidor web.”

---

## 10. ¿Y el `@Autowired`?

No hace falta. Desde **Spring 4.3**, si una clase tiene **un único
constructor**, Spring lo usa automáticamente y le inyecta los parámetros:

```java
@RestController
public class EscaneoController {

    private final FachadaEscaneo fachadaEscaneo;

    public EscaneoController(FachadaEscaneo fachadaEscaneo) {  // sin @Autowired
        this.fachadaEscaneo = fachadaEscaneo;
    }
}
```

`@Autowired` solo es necesario si hay **más de un constructor** y hay que
decirle cuál usar. La inyección por constructor es la recomendada porque:

- los campos pueden ser `final`;
- la clase no se puede crear “a medias”;
- se puede instanciar con `new` sin depender de Spring.

### ¿Y Lombok?

No se usa. Los DTOs son `record`, el dominio es inmutable y la entidad JPA
expone solo los getters necesarios más `actualizar(...)`.

> “Un `@Setter` público en el dominio rompería State: permitiría cambiar el
> estado sin pasar por la transición válida.”

---

## 11. Preguntas probables

**¿Por qué no un `switch`?**
> Porque cada estado tiene su propio comportamiento y el `switch` habría que
> repetirlo en cada operación. Con State, agregar un estado nuevo es agregar
> una clase, no tocar las existentes.

**¿Qué pasa si reservo dos veces?**
> `EstadoReservado.reservar()` lanza `TransicionInvalidaException` y el estado
> queda igual. La request 06 de la colección lo muestra.

**¿Un lote vacío?**
> `getCantidad()` devuelve 0. El alta por API exige `cantidad >= 1`; la regla
> que impide reservar un lote vacío creado a mano queda pendiente.

**¿Se puede agregar el mismo colchón a dos lotes?**
> El dominio no lo valida. En la base, el hijo tiene un solo `codigo_padre`, así
> que una segunda asignación lo mueve de lote. Es una limitación conocida.

**¿Por qué hay un enum y clases de estado?**
> El enum (`EstadoProducto`) es el dato que se persiste y viaja en los DTO. Las
> clases de estado son el comportamiento. `Estados` traduce uno al otro.

**¿Cómo sabe un lote cuáles son sus hijos en la base?**
> Cada fila hija guarda el código del lote en `codigo_padre`.
> `JpaRepositorioItems` lee esas filas y reconstruye el árbol con
> `Lote.reconstruir(...)`.

**¿Qué pasa si dos operarios escanean a la vez?**
> La entidad tiene `@Version` (concurrencia optimista). Si dos transacciones
> modifican la misma fila, la segunda falla en vez de pisar el cambio.

**¿Dónde se guarda el estado?**
> En `persistence/entity/ItemInventarioEntity`, como texto
> (`@Enumerated(EnumType.STRING)`).

**¿Por qué el dominio no tiene anotaciones de Spring?**
> Porque es Java puro y no depende del framework. Las anotaciones viven en
> `presentation`, `persistence` y `config`. Es una separación deliberada.

**¿Por qué los constructores no llevan `@Autowired`?**
> Desde Spring 4.3, con un único constructor Spring inyecta solo. `@Autowired`
> solo hace falta si hay varios constructores.

---

## 12. Mapa rápido de archivos

| Archivo | Qué mostrar |
|---|---|
| `domain/model/EstadoProducto.java` | Los 4 estados y el ciclo de vida |
| `domain/state/EstadoItem.java` | El contrato del patrón |
| `domain/state/EstadoProducido.java` | Un estado concreto y sus rechazos |
| `domain/state/EstadoReservado.java` | Estado con dos salidas válidas |
| `domain/state/EstadoDespachado.java` | Estado final |
| `domain/model/ItemInventarioBase.java` | Delegación al estado actual |
| `domain/model/ItemInventario.java` | Interfaz común (Composite) |
| `domain/composite/Colchon.java` | Hoja del Composite |
| `domain/composite/Lote.java` | Compuesto: cantidad, cascada y reconstrucción |
| `domain/facade/FachadaEscaneo.java` | Puerta de entrada del dominio |
| `domain/facade/FachadaEscaneoImpl.java` | Normalización y delegación |
| `domain/service/GestorDeStock.java` | Transición + persistencia + auditoría |
| `domain/service/ServicioConsultas.java` | Lecturas sin efectos |
| `domain/port/RepositorioItems.java` | Puerto de persistencia |
| `persistence/repository/JpaRepositorioItems.java` | Adapter que reconstruye lotes |
| `persistence/mapper/ItemInventarioMapper.java` | Entidad ↔ dominio |
| `persistence/entity/ItemInventarioEntity.java` | Estado, `codigo_padre`, `@Version` |
| `presentation/exception/ManejadorGlobalDeErrores.java` | Dominio → HTTP |
| `config/DomainConfig.java` | Singleton por beans |
| `docs/http/inventario.postman_collection.json` | Colección para Postman/Apidog |

---

## 13. Lo que todavía no está

Para no prometer de más:

- **Decorator y Factory** (B): validación de embalaje, FIFO y alerta de stock
  bajo. El campo `embalajeOk` ya viaja en `EscaneoDTO`, pero todavía no se usa.
- **Strategy y Observer** (C): reglas por canal de venta y notificaciones de
  stock. `CanalVenta` ya existe en el modelo.
- **Lector de QR simulado** (Adapter de entrada, E): hoy el código llega por el
  body del request.
- **FIFO**: `RepositorioItems.buscarMasAntiguoEnDeposito()` ya está en el puerto,
  pero todavía no lo usa ninguna operación.

> “La base de la entrega es el dominio y la API funcionando. Los patrones que
> faltan ya tienen contrato acordado en `docs/equipo/CONTRATOS.md`.”
