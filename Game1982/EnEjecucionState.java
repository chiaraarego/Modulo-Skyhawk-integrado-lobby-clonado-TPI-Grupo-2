public class EnEjecucionState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya está en ejecución.");
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya está en ejecución, no está pausado.");
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido
  }
  public String getNombre() { return "EN_EJECUCION"; }
}
