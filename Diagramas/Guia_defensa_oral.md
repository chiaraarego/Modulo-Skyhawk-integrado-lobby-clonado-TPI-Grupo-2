# Guía de defensa oral — Módulo Skyhawk 1942

Resumen de apoyo, **explicado en detalle**, para defender el **diagrama de clases** y
los **diagramas de secuencia**. Todo lo que está acá sale del código real del módulo
(`Game1982/`). Pensado para tener a mano durante la defensa: qué decir, por qué se hizo
así, qué te pueden preguntar y cómo responderlo.

Archivos relacionados en esta carpeta:
- [Diagrama_clases_completo.md](Diagrama_clases_completo.md) — clases del módulo + integración con el lobby.
- [DiagramaClases_Skyhawk.md](DiagramaClases_Skyhawk.md) — solo las clases del módulo.
- [DiagramasSecuencia_Skyhawk.md](DiagramasSecuencia_Skyhawk.md) — los 6 casos de uso.

---

## 1. ¿Qué es el trabajo? (frase de arranque)

Es un videojuego hecho en **Processing (Java)**. La arquitectura está dividida en dos
partes que se desarrollan por separado y se integran al final:

- Un **Lobby/Home** (hecho por otro grupo) que muestra el menú de aviones, las
  estadísticas acumuladas y **coordina el ciclo de vida** de cada juego (iniciarlo,
  pausarlo, finalizarlo, guardar sus estadísticas).
- **Módulos de avión** que se "enchufan" al lobby. Cada grupo hace uno. El nuestro es el
  **A-4 Skyhawk**, un *shooter* vertical estilo *1942*: tu avión está abajo, los enemigos
  caen desde arriba, vos disparás para derribarlos y esquivás sus balas.

La clave del diseño es que el lobby define un **contrato** (la interfaz `ModuloJuego`,
con métodos como `iniciar()`, `pausar()`, `actualizar()`, `dibujar()`…) y nuestro módulo
lo **implementa**. Gracias a eso, el lobby puede controlar nuestro juego **sin conocer
una sola línea de su lógica interna**: solo le habla a través del contrato. Esto es
**programar contra una interfaz, no contra una implementación**, uno de los principios
centrales de la POO.

```
Game1982.pde  →  HomeJuego (lobby)  →  «interface» ModuloJuego  →  AvionSkyhawk  →  GameController (nuestro juego)
```

`Game1982.pde` es el punto de entrada del sketch: crea el `HomeJuego` y le **registra**
nuestro `AvionSkyhawk`. De ahí en más, el lobby maneja todo.

---

## 2. El diagrama de clases (explicado)

### 2.1 Tres capas

Conviene presentarlo dividido en tres capas, de adentro hacia afuera:

**Capa 1 — Núcleo del juego (la lógica pura del Skyhawk).** No sabe nada del lobby.

- **`GameController`** — el "cerebro" de la partida. Contiene el avión del jugador, la
  lista de enemigos y las dos listas de balas (las del jugador y las de los enemigos),
  más el `puntaje` y el contador `enemigosDerribados`. Sus métodos principales:
  - `actualizar()`: el latido de la partida. Lee el teclado, mueve el avión, mueve los
    enemigos y las balas, y llama a `detectarColisiones()`.
  - `detectarColisiones()`: resuelve los choques (bala del jugador vs enemigo, enemigo
    vs avión, bala enemiga vs avión) y actualiza puntaje/vida.
  - `dibujar()`: pinta todo en pantalla + el HUD (puntaje y vida).
- **`Nave`** (clase base) → **`Skyhawk`** (jugador) y **`Enemigo`**. `Nave` aporta lo
  común a toda nave: posición `x`/`y`, `vida`, y los métodos `colisionaCon()`,
  `recibirDanio()` y `estaViva()`.
  - `Skyhawk`: vida 3, velocidad 5, su sprite; el método `mover(Direccion)` lo desplaza
    sin dejarlo salir de la pantalla.
  - `Enemigo`: vida 1 (muere de un tiro), velocidad 2; cae, `reaparecer()` lo manda
    arriba en una columna al azar, e `intentaDisparar()` decide al azar si dispara.
- **`Proyectil`** (clase base) → **`ProyectilSkyhawk`** (sube, color rojo) y
  **`ProyectilEnemigo`** (baja, color naranja). Velocidad 8.
- **`Direccion`** (enum) — las 4 direcciones de movimiento. **Ver detalle en 2.3.**

**Capa 2 — Adaptador (`AvionSkyhawk`).** Implementa la interfaz `ModuloJuego` y
**delega** toda la lógica del juego en un `GameController`. Es el traductor entre "lo que
el lobby pide" y "lo que nuestro juego sabe hacer". Además guarda el estado actual
(patrón State) y la lista de observers (patrón Observer), y maneja los tiempos del splash
de carga y del game over.

**Capa 3 — Contratos del lobby (externos).** Cosas que **usamos pero no definimos**:
`ModuloJuego`, `EstadoJuego`, `IModuloObserver`, `ContextoJuego`, `ModuloEvento`,
`EstadisticasGenerales` y los estados (`NoIniciadoState`, `IniciandoState`, etc.). En el
diagrama se muestran como frontera para entender de qué depende el módulo.

### 2.2 Tipos de relación (saber nombrarlos con sus símbolos)

- **Herencia / generalización (`<|--`, "es un")**: `Skyhawk` y `Enemigo` **son** una
  `Nave`; los dos proyectiles **son** un `Proyectil`. Las clases base traen métodos
  vacíos (`actualizar()`, `dibujar()`) que las hijas **sobrescriben** → polimorfismo: el
  `GameController` puede recorrer una lista de `Enemigo` y llamar a `dibujar()` sin saber
  el detalle de cada uno.
- **Composición (`*--`, "es parte de / contiene y posee")**: `GameController` crea y
  contiene un `Skyhawk`, una lista de `Enemigo` y dos de balas. `AvionSkyhawk` contiene
  un `GameController`. Si se destruye el contenedor, se destruye el contenido.
- **Realización / implementación (`..|>`)**: `AvionSkyhawk` implementa `ModuloJuego`;
  cada `*State` implementa `EstadoJuego`.
- **Dependencia (`..>`, "usa")**: `Skyhawk` y `GameController` *usan* `Direccion`;
  `AvionSkyhawk` *crea* estados, eventos y estadísticas.

### 2.3 El enum `Direccion` en detalle (qué hace y para qué sirve)

```java
enum Direccion { ARRIBA, ABAJO, IZQUIERDA, DERECHA }
```

**Qué es.** Un `enum` (enumeración) es un tipo de dato con un **conjunto fijo y cerrado
de valores con nombre**. `Direccion` puede valer únicamente una de cuatro cosas:
`ARRIBA`, `ABAJO`, `IZQUIERDA` o `DERECHA`. No existe una quinta dirección posible, y el
compilador lo garantiza.

**Qué hace en el juego.** Representa "hacia dónde se quiere mover el avión". Aparece en
**dos lugares**, y ahí está toda su gracia:

1. **Donde se *crea* la dirección** — en `GameController.leerTeclado()`. Ahí se traduce
   la tecla física a una `Direccion`:
   ```java
   if (key == 'w')    skyhawk.mover(Direccion.ARRIBA);   // tecla W
   if (keyCode == UP) skyhawk.mover(Direccion.ARRIBA);   // flecha ↑
   ```
2. **Donde se *usa* la dirección** — en `Skyhawk.mover(Direccion d)`. Ahí se traduce la
   `Direccion` a un cambio de coordenadas:
   ```java
   if (d == Direccion.ARRIBA) this.y = this.y - this.velocidad;  // restar en Y = subir
   ```

Es decir, hay una **cadena de traducciones**:

```
   tecla física        →        Direccion        →        cambio de x / y
  ('w' o flecha ↑)               (ARRIBA)                  (y = y - 5)
        en                          en                          en
   leerTeclado()                  el enum                     mover()
```

**Para qué sirve (por qué se usa un enum y no, por ejemplo, números).** Dos razones, y
las dos suman en la defensa:

- **Legibilidad y seguridad.** Se lee `Direccion.ARRIBA` en lugar de un `0` suelto cuyo
  significado habría que recordar. Y como el conjunto es cerrado, es **imposible** pasar
  una dirección inválida: con un `int` podrías mandar `mover(7)` por error; con el enum,
  el código directamente no compila.
- **Desacople (la razón de diseño más fuerte).** El enum separa **dos preguntas
  distintas** que antes estarían mezcladas:
  - *"¿Qué apretó el jugador?"* → lo resuelve `leerTeclado()` (sabe de teclas).
  - *"¿Cómo se mueve el avión?"* → lo resuelve `mover()` (sabe de coordenadas).

  `mover()` **no tiene idea** de que existe una tecla W ni una flecha; solo entiende
  `Direccion`. Y `leerTeclado()` no sabe cómo se calculan las coordenadas. El enum es el
  "pegamento" limpio entre ambos.

  **Ventaja concreta para nombrar en la defensa:** si mañana quisiéramos controlar el
  avión con un *joystick* o con el mouse, **no tocaríamos `mover()` para nada**: solo
  agregaríamos código que produzca `Direccion.ARRIBA` desde el nuevo control. Y al revés:
  si cambiáramos cómo se mueve (que rebote en el borde, que acelere), tocaríamos solo
  `mover()` y `leerTeclado()` ni se enteraría. Eso es **bajo acoplamiento**.

> Frase corta para decir: *"El enum `Direccion` desacopla la entrada del jugador de la
> física del movimiento: una parte traduce tecla → Direccion, la otra Direccion →
> coordenadas, y ninguna depende de la otra."*

### 2.4 Los tres patrones de diseño (lo más importante)

| Patrón | Dónde está | Qué problema resuelve |
|--------|-----------|-----------------------|
| **Adapter** | `AvionSkyhawk` adapta `GameController` al contrato `ModuloJuego` | Conectar nuestro juego al lobby **sin modificar ninguno de los dos**. El juego no se diseñó pensando en el lobby, y el lobby no conoce el juego. |
| **State** | Campo `estadoActual : EstadoJuego` + clases `*State` | Controlar el **ciclo de vida**: cada estado sabe qué transiciones son válidas y rechaza las inválidas (ej.: no podés `pausar()` algo que no está corriendo). |
| **Observer** | Lista `observers` + `notificar()` + interfaz `IModuloObserver` | Avisarle al lobby de eventos (INICIADO, PAUSADO, FINALIZADO…) **sin que el módulo dependa del lobby**. El módulo emite eventos "al aire"; quien quiera, se suscribe. |

**Patrón State con más detalle.** En vez de tener un `if` gigante con un `int estado`,
cada estado es **una clase** (`NoIniciadoState`, `IniciandoState`, `EnEjecucionState`,
`PausadoState`, `FinalizadoState`, `ErrorState`) que implementa `EstadoJuego`. Cuando se
llama `pausar()`, el módulo le pregunta primero al estado actual si esa transición es
válida: `EnEjecucionState.pausar()` no lanza nada (válido), pero
`FinalizadoState.pausar()` lanzaría `EstadoInvalidoException`. El módulo **valida →
cambia de estado → notifica**, siempre en ese orden.

**Patrón Observer con más detalle.** `AvionSkyhawk` tiene una lista de `IModuloObserver`.
Cuando pasa algo importante, recorre la lista y llama `onEventoModulo(evento)` a cada uno
(método `notificar()`). El `HomeJuego` se registra como observer al iniciar el módulo, así
se entera cuando termina la partida y puede guardar las estadísticas y volver al menú. El
módulo **no llama al Home directamente**: solo emite el evento.

> Si te preguntan "¿por qué tantos patrones y no todo junto?", la respuesta es **bajo
> acoplamiento y alta cohesión**: cada clase tiene una sola responsabilidad y el módulo
> no depende de la implementación concreta del lobby, solo de sus interfaces.

---

## 3. Los diagramas de secuencia (los 6 casos de uso, explicados)

Para cada uno: qué representa, cómo fluye y el **concepto clave** que conviene decir.

### 3.1 Cargar la partida, el avión e iniciar el juego
Arranca en **dos fases**:
- **Fase A (un clic):** el Home prepara el módulo (`reset()`, `agregarObserver()`,
  `inicializarContexto()`) y llama `iniciar()`. El módulo valida con el estado, pasa a
  `INICIANDO` y notifica "Cargando…". **Todavía no existe la partida.**
- **Fase B (dentro del loop):** cada frame, `actualizar()` mira el reloj; cuando pasan
  ~600 ms crea el `GameController` (que en su constructor crea el `Skyhawk` y los 5
  `Enemigo`), pasa a `EN_EJECUCION` y notifica "En ejecución".

**Concepto clave — carga perezosa (*lazy*):** la partida no se crea en el clic sino un
ratito después, mientras se ve "CARGANDO…". Así crear los sprites no congela la interfaz.

### 3.2 Mover el avión (WASD / flechas)
Cada frame, `GameController.leerTeclado()` **consulta directamente** las variables
globales de Processing (`keyPressed`, `key`, `keyCode`), traduce la tecla a una
`Direccion` (ver 2.3) y llama `Skyhawk.mover()`, que cambia `x`/`y` y hace *clamp* para
no salirse de la pantalla.

**Concepto clave — *polling*:** se consulta el teclado en cada frame (no se espera un
evento). Por eso **mantener** la tecla apretada mueve de forma continua.

### 3.3 Disparar y matar un enemigo
También en dos tiempos:
- **(A)** Al apretar espacio se detecta el **flanco** (la barra pasó de no-apretada a
  apretada) y se crea **una** bala en la punta del avión.
- **(B)** En frames siguientes la bala sube; cuando colisiona con un enemigo,
  `detectarColisiones()` le baja la vida, lo derriba (vida 1 → 0), suma 10 al puntaje,
  incrementa `enemigosDerribados` y lo hace **reaparecer** arriba.

**Concepto clave — "matar" = `reaparecer()`:** hay un pool fijo de 5 enemigos que nunca
se achica. La muerte se refleja en el puntaje y el contador, **no** en el tamaño de la
lista. La bala que pega se descarta (no se conserva).

### 3.4 Morir (colisión / proyectil)
Dos fuentes de daño —choque con un enemigo **o** impacto de una bala enemiga— que
convergen en `skyhawk.recibirDanio(1)`. Como el avión tiene vida 3, hacen falta 3 golpes.

**Concepto clave — la muerte es un estado que se *detecta*, no un evento:** el golpe solo
baja la vida. En el **frame siguiente**, `jugadorVivo()` (→ `estaViva()` → `vida > 0`) da
`false` y recién ahí arranca el game over: se marca `muerto`, se congela la escena, se
muestra "GAME OVER" ~2.5 s y después se llama `finalizar()`.

### 3.5 Game over → guardar estadísticas → volver al menú
`finalizar()` cambia el estado a `FINALIZADO` y **notifica** el evento. El Home (que es
observer) lo recibe, **se desuscribe** (para evitar recursión), pide las estadísticas,
las **guarda acumulándolas** en disco, pone el módulo en `null` y vuelve a la pantalla de
selección.

**Concepto clave — se juntan los 3 patrones:** **State** (cambio a FINALIZADO),
**Observer** (notificación al Home) y **persistencia** (JSON acumulado: lee lo anterior,
suma y reescribe). Este mismo flujo lo disparan el game over automático y la tecla Q.

### 3.6 Pausar (ESC)
ESC lo intercepta el **Home** (no el módulo) y funciona como **toggle**: si el juego
corre, `pausar()`; si está pausado, `reanudar()`. El cambio a `PausadoState` hace que
`actualizar()` no toque la lógica (congela) y `dibujar()` muestre "-- PAUSA --".

**Concepto clave — la pausa la produce el cambio de estado, no el evento:** el evento
`PAUSADO` se emite igual, pero el Home **no reacciona** a él (solo a FINALIZADO/ERROR); es
informativo. Lo que congela el juego es estar en `PausadoState`.

---

## 4. Conceptos transversales (los que más se preguntan)

### Los tres mecanismos de entrada (¡muy preguntado!)
No es casualidad que cada acción se lea distinto; cada una tiene una necesidad diferente:

| Acción | Mecanismo | Por qué |
|--------|-----------|---------|
| Mover (WASD/flechas) | *Polling* cada frame en `leerTeclado()` | Mantener la tecla debe mover continuamente |
| Disparar (espacio) | Detección por **flanco** en el módulo | Una bala por pulsación, no un chorro de 60 por segundo |
| Pausar/Finalizar (ESC/Q) | **Evento** del Home (`manejarTecla()`) | Son acciones del lobby, ocurren una sola vez |

### `key` vs `keyCode` (entrada de teclado en Processing)
- `keyPressed` (boolean): ¿hay alguna tecla apretada?
- `key` (char): el **carácter** de la última tecla (ej.: `'w'`, `' '`). Para teclas
  especiales que no tienen carácter (flechas, Shift), Processing pone `key == CODED`.
- `keyCode` (int): el **código numérico**; se usa para las teclas especiales (`UP`,
  `DOWN`, `LEFT`, `RIGHT` son constantes con nombre). Por eso `leerTeclado()` tiene un
  bloque para letras (`key == 'w'`) y otro para flechas (`key == CODED` → `keyCode == UP`).

### El "truco del reloj" (esperar sin congelar)
Processing no permite "esperar 600 ms parado" (congelaría todo el sketch). En su lugar se
guarda la hora de inicio y **cada frame se compara** `ahora - inicio >= límite`. Mientras
no se cumple, se muestra el cartel; cuando se cumple, se ejecuta la acción **una sola vez**
(el cambio de estado evita que vuelva a entrar). Se usa para el splash de carga
(`TIEMPO_CARGA_MS = 600`) y para el game over (`GAME_OVER_MS = 2500`).

### `app.key = 0` (consumir ESC)
Processing **cierra el sketch** si detecta ESC. Después de usar ESC para pausar, se hace
`app.key = 0` (es una **asignación**, `=`, no una comparación `==`): se *borra* la tecla
para que, cuando Processing revise si hubo ESC, ya no la encuentre y no cierre la ventana.

### Persistencia acumulativa
Al guardar, el repositorio **lee** el JSON anterior del módulo (si existe), **suma** los
valores y reescribe el archivo. Por eso las estadísticas persisten y crecen entre
partidas y sesiones. Como es un *survival* (sin victoria), cada partida aporta
`1 jugada / 0 ganadas / 1 perdida`. Si el guardado falla, se captura la excepción y el
juego igual vuelve al menú (robustez).

---

## 5. Preguntas frecuentes y cómo responderlas

**¿Por qué `AvionSkyhawk` es `.pde` y no `.java`?**
Para que nuestras clases (`Skyhawk`, `Enemigo`, …) queden como *inner classes* del sketch
y puedan usar las funciones globales de Processing (`image`, `rect`, `width`…) sin
prefijar `app.`.

**¿Por qué `Skyhawk.actualizar()` está vacío?**
El movimiento ya lo aplica `mover()` cuando se lee el teclado. El método existe por el
contrato heredado de `Nave` (todas las naves se actualizan en el loop), aunque acá no
tenga lógica propia.

**¿Para qué sirve el enum `Direccion`?**
Para desacoplar la entrada del jugador de la física del movimiento, y para que sea
imposible pasar una dirección inválida. (Ver explicación completa en 2.3.)

**¿Qué pasa si el módulo nunca notifica `FINALIZADO`?**
El Home nunca vuelve al menú ni guarda las estadísticas. Por eso el game over termina
siempre llamando a `finalizar()`.

**¿Cómo se evita disparar dos veces o finalizar dos veces?**
- Disparo: detección por **flanco** (`espacioAhora && !espacioAntes`).
- Finalizar: el patrón **State** actúa de guardia — el módulo ya está en `FINALIZADO`, así
  que el Home consulta el estado y no vuelve a finalizar. Además se **desuscribe** del
  observer antes, para evitar recursión.

**¿Cómo se detectan las colisiones?**
Por distancia simple: `dist(x1, y1, x2, y2) < 30` en `Nave.colisionaCon()`. Si la
distancia entre dos objetos es menor a 30 píxeles, se considera que se tocan.

**¿Por qué hay dos clases base (`Nave` y `Proyectil`) con métodos vacíos?**
Para aprovechar herencia y polimorfismo: el `GameController` trata a todos los enemigos
(o todas las balas) igual, recorriendo listas y llamando `actualizar()`/`dibujar()`, sin
preguntar de qué tipo es cada uno.

---

## 6. Mejoras pendientes (para la sección "posibles mejoras")

Mostrar que conocés los límites del trabajo suma puntos:

1. **Límites de pantalla del avión:** el sprite se dibuja centrado (90×60) pero el *clamp*
   es contra `0`/`width`/`height`, así que puede salirse media imagen por el borde.
   Faltaría restar/sumar el medio ancho.
2. **`Nave` no inicializa `vida`:** funciona solo porque cada subclase la setea; es
   frágil. Convendría inicializarla en la base o por constructor.
3. **`loadImage` por instancia:** cada `Skyhawk`/`Enemigo` recarga el PNG. Se podría
   cargar el sprite una sola vez y compartirlo.
4. **Dificultad fija:** velocidad y frecuencia de disparo de los enemigos son constantes;
   se podría escalar con el puntaje.
5. **Usar el `ContextoJuego`:** el juego usa los globales `width`/`height` en vez de
   `contexto.getAnchoPantalla()/getAltoPantalla()` como recomienda el contrato.

---

## 7. Glosario rápido

- **Sketch:** un programa de Processing. El nuestro es `Game1982`.
- **Frame / loop:** Processing ejecuta `draw()` ~60 veces por segundo.
- **Polling:** consultar el estado (del teclado) en cada frame, en vez de esperar un evento.
- **Flanco:** transición de una señal (tecla no-apretada → apretada).
- **Enum (enumeración):** tipo con un conjunto fijo y cerrado de valores con nombre (ej.: `Direccion`).
- **Patrón State:** cada estado decide qué transiciones permite.
- **Patrón Observer:** un objeto (módulo) notifica eventos a sus suscriptores (Home).
- **Patrón Adapter:** una clase traduce una interfaz a otra (AvionSkyhawk ↔ GameController).
- **Polimorfismo:** tratar objetos de distintas clases hijas a través de su clase base.
- **Acoplamiento:** cuánto depende una clase de otra; buscamos que sea **bajo**.
- **Survival:** modo sin condición de victoria; se juega hasta morir.
