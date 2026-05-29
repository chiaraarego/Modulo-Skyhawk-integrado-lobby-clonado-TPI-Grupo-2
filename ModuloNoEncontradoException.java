public class ModuloNoEncontradoException extends JuegoException {
  public ModuloNoEncontradoException(String nombreModulo) {
    super("Módulo no encontrado: " + nombreModulo);
  }
}
