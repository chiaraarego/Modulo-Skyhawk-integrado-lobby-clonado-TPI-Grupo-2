public class ModuloEvento {
  public enum Tipo {
    INICIADO, PAUSADO, REANUDADO, FINALIZADO, ERROR
  }

  private Tipo tipo;
  private String nombreModulo;
  private String mensaje;

  public ModuloEvento(Tipo tipo, String nombreModulo) {
    this.tipo = tipo;
    this.nombreModulo = nombreModulo;
    this.mensaje = "";
  }

  public ModuloEvento(Tipo tipo, String nombreModulo, String mensaje) {
    this.tipo = tipo;
    this.nombreModulo = nombreModulo;
    this.mensaje = mensaje;
  }

  public Tipo getTipo() { return tipo; }
  public String getNombreModulo() { return nombreModulo; }
  public String getMensaje() { return mensaje; }
}
