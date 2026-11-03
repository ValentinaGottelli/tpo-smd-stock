# Rol B — Casos de aceptación y estado

**Patrones:** Decorator, Simple Factory · **Paquetes:** `domain/decorator`, `domain/factory`
**Estado:** implementado y probado (11 tests unitarios + recorrido por consola/HTTP).

## Qué quedó hecho

| Clase | Rol |
|---|---|
| `IRegistroMovimiento` | Interfaz del Decorator: `registrar(EscaneoDTO, Movimiento)` |
| `RegistroMovimientoBase` | Componente base: guarda el movimiento en la auditoría |
| `DecoradorRegistro` | Base abstracta de los decoradores (guarda `siguiente`) |
| `ValidacionEmbalaje` | Rechaza el ingreso con `embalajeOk == false` |
| `ValidacionFIFO` | Rechaza el despacho si hay otro ítem `RESERVADO` más antiguo |
| `AlertaStockBajo` | Loguea `WARN` si el stock en depósito queda bajo el umbral; no aborta |
| `FabricaMovimiento` | Simple Factory: arma la cadena según `TipoMovimiento` |

Cadenas: `INGRESO` → embalaje → base · `DESPACHO` → FIFO → alerta → base ·
`RESERVA`/`CANCELACION`/`AJUSTE` → base.

Cambios en archivos de A (avisar al grupo):
- `GestorDeStock`: recibe `FabricaMovimiento` en vez de `RepositorioMovimientos`; en `ejecutar` llama a la cadena **antes** de `repositorioItems.guardar(item)`.
- `DomainConfig`: bean `fabricaMovimiento` y `gestorDeStock` actualizado.
- `application.properties`: `app.stock.umbral-minimo=10`.
- La colección de Postman (de E) no se modificó: el flujo de un solo ítem sigue funcionando con FIFO.

## Decisiones (difieren del contrato original)

1. **`registrar(EscaneoDTO, Movimiento)`** en vez de `registrar(Movimiento)`: el embalaje vive en la solicitud, no en el `Movimiento`.
2. **Los controles corren antes de guardar el ítem.** Si rechazan, el cambio de estado queda solo en memoria y no se persiste nada. Riesgo conocido: si falla el `guardar` del ítem, el movimiento ya quedó registrado (no hay transacción).
3. **FIFO en el despacho, pero solo entre los `RESERVADO`.** Reservar es libre (cualquier orden); lo que se ordena es la salida: no se despacha una reserva más nueva si hay otra reservada más vieja. Se compara `fechaProduccion`; los ítems que solo están `EN_DEPOSITO` no cuentan. Empate de fechas: se acepta. Como el despacho solo sale de `RESERVADO`, `buscarMasAntiguoEnDeposito()` no se usa; alcanza con `listarPorEstado(RESERVADO)`.
4. **`embalajeOk == null` = no validado** y se acepta (solo `false` rechaza).
5. **`FabricaMovimiento` es una clase**, no una interfaz: es una Simple Factory con una sola implementación.
6. **La alerta solo loguea.** Cuando C tenga el Observer, se publica ahí (`shortcut` comentado en `AlertaStockBajo`).

## Casos de aceptación (todos cubiertos)

| # | Caso | Resultado | Test |
|---|---|---|---|
| 1 | Ingreso con `embalajeOk: true` | Registra | `ingresoConEmbalajeOkRegistra` |
| 1b | Ingreso sin `embalajeOk` | Registra | `ingresoSinValidarEmbalajeRegistra` |
| 2 | Ingreso con `embalajeOk: false` | 409 `REGLA_DE_NEGOCIO`, no registra, el ítem sigue `PRODUCIDO` | `ingresoConEmbalajeDanadoRechazaYNoRegistra`, `gestorNoGuardaElItemSiUnControlRechaza` |
| 3 | Despacho del reservado más antiguo | Registra | `despachoDelReservadoMasAntiguoRegistra` |
| 4 | Despacho de un reservado más nuevo habiendo otro reservado más viejo | 409, el mensaje dice cuál corresponde | `despachoConReservadoMasAntiguoRechaza` |
| 4b | Empate de fechas | Registra | `despachoConEmpateDeFechasAcepta` |
| 4c | Hay uno más viejo pero solo `EN_DEPOSITO` (no reservado) | Registra | `despachoIgnoraLosMasAntiguosQueSoloEstanEnDeposito` |
| 4d | Reservar es libre aunque haya uno más viejo en depósito | Registra | `reservaEsLibreAunqueHayaUnoMasAntiguoEnDeposito` |
| 5 | Despacho con stock bajo el umbral | Registra y loguea WARN | `despachoConStockBajoRegistraSinAbortar` |
| 6 | Reserva no evalúa embalaje | Registra | `reservaNoEvaluaEmbalaje` |

Los tests están en `src/test/java/com/smd/inventario/domain/factory/FabricaMovimientoTest.java`, sin Spring (repositorios en memoria). No verifican el texto del log: la alerta se comprobó a mano en la consola de la app.

## Verificación (perfil `dev`, menú de consola)

Alta de un colchón con fecha `2026-01-01` → ingreso con embalaje dañado → rechazado y sigue `PRODUCIDO` → ingreso OK → reserva → despacho OK (log `Stock bajo: 7 unidades...`) → se reservan `COLCHON-QR-0002`, `COLCHON-QR-0001` y `LOTE-QR-0001` en ese orden (todas OK) → despachar `0002` → **FIFO rechaza** (hay que despachar antes `COLCHON-QR-0001`) → despachar `0001`, `0002` y el lote OK, con los 5 hijos `DESPACHADO`. La auditoría solo muestra los movimientos aceptados.

## Pendiente / límites

- Sin Observer: la alerta no notifica a nadie más que al log.
- FIFO solo compara ítems raíz (colchones sueltos y lotes), no hijos de un lote.
- Un ítem reservado más antiguo bloquea el despacho de los más nuevos hasta que se despache o se cancele su reserva.
- Sección del informe (B4): sin escribir.
