# Guía de defensa oral — Módulo Skyhawk 1942

Resumen de apoyo para defender el **diagrama de clases** y los **diagramas de
secuencia**. Todo lo que está acá sale del código real del módulo (`Game1982/`).
Pensado para tener a mano durante la defensa: qué decir, qué te pueden preguntar y
cómo responderlo.

Archivos relacionados en esta carpeta:
- [Diagrama_clases_completo.md](Diagrama_clases_completo.md) — clases del módulo + integración con el lobby.
- [DiagramaClases_Skyhawk.md](DiagramaClases_Skyhawk.md) — solo las clases del módulo.
- [DiagramasSecuencia_Skyhawk.md](DiagramasSecuencia_Skyhawk.md) — los 6 casos de uso.

---

## 1. ¿Qué es el trabajo? (frase de arranque)

Es un videojuego hecho en **Processing (Java)**. La arquitectura está dividida en dos
partes:

- Un **Lobby/Home** (hecho por otro grupo) que muestra el menú, las estadísticas y
  coordina el ciclo de vida de cada juego.
- **Módulos de avión** que se "enchufan" al lobby. Nuestro módulo es el **A-4 Skyhawk**,
  un shooter vertical estilo *1942*.

La clave del diseño: el lobby define un **contrato** (la interfaz `ModuloJuego`) y
nuestro módulo lo implementa. Así el lobby coordina nuestro juego **sin conocer sus
detalles internos**.

```
Game1982.pde  →  HomeJuego (lobby)  →  «interface» ModuloJuego  →  AvionSkyhawk  →  GameController (nuestro juego)
```

---

## 2. El diagrama de clases

### 2.1 Tres capas (así conviene explicarlo)

1. **Núcleo del juego** — la lógica pura del Skyhawk:
   - `GameController`: el orquestador de la partida.
   - Jerarquía `Nave` → `Skyhawk`, `Enemigo`.
   - Jerarquía `Proyectil` → `ProyectilSkyhawk`, `ProyectilEnemigo`.
   - `Direccion` (enum).
2. **Adaptador** — `AvionSkyhawk`: implementa `ModuloJuego` y **delega** toda la lógica
   en `GameController`. Es el puente entre el lobby y nuestro juego.
3. **Contratos del lobby** (externos) — interfaces y clases que **usamos pero no
   definimos**: `ModuloJuego`, `EstadoJuego`, `IModuloObserver`, `ContextoJuego`,
   `ModuloEvento`, `EstadisticasGenerales` y los estados (`NoIniciadoState`, etc.).

### 2.2 Relaciones que tenés que saber nombrar

- **Herencia (`extends`)**: `Skyhawk` y `Enemigo` heredan de `Nave`; los dos proyectiles
  heredan de `Proyectil`. Las clases base traen métodos vacíos (`actualizar()`,
  `dibujar()`) que las hijas **sobrescriben** (polimorfismo).
- **Composición (`*--`)**: `GameController` **contiene y crea** un `Skyhawk`, una lista
  de `Enemigo` y dos listas de balas. `AvionSkyhawk` contiene un `GameController`.
- **Realización / implementación (`..|>`)**: `AvionSkyhawk` implementa `ModuloJuego`;
  cada `*State` implementa `EstadoJuego`.
- **Dependencia (`..>`)**: `Skyhawk` y `GameController` *usan* `Direccion`; `AvionSkyhawk`
  *crea* los estados, los eventos y las estadísticas.

### 2.3 Los tres patrones de diseño (lo más importante de la defensa)

| Patrón | Dónde está | Para qué sirve |
|--------|-----------|----------------|
| **Adapter** | `AvionSkyhawk` adapta `GameController` al contrato `ModuloJuego` | Conectar nuestro juego al lobby sin modificar ninguno de los dos |
| **State** | Campo `estadoActual : EstadoJuego` + clases `*State` | Controlar el ciclo de vida (qué transiciones son válidas en cada estado) |
| **Observer** | Lista `observers` + método `notificar()` + interfaz `IModuloObserver` | Avisarle al lobby de eventos (INICIADO, PAUSADO, FINALIZADO…) sin depender de él |

> Si te preguntan "¿por qué patrones y no todo junto?", la respuesta es:
> **bajo acoplamiento y alta cohesión**. Cada clase tiene una responsabilidad y el
> módulo no depende de la implementación concreta del lobby.

---

## 3. Los diagramas de secuencia (los 6 casos de uso)

Para cada uno, la **idea central** que conviene decir en voz alta:

### 3.1 Cargar la partida, el avión e iniciar el juego
- Arranca en **dos fases**: (A) un clic dispara `iniciar()` → estado `INICIANDO`
  (splash de carga), pero **todavía no se crea la partida**; (B) dentro del loop, cuando
  pasan ~600 ms, se crea el `GameController` (que crea el avión y los 5 enemigos) y se
  pasa a `EN_EJECUCION`.
- **Concepto clave:** carga perezosa (*lazy*). Crear los sprites no congela el clic; el
  jugador ve "CARGANDO…".

### 3.2 Mover el avión (WASD / flechas)
- Cada frame, `GameController.leerTeclado()` **consulta directamente** las variables
  globales de Processing (`keyPressed`, `key`, `keyCode`) → traduce a `Direccion` →
  `Skyhawk.mover()` cambia las coordenadas.
- **Concepto clave:** *polling* (consultar cada frame). Por eso mantener la tecla mueve
  de forma continua.

### 3.3 Disparar y matar un enemigo
- También en **dos tiempos**: (A) al apretar espacio se detecta el **flanco** (pasó de
  no-apretada a apretada) y se crea **una** bala; (B) en frames siguientes la bala sube
  y, al colisionar, `detectarColisiones()` baja la vida del enemigo, lo derriba, suma
  puntaje y lo hace **reaparecer**.
- **Concepto clave:** "matar" = `reaparecer()`. Hay un pool fijo de 5 enemigos que nunca
  se achica; la muerte se refleja en el puntaje y el contador, no en el tamaño de la lista.

### 3.4 Morir (colisión / proyectil)
- Dos fuentes de daño (choque con enemigo **o** bala enemiga) que convergen en
  `skyhawk.recibirDanio(1)`. Con vida 3 hacen falta 3 golpes.
- **Concepto clave:** la muerte es un **estado que se detecta**, no un evento. El golpe
  solo baja la vida; en el frame siguiente `jugadorVivo()` da `false` y arranca el game
  over (con demora de 2.5 s antes de finalizar).

### 3.5 Game over → guardar estadísticas → volver al menú
- `finalizar()` cambia el estado a `FINALIZADO` y **notifica** el evento. El Home (que
  es observer) lo recibe, guarda las estadísticas (acumulándolas en disco) y vuelve a la
  pantalla de selección.
- **Concepto clave:** se juntan los 3 patrones — **State** (cambio a FINALIZADO),
  **Observer** (notificación al Home) y **persistencia** (JSON acumulado).

### 3.6 Pausar (ESC)
- ESC lo intercepta el **Home** (no el módulo) y funciona como **toggle**: pausa si está
  corriendo, reanuda si está pausado. El cambio a `PausadoState` hace que `actualizar()`
  no toque la lógica (congela) y `dibujar()` muestre "-- PAUSA --".
- **Concepto clave:** la pausa la produce el **cambio de estado**, no el evento. El
  evento `PAUSADO` se emite igual pero el Home no reacciona a él (es informativo).

---

## 4. Conceptos transversales (los que más se preguntan)

### Los tres mecanismos de entrada (¡muy preguntado!)
No es casualidad que cada acción se lea distinto:

| Acción | Mecanismo | Por qué |
|--------|-----------|---------|
| Mover (WASD/flechas) | *Polling* cada frame | Mantener la tecla debe mover continuamente |
| Disparar (espacio) | Detección por **flanco** | Una bala por pulsación, no un chorro |
| Pausar/Finalizar (ESC/Q) | **Evento** del Home | Son acciones del lobby, ocurren una vez |

### El "truco del reloj" (espera sin congelar)
Processing no permite "esperar 600 ms parado" (congelaría todo). En su lugar se guarda
la hora de inicio y **cada frame se compara** `ahora - inicio >= límite`. Se usa para el
splash de carga (`TIEMPO_CARGA_MS = 600`) y para el game over (`GAME_OVER_MS = 2500`).

### `app.key = 0` (consumir ESC)
Processing cierra el sketch si detecta ESC. Después de usar ESC para pausar, se hace
`app.key = 0` (asignación, no comparación) para **borrar** la tecla y que Processing no
cierre la ventana.

### Persistencia acumulativa
Al guardar, el repositorio **lee** el JSON anterior, **suma** los valores y reescribe.
Por eso las estadísticas persisten y crecen entre partidas/sesiones. Como es un survival,
cada partida aporta `1 jugada / 0 ganadas / 1 perdida`.

---

## 5. Preguntas frecuentes y cómo responderlas

**¿Por qué `AvionSkyhawk` es `.pde` y no `.java`?**
Para que nuestras clases (`Skyhawk`, `Enemigo`, …) queden como *inner classes* del
sketch y puedan usar las funciones globales de Processing (`image`, `rect`, `width`…)
sin prefijar `app.`.

**¿Por qué `Skyhawk.actualizar()` está vacío?**
El movimiento ya lo aplica `mover()` cuando se lee el teclado. El método existe por el
contrato heredado de `Nave` (todas las naves se actualizan en el loop).

**¿Qué pasa si el módulo nunca notifica `FINALIZADO`?**
El Home nunca vuelve al menú ni guarda las estadísticas. Por eso el game over termina
llamando a `finalizar()`.

**¿Cómo se evita disparar dos veces o finalizar dos veces?**
- Disparo: detección por **flanco** (`espacioAhora && !espacioAntes`).
- Finalizar: el patrón **State** actúa de guardia — el módulo ya está en `FINALIZADO`,
  así que el Home consulta el estado y no vuelve a finalizar. Además se **desuscribe**
  del observer antes, para evitar recursión.

**¿Por qué un enum `Direccion` y no números?**
Legibilidad y **desacople**: `leerTeclado()` traduce tecla → `Direccion` y `mover()`
traduce `Direccion` → coordenadas. Cambiar el control (joystick, mouse) no obliga a
tocar `mover()`.

**¿Cómo se detectan las colisiones?**
Por distancia simple: `dist(x1, y1, x2, y2) < 30` en `Nave.colisionaCon()`.

---

## 6. Mejoras pendientes (para la sección "posibles mejoras" de la defensa)

Mostrar que conocés los límites del trabajo suma puntos:

1. **Límites de pantalla del avión:** el sprite se dibuja centrado (90×60) pero el
   *clamp* es contra `0`/`width`/`height`, así que puede salirse media imagen por el
   borde. Faltaría restar/sumar el medio ancho.
2. **`Nave` no inicializa `vida`:** funciona solo porque cada subclase la setea; es
   frágil. Convendría inicializarla en la base o por constructor.
3. **`loadImage` por instancia:** cada `Skyhawk`/`Enemigo` recarga el PNG. Se podría
   cargar el sprite una sola vez y compartirlo.
4. **Dificultad fija:** velocidad y frecuencia de disparo de enemigos son constantes;
   se podría escalar con el puntaje.
5. **Usar el `ContextoJuego`:** el juego usa los globales `width`/`height` en vez de
   `contexto.getAnchoPantalla()/getAltoPantalla()` como recomienda el contrato.

---

## 7. Glosario rápido

- **Sketch:** un programa de Processing. El nuestro es `Game1982`.
- **Frame / loop:** Processing ejecuta `draw()` ~60 veces por segundo.
- **Polling:** consultar el estado (del teclado) en cada frame.
- **Flanco:** transición de una señal (tecla no-apretada → apretada).
- **Patrón State:** cada estado decide qué transiciones permite.
- **Patrón Observer:** un objeto (módulo) notifica eventos a sus suscriptores (Home).
- **Patrón Adapter:** una clase traduce una interfaz a otra (AvionSkyhawk ↔ GameController).
- **Survival:** modo sin condición de victoria; se juega hasta morir.
