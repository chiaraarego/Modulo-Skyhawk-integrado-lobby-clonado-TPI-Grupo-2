# Diagramas de secuencia — Módulo Skyhawk

Basados 100% en el código (`Game1982.pde`, `HomeJuego.java`, `Skyhawk_*.pde`).
Cada diagrama documenta un caso de uso del módulo.

---

## 1. Cargar la partida, el avión e iniciar el juego

Cubre desde que el jugador elige el módulo Skyhawk en el menú hasta que el juego
queda corriendo. El arranque tiene **dos fases**:

- **Fase A (un clic):** el Home prepara el módulo y dispara `iniciar()`. El módulo
  pasa a `INICIANDO` (splash de carga) pero **todavía no crea la partida**.
- **Fase B (en el loop de `draw`):** cuando pasan ~600 ms de splash, el módulo
  crea el `GameController` (la partida), que a su vez crea el avión y los enemigos,
  y recién ahí pasa a `EN_EJECUCION`.

```mermaid
sequenceDiagram
    actor Jugador
    participant Home as HomeJuego
    participant Gestor as GestorModulos
    participant Mod as AvionSkyhawk
    participant Estado as estadoActual<br/>(EstadoJuego)
    participant Game as GameController
    participant Sky as Skyhawk
    participant Enem as Enemigo

    rect rgb(235, 245, 255)
    note over Jugador, Estado: FASE A — Selección e iniciar (un clic / ENTER)
    Jugador->>Home: manejarClick(mx, my)
    Home->>Home: seleccionarModulo("Skyhawk 1942")
    Home->>Gestor: buscarModulo("Skyhawk 1942")
    Gestor-->>Home: AvionSkyhawk

    Home->>Mod: reset()
    note right of Mod: estadoActual = NoIniciadoState<br/>game = null, observers.clear()
    Home->>Mod: agregarObserver(this)
    Home->>Mod: inicializarContexto(ctx)
    Home->>Mod: iniciar()
    activate Mod
    Mod->>Estado: iniciar(this)
    note right of Estado: NoIniciadoState valida:<br/>transición permitida (no lanza)
    Mod->>Mod: estadoActual = IniciandoState
    Mod->>Mod: tiempoInicioCarga = now
    Mod-->>Home: onEventoModulo(INICIADO, "Cargando...")
    deactivate Mod
    Home->>Home: controladorNav.iniciarModulo()<br/>(pantalla = JUEGO)
    end

    rect rgb(235, 255, 235)
    note over Home, Enem: FASE B — Carga diferida dentro del loop draw() (60 fps)
    loop cada frame mientras estado == INICIANDO
        Home->>Mod: actualizar(app)
        alt pasaron < 600 ms (TIEMPO_CARGA_MS)
            Mod->>Mod: (sigue mostrando splash)
        else pasaron >= 600 ms
            Mod->>Game: new GameController()
            activate Game
            Game->>Sky: new Skyhawk(width/2, height-80)
            note right of Sky: vida = 3, velocidad = 5<br/>loadImage("SkyhawkSprite.png")
            Game->>Game: crearEnemigos()
            loop i = 0..4
                Game->>Enem: new Enemigo(x, y)
                note right of Enem: vida = 1, velocidad = 2<br/>loadImage("EnemigoSprite.png")
            end
            Game-->>Mod: game
            deactivate Game
            Mod->>Mod: estadoActual = EnEjecucionState
            Mod->>Mod: tiempoInicio = now
            Mod-->>Home: onEventoModulo(INICIADO, "En ejecucion")
        end
        Home->>Mod: dibujar(app)
        Mod->>Mod: dibujarSplash(app)
    end
    end

    note over Home, Enem: A partir de acá estado == EN_EJECUCION:<br/>cada frame Home llama actualizar()/dibujar()<br/>y el módulo delega en game.actualizar()/game.dibujar()
```

### Notas de diseño

- **Carga perezosa (lazy):** la partida no se crea en `iniciar()` sino en el primer
  `actualizar()` que supera `TIEMPO_CARGA_MS`. Así el splash "CARGANDO..." se ve y la
  creación de sprites no congela el clic.
- **Patrón State:** `iniciar()` primero **valida** contra el estado actual
  (`NoIniciadoState.iniciar()` no lanza excepción) y recién después cambia de estado.
  Si el módulo ya estuviera en ejecución, el estado lanzaría `EstadoInvalidoException`.
- **Patrón Observer:** el módulo no conoce al Home; le **notifica** eventos
  (`INICIADO`) y el Home reacciona. El Home se registró como observer en la Fase A.
- **Dos eventos `INICIADO`:** uno al entrar en `INICIANDO` ("Cargando...") y otro al
  pasar a `EN_EJECUCION` ("En ejecucion").

---

## 2. Mover el avión (arriba, abajo, izquierda, derecha)

Cubre lo que pasa en **un cuadro** mientras el jugador tiene una tecla de movimiento
apretada (W/A/S/D o las flechas). Es un caso de uso que se repite 60 veces por
segundo mientras el estado sea `EN_EJECUCION`.

El detalle de diseño clave: el movimiento **no** llega por el evento `keyPressed` del
Home (`manejarTecla()`), sino que `GameController` **lee directamente** las variables
globales de Processing (`keyPressed`, `key`, `keyCode`) en cada cuadro. Esto se llama
*polling* (consultar el estado del teclado cada frame) y es lo que hace que, al
**mantener apretada** la tecla, el avión se mueva de forma continua.

```mermaid
sequenceDiagram
    participant Proc as Processing
    participant Home as HomeJuego
    participant Mod as AvionSkyhawk
    participant Game as GameController
    participant Sky as Skyhawk
    participant Teclado as Variables globales<br/>(keyPressed, key, keyCode)

    note over Proc, Teclado: Precondición: estado == EN_EJECUCION y el jugador<br/>tiene apretada una tecla de movimiento (ej: 'd')

    Proc->>Home: dibujar()
    Home->>Mod: actualizar(app)
    activate Mod
    note right of Mod: jugadorVivo() == true<br/>(omito el manejo del disparo/espacio)
    Mod->>Game: actualizar()
    activate Game

    Game->>Game: leerTeclado()
    Game->>Teclado: ¿keyPressed?
    Teclado-->>Game: true
    Game->>Teclado: ¿key? / ¿keyCode?
    Teclado-->>Game: key == 'd'

    alt key es 'w' / 'a' / 's' / 'd'
        Game->>Sky: mover(Direccion.DERECHA)
    else key == CODED (flechas)
        note over Game: según keyCode:<br/>UP/DOWN/LEFT/RIGHT
        Game->>Sky: mover(Direccion.DERECHA)
    end

    activate Sky
    note right of Sky: DERECHA → x = x + velocidad (5)<br/>luego clamp a [0, width] x [0, height]
    Sky-->>Game: (posición actualizada)
    deactivate Sky

    Game->>Sky: actualizar()
    note right of Sky: no hace nada para el movimiento<br/>(el cambio de x/y ya lo hizo mover())

    Game-->>Mod: (fin actualizar)
    deactivate Game
    deactivate Mod

    Home->>Mod: dibujar(app)
    Mod->>Game: game.dibujar()
    Game->>Sky: dibujar()
    note right of Sky: image(sprite, x, y) en la nueva posición
```

### Notas de diseño

- **Polling, no eventos:** el movimiento se consulta cada frame dentro de
  `GameController.leerTeclado()`. Por eso *mantener* la tecla mueve continuamente. En
  cambio, ESC/Q sí pasan por el evento `manejarTecla()` del Home (pausar/finalizar) y
  el disparo se detecta por flanco en el módulo. Tres mecanismos distintos según la
  necesidad de cada acción.
- **WASD y flechas a la vez:** las letras viajan en `key`; las flechas son teclas
  "especiales" → Processing pone `key == CODED` y el código real en `keyCode`. Por eso
  hay dos bloques que terminan llamando al mismo `mover(Direccion.X)`.
- **El enum `Direccion`** desacopla "qué tecla se apretó" de "cómo se mueve":
  `leerTeclado()` traduce tecla → `Direccion`, y `Skyhawk.mover()` traduce
  `Direccion` → cambio de coordenadas. Cada uno tiene una sola responsabilidad.
- **Clamp a los bordes:** después de mover, `mover()` corrige `x`/`y` para que el avión
  no se salga de la pantalla (lo deja pegado al borde).
- **`Skyhawk.actualizar()` está vacío** a propósito: el movimiento ya lo aplicó
  `mover()`. Se mantiene por el contrato heredado de `Nave` (todas las naves se
  actualizan en el loop), aunque acá no tenga lógica propia.

---

## 3. Disparar y matar un enemigo

Cubre desde que el jugador aprieta la barra espaciadora hasta que una bala derriba a
un enemigo y suma puntaje. Es importante notar que **no pasa todo en un cuadro**: la
bala se crea en un frame y recién impacta varios frames después, cuando subió lo
suficiente para tocar al enemigo.

- **Fase A (un frame):** se detecta el disparo **por flanco** (la barra pasó de
  no-apretada a apretada) y se crea **una** bala en la punta del avión.
- **Fase B (varios frames):** cada frame la bala sube; cuando entra en rango de un
  enemigo, `detectarColisiones()` le baja la vida, lo derriba (vida llega a 0), suma
  puntaje y lo hace reaparecer arriba.

```mermaid
sequenceDiagram
    participant Proc as Processing<br/>(draw 60 fps)
    participant Mod as AvionSkyhawk
    participant Game as GameController
    participant Bala as ProyectilSkyhawk
    participant Enem as Enemigo

    note over Proc, Enem: Precondición: estado == EN_EJECUCION y jugadorVivo() == true

    rect rgb(255, 245, 235)
    note over Proc, Bala: FASE A — Disparo (el frame en que se aprieta espacio)
    Proc->>Mod: actualizar(app)
    activate Mod
    Mod->>Mod: espacioAhora = keyPressed && key == ' '
    alt espacioAhora && !espacioAntes (flanco: recién se apretó)
        Mod->>Game: dispararSkyhawk()
        Game->>Bala: new ProyectilSkyhawk(skyhawk.x, skyhawk.y - 20)
        Game->>Game: balasJugador.add(bala)
    else ya estaba apretada (o no se apretó)
        note over Mod: no dispara (evita chorro de balas)
    end
    Mod->>Mod: espacioAntes = espacioAhora
    Mod->>Game: actualizar()
    deactivate Mod
    end

    rect rgb(235, 245, 255)
    note over Proc, Enem: FASE B — La bala viaja y derriba al enemigo (frames siguientes)
    loop cada frame, dentro de game.actualizar()
        Game->>Enem: actualizar()
        note right of Enem: el enemigo baja (y += 2)
        Game->>Bala: actualizar()
        note right of Bala: la bala sube (y -= 8)
        Game->>Game: detectarColisiones()

        Game->>Enem: colisionaCon(bala.x, bala.y)
        note right of Enem: dist(enemigo, bala) < 30 ?
        alt todavía no la alcanza
            Enem-->>Game: false
            note over Game: la bala sigue (se conserva si bala.y >= 0)
        else impacto
            Enem-->>Game: true
            Game->>Enem: recibirDanio(1)
            note right of Enem: vida: 1 → 0
            Game->>Enem: estaViva()
            Enem-->>Game: false
            Game->>Game: puntaje += 10
            Game->>Game: enemigosDerribados += 1
            Game->>Enem: reaparecer()
            note right of Enem: y = 0, x = random(width), vida = 1
            note over Game: la bala NO se conserva (golpeo == true)
        end
    end
    end

    note over Proc, Enem: En game.dibujar(): el HUD muestra "Puntaje" actualizado<br/>y el enemigo reaparece arriba como uno nuevo
```

### Notas de diseño

- **Disparo por flanco:** `espacioAhora && !espacioAntes` dispara **una sola** bala por
  pulsación. Sin el `!espacioAntes`, mantener la barra apretada crearía una bala por
  frame (60 por segundo). Compará con el **movimiento**, que sí quiere repetirse al
  mantener la tecla — por eso usa polling puro sin detección de flanco.
- **Por qué espacio se lee distinto:** el Home no le pasa la barra espaciadora al
  módulo (solo intercepta ESC y Q), así que el módulo la lee directo de `app.keyPressed`
  / `app.key` y se guarda `espacioAntes` para comparar entre frames.
- **El `GameController` crea las balas**, no la nave. Igual criterio que con las balas
  enemigas: la nave decide *cuándo* (intención), el controlador maneja *la lista*.
- **"Matar" en realidad es reaparecer:** hay un pool fijo de 5 enemigos que nunca se
  achica. Al derribar uno, no se elimina de la lista: se le reinicia la vida y vuelve
  arriba en una columna al azar (`reaparecer()`). La "muerte" se refleja en el puntaje
  y el contador `enemigosDerribados`, no en el tamaño de la lista.
- **La bala se elimina al golpear:** cuando `golpeo == true`, la bala no se agrega a
  `balasQueSiguen`, así que desaparece. Las que no golpean se conservan mientras sigan
  dentro de la pantalla (`bala.y >= 0`).
- **`recibirDanio` + `estaViva`** vienen de la clase base `Nave`: el mismo mecanismo de
  vida sirve para el enemigo (vida 1, muere de un tiro) y para el avión (vida 3).

---

## 4. Morir (por colisión / choque de proyectil)

Cubre cómo el avión del jugador pierde vida y muere. Hay **dos fuentes de daño**, las
dos detectadas en `detectarColisiones()` y las dos terminan en `skyhawk.recibirDanio(1)`:

1. **Choque:** un enemigo toca al avión.
2. **Proyectil:** una bala enemiga toca al avión.

Como el avión arranca con **vida 3**, hacen falta **3 golpes** para morir. La muerte no
se "ejecuta" en el momento del golpe: el golpe solo baja la vida a 0. Recién en el
**frame siguiente**, el módulo pregunta `jugadorVivo()`, ve que es `false` y arranca la
secuencia de game over.

```mermaid
sequenceDiagram
    participant Proc as Processing<br/>(draw 60 fps)
    participant Mod as AvionSkyhawk
    participant Game as GameController
    participant Sky as Skyhawk
    participant Enem as Enemigo
    participant BalaE as ProyectilEnemigo

    note over Proc, BalaE: Precondición: estado == EN_EJECUCION, skyhawk.vida > 0

    rect rgb(255, 240, 240)
    note over Proc, BalaE: FASE A — Recibir daño (dentro de game.actualizar())
    Proc->>Mod: actualizar(app)
    activate Mod
    note right of Mod: jugadorVivo() == true → juega normal
    Mod->>Game: actualizar()
    activate Game
    Game->>Game: detectarColisiones()

    alt Choque: un enemigo toca al avión
        Game->>Sky: colisionaCon(e.x, e.y)
        Sky-->>Game: true
        Game->>Sky: recibirDanio(1)
        note right of Sky: vida -= 1
        Game->>Enem: reaparecer()
        note right of Enem: el enemigo que chocó vuelve arriba
    else Proyectil: una bala enemiga toca al avión
        Game->>Sky: colisionaCon(bala.x, bala.y)
        Sky-->>Game: true
        Game->>Sky: recibirDanio(1)
        note right of Sky: vida -= 1
        note over Game: la bala NO se conserva (golpeo == true)
    end
    Game-->>Mod: (fin actualizar)
    deactivate Game
    deactivate Mod
    end

    note over Sky: Se repite hasta que el golpe fatal deja vida == 0<br/>(3 golpes: 3 → 2 → 1 → 0)

    rect rgb(240, 240, 240)
    note over Proc, Sky: FASE B — Detección de la muerte (frame siguiente al golpe fatal)
    Proc->>Mod: actualizar(app)
    activate Mod
    Mod->>Game: jugadorVivo()
    Game->>Sky: estaViva()
    note right of Sky: vida > 0 ? → false
    Sky-->>Game: false
    Game-->>Mod: false
    note right of Mod: rama de game over:<br/>muerto = true<br/>tiempoMuerte = now
    deactivate Mod

    loop cada frame mientras !muerto-expirado
        Proc->>Mod: actualizar(app)
        note right of Mod: jugadorVivo() == false → escena congelada
        Proc->>Mod: dibujar(app)
        note right of Mod: dibuja game.dibujar() + cartel "GAME OVER"
    end

    Proc->>Mod: actualizar(app)
    note right of Mod: now - tiempoMuerte >= GAME_OVER_MS (2500)
    Mod->>Mod: finalizar()
    note over Mod: continúa en el diagrama 5<br/>(game over → guardar stats → volver al menú)
    end
```

### Notas de diseño

- **Dos fuentes, un solo método de daño:** tanto el choque como la bala enemiga llaman
  a `skyhawk.recibirDanio(1)`. Centralizar el daño en `Nave.recibirDanio()` evita
  duplicar la lógica de vida.
- **La muerte es un estado, no un evento instantáneo:** `recibirDanio()` solo baja la
  vida. Nadie "mata" al avión en ese instante. La muerte se **detecta** después, cuando
  `jugadorVivo()` (→ `estaViva()` → `vida > 0`) da `false`. Esto desacopla *recibir el
  golpe* de *reaccionar a la muerte*.
- **Game over con demora (`GAME_OVER_MS` = 2500):** al morir no se vuelve al menú de
  golpe. El módulo marca `muerto`, congela la escena, muestra "GAME OVER" ~2.5 s y
  recién después llama `finalizar()`. Misma técnica de "mirar el reloj cada frame" que
  el splash de carga.
- **`muerto` se setea una sola vez:** el `if (!muerto)` arranca el cronómetro en el
  primer frame sin vida; los frames siguientes solo comparan el reloj. Sin esa guarda,
  `tiempoMuerte` se reiniciaría cada frame y nunca se cumpliría la espera.
- **El choque hace reaparecer al enemigo**, pero la bala enemiga simplemente se
  descarta (no se conserva en `balasEnemQueSiguen`): una vez que pegó, ya no existe.

---

## 5. Game over → guardar estadísticas → volver al menú

Continúa donde terminó el diagrama 4: el módulo llama a `finalizar()`. Acá se ve cómo
el módulo avisa al Home (patrón **Observer**), el Home **guarda las estadísticas**
(acumulándolas en disco) y vuelve a la pantalla de selección.

Este mismo flujo se dispara de **dos maneras**: automáticamente al morir (game over) o
manualmente cuando el jugador aprieta **Q**. Las dos terminan en `finalizar()`, así que
de acá en adelante el camino es idéntico.

```mermaid
sequenceDiagram
    participant Mod as AvionSkyhawk
    participant Estado as estadoActual<br/>(EstadoJuego)
    participant Home as HomeJuego<br/>(observer)
    participant GEstad as GestorEstadisticas
    participant Repo as RepositorioEstadisticasArchivo
    participant Nav as ControladorNavegacion

    note over Mod: Disparado por game over (now - tiempoMuerte >= 2500)<br/>o por la tecla Q

    Mod->>Mod: finalizar()
    activate Mod
    Mod->>Estado: finalizar(this)
    note right of Estado: EnEjecucionState permite finalizar()<br/>(no lanza excepción)
    Mod->>Mod: estadoActual = FinalizadoState

    Mod->>Mod: notificar(FINALIZADO)
    Mod->>Home: onEventoModulo(FINALIZADO)
    deactivate Mod

    activate Home
    Home->>Home: finalizarModuloActual()
    Home->>Mod: removerObserver(this)
    note right of Home: se desuscribe PRIMERO<br/>para evitar recursión

    Home->>Mod: getEstadisticasGenerales()
    note right of Mod: arma EstadisticasGenerales:<br/>puntaje, derribados, tiempo,<br/>1 jugada / 0 ganadas / 1 perdida
    Mod-->>Home: stats

    Home->>GEstad: guardarEstadisticas(stats)
    GEstad->>Repo: guardar("Skyhawk 1942", stats)
    activate Repo
    Repo->>Repo: cargar(nombre) — lee JSON anterior si existe
    note right of Repo: ACUMULA: suma stats nuevas + anteriores<br/>(puntaje, partidas, derribados, tiempo)
    Repo->>Repo: escribe Skyhawk 1942.json
    Repo-->>GEstad: (guardado)
    deactivate Repo

    Home->>Mod: getEstado().getNombre()
    Mod-->>Home: "FINALIZADO"
    note right of Home: ya está FINALIZADO →<br/>NO se vuelve a llamar finalizar()

    Home->>Home: moduloActual = null
    Home->>Nav: irSeleccionModulo()
    note right of Nav: pantallaActual = SELECCION
    deactivate Home

    note over Mod, Nav: En el próximo draw(): pantalla == SELECCION →<br/>pantallaSeleccion.dibujar() (de vuelta en el menú)
```

### Notas de diseño

- **Observer desacopla módulo y Home:** el módulo no llama al Home directamente; solo
  emite el evento `FINALIZADO`. El Home, que está suscripto, reacciona. El módulo podría
  funcionar con cualquier observer (o con ninguno) sin cambiar una línea.
- **Desuscribirse primero (`removerObserver`):** evita recursión. Si el Home más abajo
  necesitara llamar `finalizar()` de nuevo, eso volvería a notificar y reentraría en
  `finalizarModuloActual()`. Al desuscribirse antes, ese segundo aviso no le llega.
- **Doble finalize evitado por el patrón State:** el módulo ya pasó a `FINALIZADO` antes
  de notificar, así que el Home consulta `getEstado()`, ve `"FINALIZADO"` y **no** vuelve
  a finalizar. El State sirve de guardia contra transiciones repetidas.
- **Persistencia acumulativa:** `Repo.guardar()` primero **lee** el JSON anterior del
  módulo (si existe), **suma** los valores y reescribe el archivo. Por eso las stats
  persisten y crecen entre partidas/sesiones. Como es un survival, cada partida aporta
  siempre `1 jugada / 0 ganadas / 1 perdida`.
- **Errores de guardado no rompen el juego:** `GestorEstadisticas.guardarEstadisticas()`
  atrapa `PersistenciaException` y solo lo loguea. Aunque falle el disco, el Home igual
  vuelve al menú.
- **Vuelve a SELECCION, no a INICIO:** terminada la partida, el jugador queda en la lista
  de módulos para elegir otro o ver estadísticas, no en la pantalla de título.
- **Dos disparadores, un solo camino:** game over automático y tecla Q llegan ambos a
  `finalizar()` → mismo `notificar(FINALIZADO)` → mismo `finalizarModuloActual()`.

---

## 6. Pausar

Cubre qué pasa cuando el jugador aprieta **ESC** durante la partida. La tecla ESC la
intercepta el **Home** (no el módulo) y funciona como *toggle*: si el juego está
corriendo, pausa; si ya está pausado, reanuda.

El efecto de la pausa se logra por el **cambio de estado** (`EN_EJECUCION → PAUSADO`),
no por el evento. A partir de ahí:

- `actualizar()` **no toca la lógica** del juego → la escena queda congelada.
- `dibujar()` sigue dibujando la escena congelada + el cartel "-- PAUSA --".

```mermaid
sequenceDiagram
    participant Proc as Processing<br/>(draw 60 fps)
    participant Home as HomeJuego<br/>(observer)
    participant Mod as AvionSkyhawk
    participant Estado as estadoActual<br/>(EstadoJuego)
    participant Game as GameController

    rect rgb(245, 240, 255)
    note over Proc, Estado: FASE A — El jugador aprieta ESC (estado == EN_EJECUCION)
    Proc->>Home: manejarTecla(keyCode, key)
    activate Home
    note right of Home: pantalla == JUEGO, keyCode == ESC<br/>app.key = 0 (consume ESC para no cerrar Processing)
    Home->>Mod: getEstado().getNombre()
    Mod-->>Home: "EN_EJECUCION"
    Home->>Mod: pausar()
    activate Mod
    Mod->>Estado: pausar(this)
    note right of Estado: EnEjecucionState permite pausar()<br/>(no lanza excepción)
    Mod->>Mod: estadoActual = PausadoState
    Mod->>Mod: notificar(PAUSADO)
    Mod->>Home: onEventoModulo(PAUSADO)
    note right of Home: el Home solo reacciona a FINALIZADO/ERROR<br/>→ ignora PAUSADO (evento informativo)
    deactivate Mod
    deactivate Home
    end

    rect rgb(240, 240, 240)
    note over Proc, Game: FASE B — Efecto en el loop mientras estado == PAUSADO
    loop cada frame mientras esté pausado
        Proc->>Home: dibujar()
        Home->>Mod: actualizar(app)
        note right of Mod: estado == PAUSADO →<br/>no actualiza la lógica (congelado)
        Home->>Mod: dibujar(app)
        Mod->>Game: game.dibujar()
        note right of Game: escena congelada de fondo
        Mod->>Mod: dibuja "-- PAUSA --"<br/>"ESC reanudar | Q finalizar"
    end
    end

    note over Proc, Game: Salidas: ESC de nuevo → reanudar() (vuelve a EN_EJECUCION)<br/>Q → finalizar() (ver diagrama 5)
```

### Notas de diseño

- **ESC lo maneja el Home, no el módulo:** el contrato pide que los módulos NO manejen
  ESC ni Q; el Home los intercepta. Por eso el disparo (espacio) y el movimiento (WASD)
  los lee el módulo, pero pausar/reanudar/finalizar entran por `manejarTecla()`.
- **ESC es un toggle:** el Home consulta el estado actual. Si es `EN_EJECUCION` llama
  `pausar()`; si es `PAUSADO` llama `reanudar()`. Una sola tecla, dos comportamientos
  según el estado — apoyado en el patrón State.
- **`app.key = 0` consume el ESC:** en Processing, ESC cierra el sketch por defecto.
  Ponerlo en 0 "se come" la tecla para que no cierre la ventana.
- **La pausa la hace el estado, no el evento:** el cambio a `PausadoState` es lo que
  congela el juego (`actualizar()` corta la lógica). El evento `PAUSADO` se emite igual,
  pero el Home no hace nada con él — queda disponible por si otro observer lo necesita.
- **El Home sigue llamando `actualizar()` y `dibujar()` en PAUSADO:** su filtro en
  `dibujar()` incluye los tres estados activos (INICIANDO, EN_EJECUCION, PAUSADO). Por
  eso el módulo DEBE dibujar algo en PAUSADO; si no, la pantalla quedaría congelada sin
  aviso de que está en pausa.
- **State como guardia:** si por algún motivo se llamara `pausar()` fuera de
  `EN_EJECUCION`, el estado lanzaría `EstadoInvalidoException` (el Home la atrapa e
  ignora). El toggle ya evita ese caso, pero el estado protege igual.
- **Reanudar es el inverso simétrico:** ESC desde `PAUSADO` → `reanudar()` →
  `EnEjecucionState`, y `actualizar()` vuelve a correr la lógica del juego.
