public class NoIniciadoState implements EstadoJuego {
  public void iniciar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido
  }
  public void pausar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("No se puede pausar: el módulo no ha iniciado.");
  }
  public void reanudar(ModuloJuego modulo) throws EstadoInvalidoException {
    throw new EstadoInvalidoException("No se puede reanudar: el módulo no ha iniciado.");
  }
  public void finalizar(ModuloJuego modulo) throws EstadoInvalidoException {
    // válido: cancelar antes de iniciar
  }
  public String getNombre() { return "NO_INICIADO"; }
}
