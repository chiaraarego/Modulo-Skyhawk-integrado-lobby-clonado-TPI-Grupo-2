public class PausadoState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("No se puede iniciar: el módulo está pausado.");
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya está pausado.");
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido
  }
  public String getNombre() { return "PAUSADO"; }
}
