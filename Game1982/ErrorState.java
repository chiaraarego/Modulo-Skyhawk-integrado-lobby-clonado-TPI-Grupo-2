public class ErrorState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo está en error, no puede iniciarse.");
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo está en error.");
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo está en error.");
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido: permite limpieza desde estado de error
  }
  public String getNombre() { return "ERROR"; }
}
