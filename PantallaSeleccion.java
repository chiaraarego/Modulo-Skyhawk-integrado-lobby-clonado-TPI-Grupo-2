import processing.core.*;
import java.util.*;

public class PantallaSeleccion {
  private List<Boton> botonesModulos;
  private List<String> nombresModulos;
  private Boton botonEstadisticas;
  private Boton botonVolver;
  private List<Boton> todosBotones;
  private int indiceSeleccion;
  private String mensajeError;

  public PantallaSeleccion(int anchoVentana, int altoVentana) {
    botonesModulos = new ArrayList<>();
    nombresModulos = new ArrayList<>();
    todosBotones = new ArrayList<>();
    indiceSeleccion = 0;
    mensajeError = null;

    float bAncho = 320;
    float bAlto = 44;

    botonEstadisticas = new Boton(anchoVentana / 2f - bAncho / 2f, altoVentana * 0.78f, bAncho, bAlto, "ESTADISTICAS");
    botonVolver = new Boton(anchoVentana / 2f - bAncho / 2f, altoVentana * 0.88f, bAncho, bAlto, "VOLVER");
  }

  public void setModulos(List<ModuloJuego> modulos, int anchoVentana, int altoVentana) {
    botonesModulos.clear();
    nombresModulos.clear();
    todosBotones.clear();

    float bAncho = 320;
    float bAlto = 44;
    float gap = 12;
    float totalH = modulos.size() * (bAlto + gap) - gap;
    float inicioY = altoVentana * 0.35f - totalH / 2f;

    for (int i = 0; i < modulos.size(); i++) {
      ModuloJuego m = modulos.get(i);
      Boton b = new Boton(anchoVentana / 2f - bAncho / 2f, inicioY + i * (bAlto + gap), bAncho, bAlto,
          m.getNombreAvion() + "  [" + m.getNombreModulo() + "]");
      botonesModulos.add(b);
      nombresModulos.add(m.getNombreModulo());
    }

    todosBotones.addAll(botonesModulos);
    todosBotones.add(botonEstadisticas);
    todosBotones.add(botonVolver);

    indiceSeleccion = 0;
    actualizarSeleccion();
  }

  private void actualizarSeleccion() {
    for (int i = 0; i < todosBotones.size(); i++) {
      todosBotones.get(i).setSeleccionado(i == indiceSeleccion);
    }
  }

  public void moverArriba() {
    if (todosBotones.isEmpty()) return;
    mensajeError = null;
    indiceSeleccion = (indiceSeleccion - 1 + todosBotones.size()) % todosBotones.size();
    actualizarSeleccion();
  }

  public void moverAbajo() {
    if (todosBotones.isEmpty()) return;
    mensajeError = null;
    indiceSeleccion = (indiceSeleccion + 1) % todosBotones.size();
    actualizarSeleccion();
  }

  public String confirmarSeleccion() {
    if (todosBotones.isEmpty()) return null;
    Boton sel = todosBotones.get(indiceSeleccion);
    if (sel == botonEstadisticas) return "ESTADISTICAS";
    if (sel == botonVolver) return "VOLVER";
    int idx = botonesModulos.indexOf(sel);
    if (idx >= 0) return nombresModulos.get(idx);
    return null;
  }

  public void setMensajeError(String msg) { this.mensajeError = msg; }

  public void dibujar(PApplet app) {
    app.background(0);

    app.fill(255);
    app.textSize(16);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.text("SELECCIONAR MODULO", app.width / 2f, app.height * 0.12f);

    app.stroke(0, 120, 0);
    app.strokeWeight(1);
    app.line(50, app.height * 0.19f, app.width - 50, app.height * 0.19f);

    app.fill(255);
    app.textSize(8);
    app.text("W/S para navegar  |  ENTER para confirmar  |  ESC para volver", app.width / 2f, app.height * 0.24f);

    for (Boton b : botonesModulos) b.dibujar(app);
    botonEstadisticas.dibujar(app);
    botonVolver.dibujar(app);

    if (mensajeError != null) {
      app.fill(255);
      app.textSize(8);
      app.textAlign(PApplet.CENTER, PApplet.CENTER);
      app.text("ERROR: " + mensajeError, app.width / 2f, app.height * 0.94f);
    }
  }

  public String clicEnModulo(float mx, float my) {
    for (int i = 0; i < botonesModulos.size(); i++) {
      if (botonesModulos.get(i).estaEncima(mx, my)) {
        indiceSeleccion = i;
        actualizarSeleccion();
        return nombresModulos.get(i);
      }
    }
    return null;
  }

  public boolean clicEnEstadisticas(float mx, float my) { return botonEstadisticas.estaEncima(mx, my); }
  public boolean clicEnVolver(float mx, float my) { return botonVolver.estaEncima(mx, my); }
}
