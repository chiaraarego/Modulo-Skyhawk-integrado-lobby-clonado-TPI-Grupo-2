import java.util.*;

public class GestorModulos {
  private List<ModuloJuego> modulos;

  public GestorModulos() {
    this.modulos = new ArrayList<>();
  }

  public void agregarModulo(ModuloJuego modulo) {
    modulos.add(modulo);
  }

  public void eliminarModulo(ModuloJuego modulo) {
    modulos.remove(modulo);
  }

  public ModuloJuego buscarModulo(String nombre) throws ModuloNoEncontradoException {
    for (ModuloJuego m : modulos) {
      if (m.getNombreModulo().equalsIgnoreCase(nombre)) {
        return m;
      }
    }
    throw new ModuloNoEncontradoException(nombre);
  }

  public List<ModuloJuego> listarModulos() {
    return Collections.unmodifiableList(modulos);
  }
}
