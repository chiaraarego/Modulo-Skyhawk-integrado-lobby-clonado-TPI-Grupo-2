import processing.core.*;
import java.util.*;

public class PantallaEstadisticas {
  private Boton botonVolver;

  public PantallaEstadisticas(int anchoVentana, int altoVentana) {
    float bAncho = 220;
    float bAlto = 44;
    botonVolver = new Boton(anchoVentana / 2f - bAncho / 2f, altoVentana * 0.88f, bAncho, bAlto, "VOLVER");
    botonVolver.setSeleccionado(true);
  }

  public void dibujar(PApplet app, List<EstadisticasGenerales> estadisticas, int puntajeTotal) {
    app.background(0);

    app.fill(255);
    app.textSize(16);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.text("ESTADISTICAS", app.width / 2f, app.height * 0.10f);

    app.stroke(0, 120, 0);
    app.strokeWeight(1);
    app.line(50, app.height * 0.17f, app.width - 50, app.height * 0.17f);

    if (estadisticas == null || estadisticas.isEmpty()) {
      app.fill(255);
      app.textSize(9);
      app.textAlign(PApplet.CENTER, PApplet.CENTER);
      app.text("SIN DATOS AUN", app.width / 2f, app.height * 0.45f);
    } else {
      float startY = app.height * 0.22f;
      float rowH = 68;
      app.textSize(8);

      for (int i = 0; i < estadisticas.size(); i++) {
        EstadisticasGenerales s = estadisticas.get(i);
        float cy = startY + i * rowH;

        app.stroke(0, 51, 0);
        app.fill(0, 24, 0);
        app.rect(40, cy, app.width - 80, rowH - 6, 3);

        app.fill(255);
        app.textAlign(PApplet.LEFT, PApplet.TOP);
        app.text(s.getNombreModulo(), 55, cy + 6);

        app.fill(255);
        app.text("PUNTAJE: " + s.getPuntajeTotal()
            + "   PARTIDAS: " + s.getPartidasJugadas()
            + "   VICTORIAS: " + s.getPartidasGanadas(), 55, cy + 22);
        app.text("ENEMIGOS: " + s.getEnemigosDestruidos()
            + "   TIEMPO: " + s.getTiempoJugadoSegundos() + "s", 55, cy + 38);
      }
    }

    app.stroke(0, 120, 0);
    app.line(50, app.height * 0.80f, app.width - 50, app.height * 0.80f);
    app.fill(255);
    app.textSize(10);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.text("PUNTAJE TOTAL DEL CURSO: " + puntajeTotal, app.width / 2f, app.height * 0.84f);

    botonVolver.dibujar(app);
  }

  public boolean clicEnVolver(float mx, float my) { return botonVolver.estaEncima(mx, my); }
}
