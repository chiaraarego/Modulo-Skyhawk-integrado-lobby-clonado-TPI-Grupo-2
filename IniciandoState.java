public class IniciandoState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("El módulo ya está iniciando.");
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("No se puede pausar mientras se cargan recursos.");
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("No se puede reanudar mientras se cargan recursos.");
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido: permite abortar la carga antes de que termine
  }
  public String getNombre() { return "INICIANDO"; }
}
