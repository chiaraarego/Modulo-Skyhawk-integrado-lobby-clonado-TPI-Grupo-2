public class ContextoJuego {
  private final String nombreJugador;
  private final int anchoPantalla;
  private final int altoPantalla;

  public ContextoJuego(String nombreJugador, int anchoPantalla, int altoPantalla) {
    this.nombreJugador = nombreJugador;
    this.anchoPantalla = anchoPantalla;
    this.altoPantalla = altoPantalla;
  }

  public String getNombreJugador() { return nombreJugador; }
  public int getAnchoPantalla() { return anchoPantalla; }
  public int getAltoPantalla() { return altoPantalla; }
}
