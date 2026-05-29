public interface EstadoJuego {
  void iniciar(ModuloJuego modulo) throws EstadoInvalidoException;
  void pausar(ModuloJuego modulo) throws EstadoInvalidoException;
  void reanudar(ModuloJuego modulo) throws EstadoInvalidoException;
  void finalizar(ModuloJuego modulo) throws EstadoInvalidoException;
  String getNombre();
}
