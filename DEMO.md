# Guía para mostrar en clase — State + Composite

> **No hace falta ejecutar nada.** Todo se explica leyendo el código.
> Tiempo estimado: 5–7 minutos.
>
> Si te piden verlo funcionando, la sección **6** trae una colección de Postman
> opcional. La sección **7** explica por qué el dominio no lleva anotaciones.

Abrí el proyecto en el IDE y seguí este orden. Las rutas son relativas a
`src/main/java/com/smd/inventario/`.

---

## 1. El problema (30 segundos)

**Archivo:** `domain/model/EstadoProducto.java`

Decí:

> “Un colchón pasa por cuatro estados: producido, en depósito, reservado y
> despachado. Y no todas las operaciones valen en todos los estados: no puedo
> reservar algo que ya se despachó.”

Mostrá el enum y el dibujo del Javadoc:

```
PRODUCIDO --ingreso--> EN_DEPOSITO --reserva--> RESERVADO --despacho--> DESPACHADO
                             ^                       |
                             +---- cancelación ------+
```

> “Sin patrón, cada operación terminaría en un `switch` gigante repartido en
> varias clases. Con State, cada estado se lleva su propio comportamiento.”

---

## 2. State: cada estado sabe qué hacer (2–3 minutos)

### 2.1 El contrato

**Archivo:** `domain/state/EstadoItem.java`

Mostrá los cuatro métodos: `ingresar`, `reservar`, `cancelarReserva`,
`despachar`.

> “Todos los estados implementan las mismas cuatro operaciones. Eso permite
> tratarlos de forma polimórfica, sin preguntar de qué estado se trata.”

### 2.2 Un estado concreto

**Archivo:** `domain/state/EstadoProducido.java`

Señalá:

- `ingresar()` → `item.cambiarEstado(EN_DEPOSITO)`
- `reservar()`, `cancelarReserva()` y `despachar()` → lanzan
  `TransicionInvalidaException`.

> “El estado producido solo permite ingresar. Si intento reservar, no hay un
> `if`: directamente no existe el camino, lanza excepción.”

### 2.3 El resto de los estados

Abrí en orden y mostrá una línea de cada uno:

| Archivo | Operación válida | Resultado |
|---|---|---|
| `EstadoEnDeposito.java` | `reservar()` | → `RESERVADO` |
| `EstadoReservado.java` | `cancelarReserva()` | → `EN_DEPOSITO` |
| `EstadoReservado.java` | `despachar()` | → `DESPACHADO` |
| `EstadoDespachado.java` | ninguna | estado final |

> “Fijate que `EstadoReservado` tiene dos salidas válidas y `EstadoDespachado`
> ninguna. Toda la matriz está repartida entre las clases.”

### 2.4 Quién delega

**Archivo:** `domain/model/ItemInventarioBase.java`

Mostrá:

```java
public void ingresar() {
    estadoItem.ingresar(this);
}
```

> “El ítem no sabe en qué estado está. Le pasa la responsabilidad al objeto de
> estado, que es el que decide. Eso es State.”

Mostrá también `cambiarEstado`:

```java
public void cambiarEstado(EstadoProducto nuevoEstado) {
    this.estado = nuevoEstado;
    this.estadoItem = Estados.desde(nuevoEstado);
}
```

> “El enum se guarda para persistirlo en la base; el objeto de estado es el que
> tiene el comportamiento.”

### 2.5 La excepción

**Archivo:** `domain/exception/TransicionInvalidaException.java`

> “El mensaje incluye el código del ítem, el estado y la operación. Además, el
> estado no se modifica cuando falla: eso está probado.”

---

## 3. Composite: un lote es un ítem más (2 minutos)

**Archivo:** `domain/model/ItemInventario.java`

> “Colchón y lote implementan la misma interfaz. Por eso el resto del sistema
> trata a un lote igual que a un colchón.”

**Archivo:** `domain/composite/Colchon.java`

> “La hoja: siempre aporta una unidad.”

**Archivo:** `domain/composite/Lote.java`

Señalá los tres métodos:

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

> “Un solo escaneo del lote cambia el estado de los cinco colchones. La
> recursión hace que también funcione con sub-lotes.”

Cerrá con la idea:

> “State resuelve el ciclo de vida; Composite resuelve que un grupo se comporte
> como una unidad. Los dos se combinan: el lote también tiene estado y delega en
> su estado actual.”

---

## 4. La matriz completa (para señalar)

| Estado actual | `ingresar()` | `reservar()` | `cancelar()` | `despachar()` |
|---|---|---|---|---|
| `PRODUCIDO` | → `EN_DEPOSITO` | error | error | error |
| `EN_DEPOSITO` | error | → `RESERVADO` | error | error |
| `RESERVADO` | error | error | → `EN_DEPOSITO` | → `DESPACHADO` |
| `DESPACHADO` | error | error | error | error |

> “Son 16 combinaciones. Las 16 están en `StateTest`.”

---

## 5. Si te preguntan por las pruebas

**Archivos:**

- `src/test/java/com/smd/inventario/domain/state/StateTest.java`
- `src/test/java/com/smd/inventario/domain/composite/CompositeTest.java`

Podés mostrar el test parametrizado sin ejecutarlo:

```java
@ParameterizedTest(name = "{0} + {1} -> {2}")
@MethodSource("casos")
void aplicaLaMatrizDeTransiciones(EstadoProducto inicial, Operacion operacion,
                                  EstadoProducto esperado) {
    ...
}
```

> “El test recorre las 16 combinaciones y verifica dos cosas: que la transición
> válida cambie el estado y que la inválida lance excepción sin modificarlo.”

Si te piden ejecutar:

```powershell
.\mvnw.cmd test
```

Son 16 casos de State + 5 de Composite, más 5 del controller: **28 en total**.

---

## 6. Versión HTTP para Postman (opcional)

Si te piden “mostralo funcionando”, el mismo dominio está expuesto por un
controller **de demo**. No es la API definitiva: guarda todo en memoria y se
reinicia con un request.

**Archivos:**

- `presentation/demo/DemoController.java` — los endpoints
- `presentation/demo/InventarioDemo.java` — almacén en memoria
- `docs/http/demo-state-composite.postman_collection.json` — la colección

### Levantar la app

```powershell
.\mvnw.cmd spring-boot:run
```

Queda en <http://localhost:8080> (perfil `dev`, H2 en memoria).

### Importar la colección

1. Postman → **Import** → **File** → elegir el `.json`.
2. Verificar la variable `baseUrl` = `http://localhost:8080`.
3. Ejecutar en orden, o correr toda la carpeta con **Run collection**.

### Recorrido que muestra

| # | Request | Resultado |
|---|---|---|
| 00 | `POST /api/demo/reset` | Inventario vacío |
| 01 | `POST /api/demo/colchones` | Colchón en `PRODUCIDO` |
| 02 | `POST /api/demo/items/COLCHON-DEMO-01/ingresar` | → `EN_DEPOSITO` |
| 03 | `POST .../reservar` | → `RESERVADO` |
| 04 | `POST .../despachar` | → `DESPACHADO` |
| 05 | `POST .../reservar` otra vez | **409** `TRANSICION_INVALIDA` |
| 06 | `POST /api/demo/lotes` | Lote de 5, `cantidad: 5` |
| 07 | `POST .../LOTE-DEMO-01/ingresar` | Lote **y sus 5 hijos** → `EN_DEPOSITO` |
| 08 | `POST .../LOTE-DEMO-01/reservar` | Todos → `RESERVADO` |
| 09 | `POST .../LOTE-DEMO-01/despachar` | Todos → `DESPACHADO` |
| 10 | `GET /api/demo/items` | Árbol con el estado de cada hijo |
| 11 | `POST .../NO-EXISTE/reservar` | **404** `RECURSO_NO_ENCONTRADO` |

### Endpoints

| Método | Ruta | Body |
|---|---|---|
| `POST` | `/api/demo/colchones` | `{"codigo":"...","estado":"PRODUCIDO"}` |
| `POST` | `/api/demo/lotes` | `{"codigo":"...","estado":"PRODUCIDO","cantidad":5}` |
| `POST` | `/api/demo/items/{codigo}/ingresar` | — |
| `POST` | `/api/demo/items/{codigo}/reservar` | — |
| `POST` | `/api/demo/items/{codigo}/cancelar` | — |
| `POST` | `/api/demo/items/{codigo}/despachar` | — |
| `GET` | `/api/demo/items` | — |
| `POST` | `/api/demo/reset` | — |

`estado` es opcional (por defecto `PRODUCIDO`) y `cantidad` también (por
defecto `5`).

### Cómo se traducen los errores

| Excepción del dominio | HTTP | `codigo` |
|---|---|---|
| `TransicionInvalidaException` | 409 | `TRANSICION_INVALIDA` |
| `RecursoNoEncontradoException` | 404 | `RECURSO_NO_ENCONTRADO` |
| `IllegalArgumentException` | 400 | `SOLICITUD_INVALIDA` |

> “Fijate que el dominio lanza excepciones de negocio y el controller las
> traduce a HTTP. El dominio no sabe que existe HTTP.”

Si preferís no abrir Postman, el mismo flujo está verificado en
`DemoControllerTest` con MockMvc.

---

## 7. ¿Por qué no hay anotaciones de Spring en el dominio?

Porque es **intencional**. El proyecto separa el núcleo del framework:

| Capa | Paquete | Anotaciones |
|---|---|---|
| Dominio | `domain/**` | **ninguna** |
| Persistencia | `persistence/**` | `@Entity`, `@Table`, `@Id`, `@Column` |
| Aplicación / configuración | `config/**` | `@Component`, `@Profile` |
| Presentación | `presentation/**` | `@RestController`, `@Service`, `@RequestMapping` |

Las anotaciones **sí existen**; están en las capas de infraestructura. Por
ejemplo:

```java
@RestController
@RequestMapping("/api/demo")
public class DemoController { ... }
```

```java
@Service
public class InventarioDemo { ... }
```

```java
@Entity
@Table(name = "item_inventario")
public class ItemInventarioEntity { ... }
```

### Por qué el dominio queda limpio

- Se prueba con JUnit **sin levantar Spring** (`StateTest` y `CompositeTest` no
  usan `@SpringBootTest`).
- No depende de Jakarta ni de Spring: si mañana cambia el framework, el dominio
  no se toca.
- El foco del TP son los patrones, no las anotaciones.

> “Spring se queda en el borde: recibe el request, llama al dominio y traduce
> la respuesta. El dominio no sabe que hay un servidor web.”

### ¿Y el `@Autowired` de los constructores?

No hace falta. Desde **Spring 4.3**, si una clase tiene **un único
constructor**, Spring lo usa automáticamente y le inyecta los parámetros:

```java
@RestController
public class DemoController {

    private final InventarioDemo inventario;

    public DemoController(InventarioDemo inventario) {  // sin @Autowired
        this.inventario = inventario;
    }
}
```

`@Autowired` solo es necesario si hay **más de un constructor** y hay que
decirle cuál usar. La inyección por constructor es la recomendada porque:

- los campos pueden ser `final`;
- la clase no se puede crear “a medias”;
- en un test la instanciás con `new DemoController(inventarioDemo)` sin Spring.

---

## Preguntas probables y respuestas cortas

**¿Por qué no un `switch`?**
> Porque cada estado tiene su propio comportamiento y el `switch` habría que
> repetirlo en cada operación. Con State, agregar un estado nuevo es agregar una
> clase, no tocar las existentes.

**¿Qué pasa si reservo dos veces?**
> `EstadoReservado.reservar()` lanza `TransicionInvalidaException` y el estado
> queda igual. Está en el test.

**¿Un lote vacío?**
> `getCantidad()` devuelve 0. Falta la regla de negocio que impida reservarlo;
> está anotada como pendiente, no implementada.

**¿Se puede agregar el mismo colchón a dos lotes?**
> Hoy no está validado: es una limitación conocida. La validación de
> pertenencia única queda para la capa de persistencia.

**¿Por qué hay un enum y clases de estado?**
> El enum (`EstadoProducto`) es el dato que se persiste y viaja en el DTO. Las
> clases de estado son el comportamiento. `Estados` traduce uno al otro.

**¿Por qué el dominio no tiene anotaciones de Spring?**
> Porque el dominio es Java puro y se prueba sin framework. Las anotaciones
> (`@RestController`, `@Service`, `@Entity`) viven en `presentation`,
> `persistence` y `config`. Es una separación deliberada, no un olvido.

**¿Por qué los constructores no llevan `@Autowired`?**
> Desde Spring 4.3, con un único constructor Spring inyecta solo. `@Autowired`
> solo hace falta si hay varios constructores. La inyección por constructor es
> la recomendada: campos `final` y tests con `new`.

**¿Dónde se guarda esto en la base?**
> En `persistence/entity/ItemInventarioEntity`, el estado se guarda como texto
> (`@Enumerated(EnumType.STRING)`). La demo de mañana es solo dominio.

---

## Mapa rápido de archivos

| Archivo | Qué mostrar |
|---|---|
| `domain/model/EstadoProducto.java` | Los 4 estados y el ciclo de vida |
| `domain/state/EstadoItem.java` | El contrato del patrón |
| `domain/state/EstadoProducido.java` | Un estado concreto y sus rechazos |
| `domain/state/EstadoReservado.java` | Estado con dos salidas válidas |
| `domain/state/EstadoDespachado.java` | Estado final |
| `domain/model/ItemInventarioBase.java` | La delegación al estado actual |
| `domain/model/ItemInventario.java` | La interfaz común (Composite) |
| `domain/composite/Colchon.java` | Hoja del Composite |
| `domain/composite/Lote.java` | Compuesto: cantidad y cascada |
| `domain/exception/TransicionInvalidaException.java` | Error de transición |
| `domain/state/StateTest.java` | Las 16 combinaciones |
| `presentation/demo/DemoController.java` | Endpoints de la demo (opcional) |
| `presentation/demo/InventarioDemo.java` | Almacén en memoria (opcional) |
| `docs/http/demo-state-composite.postman_collection.json` | Colección de Postman |

---

## Lo que todavía no está

Para no prometer de más:

- La API REST **definitiva** (fachada + persistencia): próximos sprints. El
  `DemoController` es un atajo didáctico y no persiste nada.
- `GestorDeStock` y `FachadaEscaneo`: próximos sprints.
- Strategy (mínimo de 5 para Rubín), Decorator y Observer: asignados a B y C.
- Persistencia de movimientos y auditoría: D.

> “La demo de hoy es el núcleo del dominio; el resto está planificado en
> `docs/PLAN-TRABAJO.md`.”
