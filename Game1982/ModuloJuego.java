public interface ModuloJuego {
  String getNombreModulo();
  String getDescripcion();
  String getNombreAvion();

  void inicializarContexto(ContextoJuego ctx);
  void iniciar() throws EstadoInvalidoException;
  void pausar() throws EstadoInvalidoException;
  void reanudar() throws EstadoInvalidoException;
  void finalizar() throws EstadoInvalidoException;

  EstadoJuego getEstado();
  EstadisticasGenerales getEstadisticasGenerales();

  void agregarObserver(IModuloObserver observer);
  void removerObserver(IModuloObserver observer);

  void actualizar(processing.core.PApplet app);
  void dibujar(processing.core.PApplet app);

  void reset();
}
