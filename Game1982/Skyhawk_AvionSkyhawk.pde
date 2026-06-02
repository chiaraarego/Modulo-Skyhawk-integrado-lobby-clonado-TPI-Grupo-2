// AvionSkyhawk.pde
// Adaptador que conecta nuestro juego (GameController) con el lobby del grupo.
// Implementa el contrato ModuloJuego delegando toda la logica en GameController.
//
// Es .pde a proposito: asi nuestras clases (Skyhawk, Enemigo, Proyectil, ...)
// quedan como inner classes del sketch Game1982 y siguen usando las funciones
// globales de Processing (image, rect, width, height, key, keyPressed, ...)
// sin tener que prefijar nada con "app.".

import processing.core.PApplet;
import java.util.ArrayList;
import java.util.List;

class AvionSkyhawk implements ModuloJuego {

  private EstadoJuego estadoActual;
  private ContextoJuego contexto;
  private List<IModuloObserver> observers;

  private GameController game;        // nuestra partida (la logica del juego)
  private long tiempoInicio;          // marca para el tiempo jugado de las stats
  private long tiempoInicioCarga;     // marca para el splash de "cargando"
  private long tiempoMuerte;          // marca para mostrar GAME OVER un rato
  private boolean muerto;
  private boolean espacioAntes;       // estado anterior del espacio (disparo por flanco)

  private static final int TIEMPO_CARGA_MS = 600;
  private static final int GAME_OVER_MS = 2500;

  AvionSkyhawk() {
    estadoActual = new NoIniciadoState();
    observers = new ArrayList<IModuloObserver>();
  }

  // ---------- identidad ----------
  public String getNombreModulo() { return "Skyhawk 1942"; }
  public String getDescripcion()  { return "Shooter vertical estilo 1942"; }
  public String getNombreAvion()  { return "A-4 SKYHAWK"; }

  // ---------- ciclo de vida (patron State, igual que ModuloPrueba) ----------
  public void inicializarContexto(ContextoJuego ctx) { this.contexto = ctx; }

  public void iniciar() throws EstadoInvalidoException {
    estadoActual.iniciar(this);
    estadoActual = new IniciandoState();
    tiempoInicioCarga = System.currentTimeMillis();
    notificar(new ModuloEvento(ModuloEvento.Tipo.INICIADO, getNombreModulo(), "Cargando..."));
  }

  public void pausar() throws EstadoInvalidoException {
    estadoActual.pausar(this);
    estadoActual = new PausadoState();
    notificar(new ModuloEvento(ModuloEvento.Tipo.PAUSADO, getNombreModulo()));
  }

  public void reanudar() throws EstadoInvalidoException {
    estadoActual.reanudar(this);
    estadoActual = new EnEjecucionState();
    notificar(new ModuloEvento(ModuloEvento.Tipo.REANUDADO, getNombreModulo()));
  }

  public void finalizar() throws EstadoInvalidoException {
    estadoActual.finalizar(this);
    estadoActual = new FinalizadoState();
    notificar(new ModuloEvento(ModuloEvento.Tipo.FINALIZADO, getNombreModulo()));
  }

  public EstadoJuego getEstado() { return estadoActual; }

  public EstadisticasGenerales getEstadisticasGenerales() {
    long tiempoSeg = tiempoInicio > 0
        ? (System.currentTimeMillis() - tiempoInicio) / 1000
        : 0;
    int puntaje = (game != null) ? game.getPuntaje() : 0;
    int derribados = (game != null) ? game.getEnemigosDerribados() : 0;
    // Es un survival: no hay condicion de victoria, asi que cada partida
    // terminada cuenta como jugada (1) y perdida (1).
    return new EstadisticasGenerales(
        getNombreModulo(),
        puntaje,
        1,            // partidas jugadas
        0,            // partidas ganadas
        1,            // partidas perdidas
        derribados,   // enemigos destruidos
        tiempoSeg);   // tiempo jugado en segundos
  }

  public void reset() {
    estadoActual = new NoIniciadoState();
    contexto = null;
    game = null;
    tiempoInicio = 0;
    tiempoInicioCarga = 0;
    tiempoMuerte = 0;
    muerto = false;
    espacioAntes = false;
    observers.clear();
  }

  public void agregarObserver(IModuloObserver obs) { observers.add(obs); }
  public void removerObserver(IModuloObserver obs) { observers.remove(obs); }

  private void notificar(ModuloEvento evento) {
    for (IModuloObserver obs : new ArrayList<IModuloObserver>(observers)) {
      obs.onEventoModulo(evento);
    }
  }

  // ---------- loop del juego ----------
  public void actualizar(PApplet app) {
    String estado = estadoActual.getNombre();

    if ("INICIANDO".equals(estado)) {
      // Splash breve y luego "cargamos" la partida (crea el avion y sus sprites).
      if (System.currentTimeMillis() - tiempoInicioCarga >= TIEMPO_CARGA_MS) {
        game = new GameController();
        estadoActual = new EnEjecucionState();
        tiempoInicio = System.currentTimeMillis();
        muerto = false;
        espacioAntes = false;
        notificar(new ModuloEvento(ModuloEvento.Tipo.INICIADO, getNombreModulo(), "En ejecucion"));
      }

    } else if ("EN_EJECUCION".equals(estado)) {
      if (game == null) return;

      if (game.jugadorVivo()) {
        // Disparo de a una bala: detectamos el flanco (transicion no-presionado ->
        // presionado) de la barra espaciadora, porque el Home no nos pasa esta
        // tecla (en el juego solo intercepta ESC y Q).
        boolean espacioAhora = app.keyPressed && app.key == ' ';
        if (espacioAhora && !espacioAntes) {
          game.dispararSkyhawk();
        }
        espacioAntes = espacioAhora;

        // El movimiento (WASD / flechas) lo lee game.actualizar() directamente
        // desde las variables globales del sketch (keyPressed, key, keyCode).
        game.actualizar();
      } else {
        // Game over: congelamos la escena y, tras unos segundos, finalizamos
        // para que el Home guarde las estadisticas y vuelva al menu.
        if (!muerto) {
          muerto = true;
          tiempoMuerte = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - tiempoMuerte >= GAME_OVER_MS) {
          try { finalizar(); } catch (EstadoInvalidoException e) { /* ya finalizado */ }
        }
      }
    }
    // PAUSADO: no actualizamos la logica del juego (queda congelado).
  }

  public void dibujar(PApplet app) {
    String estado = estadoActual.getNombre();

    // pushStyle/popStyle aisla nuestros modos y estilos del resto del lobby:
    // al volver al menu, sus pantallas recuperan su rectMode/imageMode/fill.
    app.pushStyle();
    app.background(0);
    app.imageMode(PApplet.CENTER);    // el setup del lobby no setea estos modos,
    app.rectMode(PApplet.CENTER);     // y nuestras clases dibujan centrado en (x, y)
    app.ellipseMode(PApplet.CENTER);

    if ("INICIANDO".equals(estado)) {
      dibujarSplash(app);

    } else if ("EN_EJECUCION".equals(estado)) {
      if (game != null) {
        game.dibujar();
      }
      if (muerto) {
        app.fill(255, 0, 0);
        app.textAlign(PApplet.CENTER, PApplet.CENTER);
        app.textSize(40);
        app.text("GAME OVER", app.width / 2f, app.height / 2f);
      }

    } else if ("PAUSADO".equals(estado)) {
      if (game != null) {
        game.dibujar();               // escena congelada de fondo
      }
      app.fill(0, 200, 0);
      app.textAlign(PApplet.CENTER, PApplet.CENTER);
      app.textSize(24);
      app.text("-- PAUSA --", app.width / 2f, app.height * 0.45f);
      app.textSize(10);
      app.text("ESC reanudar  |  Q finalizar", app.width / 2f, app.height * 0.55f);
    }

    app.popStyle();
  }

  private void dibujarSplash(PApplet app) {
    app.fill(0, 200, 0);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.textSize(16);
    app.text("A-4 SKYHAWK", app.width / 2f, app.height * 0.40f);
    app.textSize(9);
    app.text("CARGANDO...", app.width / 2f, app.height * 0.52f);
  }
}
