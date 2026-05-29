import processing.core.*;
import java.util.*;

public class HomeJuego implements IModuloObserver {
  private GestorModulos gestorModulos;
  private GestorEstadisticas gestorEstadisticas;
  private ControladorNavegacion controladorNav;
  private PantallaInicio pantallaInicio;
  private PantallaSeleccion pantallaSeleccion;
  private PantallaEstadisticas pantallaEstadisticas;
  private ModuloJuego moduloActual;
  private int tiempoJuegoFrames;
  private PApplet app;

  public HomeJuego(PApplet app) {
    this.app = app;
  }

  public void registrarModulo(ModuloJuego modulo) {
    if (gestorModulos == null) {
      throw new IllegalStateException("Llamar iniciarHome() antes de registrarModulo()");
    }
    gestorModulos.agregarModulo(modulo);
    if (pantallaSeleccion != null) {
      pantallaSeleccion.setModulos(gestorModulos.listarModulos(), app.width, app.height);
    }
  }

  public void iniciarHome() {
    gestorModulos = new GestorModulos();
    gestorEstadisticas = new GestorEstadisticas(
        new RepositorioEstadisticasArchivo(app.sketchPath("estadisticas")));
    controladorNav = new ControladorNavegacion();
    pantallaInicio = new PantallaInicio(app.width, app.height);
    pantallaSeleccion = new PantallaSeleccion(app.width, app.height);
    pantallaEstadisticas = new PantallaEstadisticas(app.width, app.height);
    tiempoJuegoFrames = 0;
  }

  public void dibujar() {
    Pantalla actual = controladorNav.getPantallaActual();

    if (actual == Pantalla.INICIO) {
      pantallaInicio.dibujar(app);

    } else if (actual == Pantalla.SELECCION) {
      pantallaSeleccion.dibujar(app);

    } else if (actual == Pantalla.ESTADISTICAS) {
      List<EstadisticasGenerales> todas = gestorEstadisticas.obtenerTodas();
      int total = gestorEstadisticas.calcularPuntajeTotalCurso();
      pantallaEstadisticas.dibujar(app, todas, total);

    } else if (actual == Pantalla.JUEGO) {
      if (moduloActual != null) {
        String estadoNombre = moduloActual.getEstado().getNombre();
        if ("INICIANDO".equals(estadoNombre) ||
            "EN_EJECUCION".equals(estadoNombre) ||
            "PAUSADO".equals(estadoNombre)) {
          moduloActual.actualizar(app);
          if (moduloActual != null) {
            moduloActual.dibujar(app);
          }
        }
      }
    }
  }

  public void manejarClick(float mx, float my) {
    Pantalla actual = controladorNav.getPantallaActual();

    if (actual == Pantalla.INICIO) {
      if (pantallaInicio.clicEnStart(mx, my)) {
        mostrarMenuPrincipal();
      }

    } else if (actual == Pantalla.SELECCION) {
      String modulo = pantallaSeleccion.clicEnModulo(mx, my);
      if (modulo != null) {
        seleccionarModulo(modulo);
        return;
      }
      if (pantallaSeleccion.clicEnEstadisticas(mx, my)) {
        mostrarEstadisticasGenerales();
        return;
      }
      if (pantallaSeleccion.clicEnVolver(mx, my)) {
        controladorNav.irHome();
      }

    } else if (actual == Pantalla.ESTADISTICAS) {
      if (pantallaEstadisticas.clicEnVolver(mx, my)) {
        controladorNav.irSeleccionModulo();
      }
    }
  }

  public void manejarTecla(int keyCode, char key) {
    Pantalla actual = controladorNav.getPantallaActual();

    if (actual == Pantalla.INICIO) {
      if (keyCode == PApplet.ENTER || key == '\n' || key == ' ') {
        mostrarMenuPrincipal();
      }

    } else if (actual == Pantalla.SELECCION) {
      if (key == 'w' || key == 'W' || keyCode == PApplet.UP) {
        pantallaSeleccion.moverArriba();
      } else if (key == 's' || key == 'S' || keyCode == PApplet.DOWN) {
        pantallaSeleccion.moverAbajo();
      } else if (keyCode == PApplet.ENTER || key == '\n') {
        String resultado = pantallaSeleccion.confirmarSeleccion();
        if ("ESTADISTICAS".equals(resultado)) {
          mostrarEstadisticasGenerales();
        } else if ("VOLVER".equals(resultado)) {
          controladorNav.irHome();
        } else if (resultado != null) {
          seleccionarModulo(resultado);
        }
      } else if (keyCode == PApplet.ESC) {
        app.key = 0; // consumir ESC para no cerrar Processing
        controladorNav.irHome();
      }

    } else if (actual == Pantalla.ESTADISTICAS) {
      if (keyCode == PApplet.ESC) {
        app.key = 0;
        controladorNav.irSeleccionModulo();
      } else if (keyCode == PApplet.ENTER || key == '\n') {
        controladorNav.irSeleccionModulo();
      }

    } else if (actual == Pantalla.JUEGO && moduloActual != null) {
      String estadoNombre = moduloActual.getEstado().getNombre();
      if (keyCode == PApplet.ESC) {
        app.key = 0;
        if ("EN_EJECUCION".equals(estadoNombre)) {
          try { moduloActual.pausar(); } catch (EstadoInvalidoException e) { /* ignorar */ }
        } else if ("PAUSADO".equals(estadoNombre)) {
          try { moduloActual.reanudar(); } catch (EstadoInvalidoException e) { /* ignorar */ }
        }
      } else if (key == 'q' || key == 'Q') {
        if ("EN_EJECUCION".equals(estadoNombre) || "PAUSADO".equals(estadoNombre)) {
          try { moduloActual.finalizar(); } catch (EstadoInvalidoException e) { /* ignorar */ }
        }
      }
    }
  }

  private void mostrarMenuPrincipal() {
    pantallaSeleccion.setModulos(gestorModulos.listarModulos(), app.width, app.height);
    controladorNav.irSeleccionModulo();
  }

  public void seleccionarModulo(String nombreModulo) {
    try {
      ModuloJuego modulo = gestorModulos.buscarModulo(nombreModulo);
      modulo.reset();
      modulo.agregarObserver(this);
      ContextoJuego ctx = new ContextoJuego("Jugador", app.width, app.height);
      modulo.inicializarContexto(ctx);
      modulo.iniciar();
      moduloActual = modulo;
      controladorNav.iniciarModulo(modulo);
    } catch (ModuloNoEncontradoException e) {
      pantallaSeleccion.setMensajeError(e.getMessage());
      controladorNav.irSeleccionModulo();
    } catch (EstadoInvalidoException e) {
      if (moduloActual != null) {
        moduloActual.removerObserver(this);
        moduloActual = null;
      }
      pantallaSeleccion.setMensajeError("Error al iniciar: " + e.getMessage());
      controladorNav.irSeleccionModulo();
    }
  }

  private void finalizarModuloActual() {
    if (moduloActual == null) return;
    moduloActual.removerObserver(this); // primero, para evitar recursión
    EstadisticasGenerales stats = moduloActual.getEstadisticasGenerales();
    if (stats != null) {
      gestorEstadisticas.guardarEstadisticas(stats);
    }
    try {
      String estadoNombre = moduloActual.getEstado().getNombre();
      if (!"FINALIZADO".equals(estadoNombre) && !"ERROR".equals(estadoNombre)) {
        moduloActual.finalizar();
      }
    } catch (EstadoInvalidoException e) {
      // ya estaba finalizado
    }
    moduloActual = null;
    controladorNav.irSeleccionModulo();
  }

  public void mostrarEstadisticasGenerales() {
    controladorNav.irEstadisticas();
  }

  public String getPantallaActualNombre() {
    return controladorNav.getPantallaActual().name();
  }

  public void salir() {
    app.exit();
  }

  @Override
  public void onEventoModulo(ModuloEvento evento) {
    if (evento.getTipo() == ModuloEvento.Tipo.FINALIZADO) {
      finalizarModuloActual();
    } else if (evento.getTipo() == ModuloEvento.Tipo.ERROR) {
      if (moduloActual != null) {
        moduloActual.removerObserver(this);
        moduloActual = null;
      }
      pantallaSeleccion.setMensajeError("Error en módulo: " + evento.getMensaje());
      controladorNav.irSeleccionModulo();
    }
  }
}
