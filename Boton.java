import processing.core.*;

public class Boton {
  private float x, y, ancho, alto;
  private String texto;
  private boolean seleccionado;

  public Boton(float x, float y, float ancho, float alto, String texto) {
    this.x = x;
    this.y = y;
    this.ancho = ancho;
    this.alto = alto;
    this.texto = texto;
    this.seleccionado = false;
  }

  public void dibujar(PApplet app) {
    if (seleccionado) {
      app.stroke(0, 255, 0);
      app.fill(0, 51, 0);
    } else {
      app.stroke(0, 120, 0);
      app.fill(0, 17, 0);
    }
    app.strokeWeight(2);
    app.rect(x, y, ancho, alto, 3);

    app.fill(255);
    app.textAlign(PApplet.LEFT, PApplet.CENTER);
    if (seleccionado) {
      app.text("> " + texto, x + 10, y + alto / 2);
    } else {
      app.text("  " + texto, x + 10, y + alto / 2);
    }
  }

  public boolean estaEncima(float mx, float my) {
    return mx >= x && mx <= x + ancho && my >= y && my <= y + alto;
  }

  public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }
  public boolean isSeleccionado() { return seleccionado; }
  public String getTexto() { return texto; }
  public float getX() { return x; }
  public float getY() { return y; }
  public float getAncho() { return ancho; }
  public float getAlto() { return alto; }
}
