import processing.core.*;
import java.util.*;

public class ModuloPrueba implements ModuloJuego {
  private static final int DURACION_MS = 5000;

  private EstadoJuego estadoActual;
  private ContextoJuego contexto;
  private List<IModuloObserver> observers;
  private long tiempoInicio;
  private int puntaje;
  private long tiempoInicioCarga;
  private static final int TIEMPO_CARGA_MS = 800;

  public ModuloPrueba() {
    estadoActual = new NoIniciadoState();
    observers = new ArrayList<>();
    puntaje = 0;
  }

  public String getNombreModulo() { return "Prueba"; }
  public String getDescripcion() { return "Módulo de prueba del sistema Home"; }
  public String getNombreAvion() { return "A-4 SKYHAWK"; }

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
    boolean gano = puntaje > 100;
    return new EstadisticasGenerales(
        getNombreModulo(),
        puntaje,
        1,
        gano ? 1 : 0,
        gano ? 0 : 1,
        puntaje / 10,
        tiempoSeg
    );
  }

  public void reset() {
    estadoActual = new NoIniciadoState();
    tiempoInicio = 0;
    tiempoInicioCarga = 0;
    puntaje = 0;
    observers.clear();
  }

  public void agregarObserver(IModuloObserver obs) { observers.add(obs); }
  public void removerObserver(IModuloObserver obs) { observers.remove(obs); }

  private void notificar(ModuloEvento evento) {
    for (IModuloObserver obs : new ArrayList<>(observers)) obs.onEventoModulo(evento);
  }

  public void actualizar(PApplet app) {
    String estado = estadoActual.getNombre();
    if ("INICIANDO".equals(estado)) {
      if (System.currentTimeMillis() - tiempoInicioCarga >= TIEMPO_CARGA_MS) {
        estadoActual = new EnEjecucionState();
        tiempoInicio = System.currentTimeMillis();
        puntaje = 0;
        notificar(new ModuloEvento(ModuloEvento.Tipo.INICIADO, getNombreModulo(), "En ejecucion"));
      }
    } else if ("EN_EJECUCION".equals(estado)) {
      long elapsed = System.currentTimeMillis() - tiempoInicio;
      puntaje = (int)(elapsed / 50);
      if (elapsed >= DURACION_MS) {
        try { finalizar(); } catch (EstadoInvalidoException e) { /* no ocurre */ }
      }
    }
  }

  public void dibujar(PApplet app) {
    String estado = estadoActual.getNombre();
    app.background(0);

    app.fill(0, 255, 0);
    app.textSize(14);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.text("A-4 SKYHAWK  -  MODULO DE PRUEBA", app.width / 2f, app.height * 0.15f);

    app.fill(0, 120, 0);
    app.textSize(9);
    app.text("ESTADO: " + estado, app.width / 2f, app.height * 0.26f);

    if ("INICIANDO".equals(estado)) {
      long elapsed = System.currentTimeMillis() - tiempoInicioCarga;
      float progreso = PApplet.constrain((float) elapsed / TIEMPO_CARGA_MS, 0, 1);

      app.fill(0, 120, 0);
      app.textSize(9);
      app.text("CARGANDO RECURSOS...", app.width / 2f, app.height * 0.45f);

      float bw = 400, bh = 20;
      float bx = app.width / 2f - bw / 2f, by = app.height * 0.52f;
      app.stroke(0, 120, 0);
      app.noFill();
      app.rect(bx, by, bw, bh);
      app.noStroke();
      app.fill(0, 200, 0);
      app.rect(bx, by, bw * progreso, bh);

    } else if ("EN_EJECUCION".equals(estado)) {
      long elapsed = System.currentTimeMillis() - tiempoInicio;
      float progreso = PApplet.constrain((float) elapsed / DURACION_MS, 0, 1);

      app.fill(0, 200, 0);
      app.textSize(11);
      app.text("PUNTAJE: " + puntaje, app.width / 2f, app.height * 0.40f);

      app.fill(0, 120, 0);
      app.textSize(8);
      app.text("TIEMPO: " + (elapsed / 1000) + "s / " + (DURACION_MS / 1000) + "s", app.width / 2f, app.height * 0.50f);

      float bw = 400, bh = 16;
      float bx = app.width / 2f - bw / 2f, by = app.height * 0.57f;
      app.stroke(0, 120, 0);
      app.noFill();
      app.rect(bx, by, bw, bh);
      app.noStroke();
      app.fill(0, 200, 0);
      app.rect(bx, by, bw * progreso, bh);

      app.fill(0, 120, 0);
      app.textSize(8);
      app.text("ESC pausar  |  Q finalizar", app.width / 2f, app.height * 0.88f);

    } else if ("PAUSADO".equals(estado)) {
      app.fill(0, 255, 0);
      app.textSize(16);
      app.text("-- PAUSA --", app.width / 2f, app.height * 0.50f);
      app.fill(0, 120, 0);
      app.textSize(8);
      app.text("ESC para reanudar  |  Q para finalizar", app.width / 2f, app.height * 0.62f);
    }
  }
}
