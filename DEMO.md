# Guía para mostrar en clase — State + Composite

> **No hace falta ejecutar nada.** Todo se explica leyendo el código.
> Tiempo estimado: 5–7 minutos.

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

Son 16 casos de State + 5 de Composite.

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

---

## Lo que todavía no está

Para no prometer de más:

- `GestorDeStock`, `FachadaEscaneo` y la API REST: próximos sprints.
- Strategy (mínimo de 5 para Rubín), Decorator y Observer: asignados a B y C.
- Persistencia de movimientos y auditoría: D.

> “La demo de hoy es el núcleo del dominio; el resto está planificado en
> `docs/PLAN-TRABAJO.md`.”
