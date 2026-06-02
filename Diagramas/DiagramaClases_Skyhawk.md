# Diagrama de clases — Módulo Skyhawk

Basado 100% en el código de los archivos `Skyhawk_*.pde`. Las clases del lobby
(`ModuloJuego`, `EstadoJuego`, etc.) se muestran solo como frontera/contrato,
porque `AvionSkyhawk` las usa pero no las define.

## Leyenda de visibilidad
- `+` público · `-` privado · `#` protegido · `$` estático
- **Atributos**: `private` (encapsulados) o `protected` cuando una subclase los
  hereda (`Nave` → `Skyhawk`/`Enemigo`, `Proyectil` → sus balas). El resto del
  juego los lee con **getters públicos** (`getX()`, `getPuntaje()`, ...).
- **(sin símbolo)** = acceso por defecto (de paquete): es lo que tienen los
  **métodos de comportamiento** (`actualizar()`, `dibujar()`, `mover()`, ...) porque
  en el código `.pde` no llevan modificador.
- `TIEMPO_CARGA_MS` (600) y `GAME_OVER_MS` (2500) son `static final` (constantes).

```mermaid
classDiagram
    %% ===== Frontera con el lobby (definidas fuera del modulo) =====
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
        +agregarObserver(IModuloObserver observer) void
        +removerObserver(IModuloObserver observer) void
        +actualizar(PApplet app) void
        +dibujar(PApplet app) void
        +reset() void
    }

    %% ===== Adaptador del modulo =====
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
        AvionSkyhawk()
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
        +agregarObserver(IModuloObserver obs) void
        +removerObserver(IModuloObserver obs) void
        -notificar(ModuloEvento evento) void
        +actualizar(PApplet app) void
        +dibujar(PApplet app) void
        -dibujarSplash(PApplet app) void
    }

    %% ===== Logica del juego =====
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

    %% ===== Naves =====
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
        mover(Direccion direccion) void
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

    %% ===== Proyectiles =====
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

    %% ===== Enum =====
    class Direccion {
        <<enumeration>>
        ARRIBA
        ABAJO
        IZQUIERDA
        DERECHA
    }

    %% ===== Relaciones =====
    AvionSkyhawk ..|> ModuloJuego : implements
    AvionSkyhawk "1" *-- "1" GameController : game

    Skyhawk --|> Nave
    Enemigo --|> Nave
    ProyectilSkyhawk --|> Proyectil
    ProyectilEnemigo --|> Proyectil

    GameController "1" *-- "1" Skyhawk : skyhawk
    GameController "1" *-- "*" Enemigo : enemigos
    GameController "1" *-- "*" ProyectilSkyhawk : balasJugador
    GameController "1" *-- "*" ProyectilEnemigo : balasEnemigo

    Skyhawk ..> Direccion : usa en mover()
    GameController ..> Direccion : usa en leerTeclado()
```
