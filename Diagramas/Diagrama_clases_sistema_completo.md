# Diagrama de clases — Sistema completo (Lobby + Módulo Skyhawk)

> Incluye **todas** las clases del proyecto: las del **lobby/Home** (`.java`) y las
> del **módulo Skyhawk 1942** (`.pde`). Está derivado 100% del código fuente.
> Para el diagrama enfocado solo en el módulo, ver
> [Diagrama_clases_completo.md](Diagrama_clases_completo.md).

## Leyenda de visibilidad
- `+` público · `-` privado · `#` protegido · `$` estático
- En el lobby (`.java`) los atributos son `private` y se leen con **getters públicos**.
  En el módulo (`.pde`), los métodos de comportamiento (`actualizar()`, `dibujar()`, …)
  no llevan modificador → **acceso de paquete** (sin símbolo).
- Constantes `static final`: `DURACION_MS`, `TIEMPO_CARGA_MS`, `GAME_OVER_MS`.

## Cómo está organizado el sistema
1. **`Game1982` (sketch)** es el punto de entrada: crea el `HomeJuego` y le registra
   los módulos (`ModuloPrueba` y `AvionSkyhawk`).
2. **Lobby / Home**: navegación entre pantallas, gestión de módulos y persistencia de
   estadísticas. Define los **contratos** `ModuloJuego`, `EstadoJuego`, `IModuloObserver`.
3. **Módulo Skyhawk**: `AvionSkyhawk` implementa `ModuloJuego` y delega la lógica del
   juego en `GameController` (avión, enemigos y balas).

---

```mermaid
classDiagram
    direction TB

    %% ========================================================================
    %% LOBBY / HOME — contratos
    %% ========================================================================
    class ModuloJuego {
        <<interface>>
        +getNombreModulo() String
        +getDescripcion() String
        +getNombreAvion() String
        +inicializarContexto(ContextoJuego ctx) void
        +iniciar() void
        +pausar() void
        +reanudar() void
        +finalizar() void
        +getEstado() EstadoJuego
        +getEstadisticasGenerales() EstadisticasGenerales
        +agregarObserver(IModuloObserver o) void
        +removerObserver(IModuloObserver o) void
        +actualizar(PApplet app) void
        +dibujar(PApplet app) void
        +reset() void
    }

    class EstadoJuego {
        <<interface>>
        +iniciar(ModuloJuego m) void
        +pausar(ModuloJuego m) void
        +reanudar(ModuloJuego m) void
        +finalizar(ModuloJuego m) void
        +getNombre() String
    }

    class IModuloObserver {
        <<interface>>
        +onEventoModulo(ModuloEvento evento) void
    }

    %% ========================================================================
    %% LOBBY / HOME — modelos de datos
    %% ========================================================================
    class ContextoJuego {
        -String nombreJugador
        -int anchoPantalla
        -int altoPantalla
        +ContextoJuego(String n, int ancho, int alto)
        +getNombreJugador() String
        +getAnchoPantalla() int
        +getAltoPantalla() int
    }

    class ModuloEvento {
        -Tipo tipo
        -String nombreModulo
        -String mensaje
        +ModuloEvento(Tipo t, String nom)
        +ModuloEvento(Tipo t, String nom, String msg)
        +getTipo() Tipo
        +getNombreModulo() String
        +getMensaje() String
    }
    class Tipo {
        <<enumeration>>
        INICIADO
        PAUSADO
        REANUDADO
        FINALIZADO
        ERROR
    }

    class EstadisticasGenerales {
        -String nombreModulo
        -int puntajeTotal
        -int partidasJugadas
        -int partidasGanadas
        -int partidasPerdidas
        -int enemigosDestruidos
        -long tiempoJugadoSegundos
        +EstadisticasGenerales(...)
        +getNombreModulo() String
        +getPuntajeTotal() int
        +getPartidasJugadas() int
        +getPartidasGanadas() int
        +getPartidasPerdidas() int
        +getEnemigosDestruidos() int
        +getTiempoJugadoSegundos() long
    }

    %% ========================================================================
    %% LOBBY / HOME — orquestación y navegación
    %% ========================================================================
    class HomeJuego {
        -GestorModulos gestorModulos
        -GestorEstadisticas gestorEstadisticas
        -ControladorNavegacion controladorNav
        -PantallaInicio pantallaInicio
        -PantallaSeleccion pantallaSeleccion
        -PantallaEstadisticas pantallaEstadisticas
        -ModuloJuego moduloActual
        -int tiempoJuegoFrames
        -PApplet app
        +HomeJuego(PApplet app)
        +registrarModulo(ModuloJuego m) void
        +iniciarHome() void
        +dibujar() void
        +manejarClick(float mx, float my) void
        +manejarTecla(int keyCode, char key) void
        +seleccionarModulo(String nombre) void
        +mostrarEstadisticasGenerales() void
        +getPantallaActualNombre() String
        +salir() void
        +onEventoModulo(ModuloEvento evento) void
        -mostrarMenuPrincipal() void
        -finalizarModuloActual() void
    }

    class GestorModulos {
        -List~ModuloJuego~ modulos
        +GestorModulos()
        +agregarModulo(ModuloJuego m) void
        +eliminarModulo(ModuloJuego m) void
        +buscarModulo(String nombre) ModuloJuego
        +listarModulos() List~ModuloJuego~
    }

    class ControladorNavegacion {
        -Pantalla pantallaActual
        +ControladorNavegacion()
        +irHome() void
        +irSeleccionModulo() void
        +irEstadisticas() void
        +iniciarModulo(ModuloJuego m) void
        +getPantallaActual() Pantalla
    }

    class Pantalla {
        <<enumeration>>
        INICIO
        SELECCION
        ESTADISTICAS
        JUEGO
    }

    %% ========================================================================
    %% LOBBY / HOME — pantallas (UI)
    %% ========================================================================
    class PantallaInicio {
        -Boton botonStart
        +PantallaInicio(int ancho, int alto)
        +dibujar(PApplet app) void
        +clicEnStart(float mx, float my) boolean
    }

    class PantallaSeleccion {
        -List~Boton~ botonesModulos
        -List~String~ nombresModulos
        -Boton botonEstadisticas
        -Boton botonVolver
        -List~Boton~ todosBotones
        -int indiceSeleccion
        -String mensajeError
        +PantallaSeleccion(int ancho, int alto)
        +setModulos(List~ModuloJuego~ m, int ancho, int alto) void
        +moverArriba() void
        +moverAbajo() void
        +confirmarSeleccion() String
        +setMensajeError(String msg) void
        +dibujar(PApplet app) void
        +clicEnModulo(float mx, float my) String
        +clicEnEstadisticas(float mx, float my) boolean
        +clicEnVolver(float mx, float my) boolean
        -actualizarSeleccion() void
    }

    class PantallaEstadisticas {
        -Boton botonVolver
        +PantallaEstadisticas(int ancho, int alto)
        +dibujar(PApplet app, List~EstadisticasGenerales~ stats, int total) void
        +clicEnVolver(float mx, float my) boolean
    }

    class Boton {
        -float x
        -float y
        -float ancho
        -float alto
        -String texto
        -boolean seleccionado
        +Boton(float x, float y, float ancho, float alto, String texto)
        +dibujar(PApplet app) void
        +estaEncima(float mx, float my) boolean
        +setSeleccionado(boolean s) void
        +isSeleccionado() boolean
        +getTexto() String
        +getX() float
        +getY() float
        +getAncho() float
        +getAlto() float
    }

    %% ========================================================================
    %% LOBBY / HOME — estadísticas y persistencia
    %% ========================================================================
    class GestorEstadisticas {
        -RepositorioEstadisticas repositorio
        +GestorEstadisticas(RepositorioEstadisticas repo)
        +guardarEstadisticas(EstadisticasGenerales s) void
        +obtenerPorModulo(String nombre) EstadisticasGenerales
        +obtenerTodas() List~EstadisticasGenerales~
        +calcularPuntajeTotalCurso() int
    }

    class RepositorioEstadisticas {
        <<interface>>
        +guardar(String nombre, EstadisticasGenerales s) void
        +cargar(String nombre) EstadisticasGenerales
        +cargarTodas() List~EstadisticasGenerales~
    }

    class RepositorioEstadisticasArchivo {
        -String carpeta
        +RepositorioEstadisticasArchivo(String carpeta)
        +guardar(String nombre, EstadisticasGenerales s) void
        +cargar(String nombre) EstadisticasGenerales
        +cargarTodas() List~EstadisticasGenerales~
        -aJson(EstadisticasGenerales s) String
        -desdeJson(String json) EstadisticasGenerales
        -extraerString(String json, String clave) String
        -extraerInt(String json, String clave) int
        -extraerLong(String json, String clave) long
    }

    %% ========================================================================
    %% LOBBY / HOME — estados (patrón State)
    %% ========================================================================
    class NoIniciadoState {
        +getNombre() String
    }
    class IniciandoState {
        +getNombre() String
    }
    class EnEjecucionState {
        +getNombre() String
    }
    class PausadoState {
        +getNombre() String
    }
    class FinalizadoState {
        +getNombre() String
    }
    class ErrorState {
        +getNombre() String
    }

    %% ========================================================================
    %% LOBBY / HOME — excepciones
    %% ========================================================================
    class JuegoException {
        <<abstract>>
        +JuegoException(String msg)
        +JuegoException(String msg, Throwable causa)
    }
    class EstadoInvalidoException {
        +EstadoInvalidoException(String msg)
    }
    class ModuloNoEncontradoException {
        +ModuloNoEncontradoException(String nombreModulo)
    }
    class PersistenciaException {
        +PersistenciaException(String msg)
        +PersistenciaException(String msg, Throwable causa)
    }

    %% ========================================================================
    %% MÓDULO DE PRUEBA (de ejemplo, provisto por el lobby)
    %% ========================================================================
    class ModuloPrueba {
        -EstadoJuego estadoActual
        -ContextoJuego contexto
        -List~IModuloObserver~ observers
        -long tiempoInicio
        -int puntaje
        -long tiempoInicioCarga
        -int DURACION_MS$
        -int TIEMPO_CARGA_MS$
        +ModuloPrueba()
        +getNombreModulo() String
        +getDescripcion() String
        +getNombreAvion() String
        +inicializarContexto(ContextoJuego ctx) void
        +iniciar() void
        +pausar() void
        +reanudar() void
        +finalizar() void
        +getEstado() EstadoJuego
        +getEstadisticasGenerales() EstadisticasGenerales
        +reset() void
        +agregarObserver(IModuloObserver o) void
        +removerObserver(IModuloObserver o) void
        +actualizar(PApplet app) void
        +dibujar(PApplet app) void
        -notificar(ModuloEvento e) void
    }

    %% ========================================================================
    %% MÓDULO SKYHAWK — adaptador con el lobby
    %% ========================================================================
    class AvionSkyhawk {
        -EstadoJuego estadoActual
        -ContextoJuego contexto
        -List~IModuloObserver~ observers
        -GameController game
        -long tiempoInicio
        -long tiempoInicioCarga
        -long tiempoMuerte
        -boolean muerto
        -boolean espacioAntes
        -int TIEMPO_CARGA_MS$
        -int GAME_OVER_MS$
        +AvionSkyhawk()
        +getNombreModulo() String
        +getDescripcion() String
        +getNombreAvion() String
        +inicializarContexto(ContextoJuego ctx) void
        +iniciar() void
        +pausar() void
        +reanudar() void
        +finalizar() void
        +getEstado() EstadoJuego
        +getEstadisticasGenerales() EstadisticasGenerales
        +reset() void
        +agregarObserver(IModuloObserver o) void
        +removerObserver(IModuloObserver o) void
        +actualizar(PApplet app) void
        +dibujar(PApplet app) void
        -notificar(ModuloEvento e) void
        -dibujarSplash(PApplet app) void
    }

    %% ========================================================================
    %% MÓDULO SKYHAWK — lógica del juego
    %% ========================================================================
    class GameController {
        -Skyhawk skyhawk
        -ArrayList~Enemigo~ enemigos
        -ArrayList~ProyectilSkyhawk~ balasJugador
        -ArrayList~ProyectilEnemigo~ balasEnemigo
        -int puntaje
        -int enemigosDerribados
        GameController()
        crearEnemigos() void
        dispararSkyhawk() void
        jugadorVivo() boolean
        actualizar() void
        detectarColisiones() void
        leerTeclado() void
        dibujar() void
        +getPuntaje() int
        +getEnemigosDerribados() int
    }

    class Direccion {
        <<enumeration>>
        ARRIBA
        ABAJO
        IZQUIERDA
        DERECHA
    }

    class Nave {
        #int x
        #int y
        #int vida
        Nave(int xPos, int yPos)
        actualizar() void
        dibujar() void
        colisionaCon(int otroX, int otroY) boolean
        recibirDanio(int cuanto) void
        estaViva() boolean
        +getX() int
        +getY() int
        +getVida() int
    }

    class Skyhawk {
        -int velocidad
        -PImage sprite
        Skyhawk(int xPos, int yPos)
        actualizar() void
        mover(Direccion d) void
        dibujar() void
    }

    class Enemigo {
        -int velocidad
        -PImage sprite
        Enemigo(int xPos, int yPos)
        actualizar() void
        intentaDisparar() boolean
        reaparecer() void
        dibujar() void
    }

    class Proyectil {
        #int x
        #int y
        #int velocidad
        Proyectil(int xPos, int yPos)
        actualizar() void
        dibujar() void
        +getX() int
        +getY() int
    }

    class ProyectilSkyhawk {
        ProyectilSkyhawk(int xPos, int yPos)
        actualizar() void
        dibujar() void
    }

    class ProyectilEnemigo {
        ProyectilEnemigo(int xPos, int yPos)
        actualizar() void
        dibujar() void
    }

    %% ========================================================================
    %% SKETCH (punto de entrada — no es una clase, es el .pde principal)
    %% ========================================================================
    class Game1982 {
        <<sketch>>
        ~HomeJuego homeJuego
        setup() void
        draw() void
        mousePressed() void
        keyPressed() void
    }

    %% ---- Herencia ----
    Exception <|-- JuegoException
    JuegoException <|-- EstadoInvalidoException
    JuegoException <|-- ModuloNoEncontradoException
    JuegoException <|-- PersistenciaException
    Nave <|-- Skyhawk
    Nave <|-- Enemigo
    Proyectil <|-- ProyectilSkyhawk
    Proyectil <|-- ProyectilEnemigo

    %% ---- Realización de interfaces ----
    AvionSkyhawk ..|> ModuloJuego
    ModuloPrueba ..|> ModuloJuego
    HomeJuego ..|> IModuloObserver
    RepositorioEstadisticasArchivo ..|> RepositorioEstadisticas
    NoIniciadoState ..|> EstadoJuego
    IniciandoState ..|> EstadoJuego
    EnEjecucionState ..|> EstadoJuego
    PausadoState ..|> EstadoJuego
    FinalizadoState ..|> EstadoJuego
    ErrorState ..|> EstadoJuego

    %% ---- Anidamiento (enum interno) ----
    ModuloEvento *-- Tipo

    %% ---- Composición / agregación (Home) ----
    Game1982 *-- "1" HomeJuego : homeJuego
    HomeJuego *-- "1" GestorModulos
    HomeJuego *-- "1" GestorEstadisticas
    HomeJuego *-- "1" ControladorNavegacion
    HomeJuego *-- "1" PantallaInicio
    HomeJuego *-- "1" PantallaSeleccion
    HomeJuego *-- "1" PantallaEstadisticas
    HomeJuego --> "0..1" ModuloJuego : moduloActual
    GestorModulos o-- "*" ModuloJuego : modulos
    ControladorNavegacion --> Pantalla : pantallaActual
    PantallaInicio *-- "1" Boton : botonStart
    PantallaSeleccion *-- "*" Boton : botones
    PantallaEstadisticas *-- "1" Boton : botonVolver
    GestorEstadisticas --> "1" RepositorioEstadisticas : repositorio

    %% ---- Composición (módulos) ----
    AvionSkyhawk *-- "1" GameController : game
    AvionSkyhawk --> EstadoJuego : estadoActual
    AvionSkyhawk o-- "*" IModuloObserver : observers
    AvionSkyhawk --> "0..1" ContextoJuego : contexto
    ModuloPrueba --> EstadoJuego : estadoActual
    ModuloPrueba o-- "*" IModuloObserver : observers
    ModuloPrueba --> "0..1" ContextoJuego : contexto
    GameController *-- "1" Skyhawk : skyhawk
    GameController *-- "*" Enemigo : enemigos
    GameController *-- "*" ProyectilSkyhawk : balasJugador
    GameController *-- "*" ProyectilEnemigo : balasEnemigo

    %% ---- Dependencias (usa / crea / lanza) ----
    Game1982 ..> ModuloPrueba : registra
    Game1982 ..> AvionSkyhawk : registra
    HomeJuego ..> ContextoJuego : crea
    HomeJuego ..> EstadisticasGenerales : usa
    HomeJuego ..> ModuloEvento : recibe
    GestorEstadisticas ..> EstadisticasGenerales : usa
    RepositorioEstadisticasArchivo ..> EstadisticasGenerales : crea
    RepositorioEstadisticasArchivo ..> PersistenciaException : throws
    GestorModulos ..> ModuloNoEncontradoException : throws
    PantallaSeleccion ..> ModuloJuego : lee nombres
    PantallaEstadisticas ..> EstadisticasGenerales : muestra
    AvionSkyhawk ..> ModuloEvento : crea
    AvionSkyhawk ..> EstadisticasGenerales : crea
    AvionSkyhawk ..> EstadoInvalidoException : throws
    ModuloPrueba ..> ModuloEvento : crea
    ModuloPrueba ..> EstadisticasGenerales : crea
    EstadoJuego ..> EstadoInvalidoException : throws
    Skyhawk ..> Direccion : usa
    GameController ..> Direccion : usa
```

---

## Notas para la defensa

### Lobby / Home (lo que ya estaba dado)
- **`Game1982`** (sketch principal) crea el `HomeJuego`, lo inicializa y le **registra
  los módulos**: `ModuloPrueba` y `AvionSkyhawk`. Reenvía los eventos de Processing
  (`mousePressed`, `keyPressed`, `draw`) al `HomeJuego`.
- **`HomeJuego`** es el orquestador del lobby. Mantiene las tres pantallas, el
  `ControladorNavegacion`, el `GestorModulos` y el `GestorEstadisticas`. Implementa
  `IModuloObserver`: cuando un módulo emite `FINALIZADO`/`ERROR`, guarda las stats y
  vuelve al menú.
- **Navegación**: `ControladorNavegacion` guarda en qué `Pantalla` (enum) estamos
  (`INICIO`, `SELECCION`, `ESTADISTICAS`, `JUEGO`).
- **Pantallas**: `PantallaInicio`, `PantallaSeleccion` y `PantallaEstadisticas` se
  componen de `Boton`s y solo dibujan/detectan clics; no tienen lógica de negocio.
- **Persistencia**: `GestorEstadisticas` usa la interfaz `RepositorioEstadisticas`
  (patrón **Repository**); la implementación `RepositorioEstadisticasArchivo`
  serializa a JSON en disco y **acumula** los datos de partidas anteriores.

### Patrones de diseño
- **State**: `EstadoJuego` + `NoIniciadoState` / `IniciandoState` / `EnEjecucionState`
  / `PausadoState` / `FinalizadoState` / `ErrorState`. Cada estado decide qué
  transiciones son válidas y lanza `EstadoInvalidoException` cuando no lo son.
- **Observer**: `IModuloObserver` + `ModuloEvento`. Los módulos notifican; el
  `HomeJuego` reacciona.
- **Adapter**: `AvionSkyhawk` adapta nuestro `GameController` al contrato `ModuloJuego`.
- **Repository**: `RepositorioEstadisticas` / `RepositorioEstadisticasArchivo`.

### Módulo Skyhawk (lo que construimos nosotros)
- **`AvionSkyhawk`** implementa `ModuloJuego` (igual que `ModuloPrueba`) y delega toda
  la lógica de la partida en **`GameController`**.
- **`GameController`** contiene el `Skyhawk`, la lista de `Enemigo` y las dos listas de
  balas; lee el teclado, mueve todo y resuelve colisiones.
- **Herencia**: `Skyhawk` y `Enemigo` extienden `Nave`; `ProyectilSkyhawk` y
  `ProyectilEnemigo` extienden `Proyectil`.

### Jerarquía de excepciones
- `JuegoException` (abstracta, extiende `Exception`) es la base de
  `EstadoInvalidoException`, `ModuloNoEncontradoException` y `PersistenciaException`.

> **Nota Mermaid**: `Tipo` aparece como clase aparte porque es el `enum` anidado dentro
> de `ModuloEvento` (`ModuloEvento.Tipo`); la relación de composición lo refleja.
