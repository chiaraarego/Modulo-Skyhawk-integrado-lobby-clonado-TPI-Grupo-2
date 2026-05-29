public class FinalizadoState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya finalizó, no puede reiniciarse.");
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya finalizó.");
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya finalizó.");
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya está finalizado.");
  }
  public String getNombre() { return "FINALIZADO"; }
}
