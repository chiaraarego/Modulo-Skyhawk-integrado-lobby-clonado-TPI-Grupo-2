import java.util.*;

public class GestorEstadisticas {
  private RepositorioEstadisticas repositorio;

  public GestorEstadisticas(RepositorioEstadisticas repositorio) {
    this.repositorio = repositorio;
  }

  public void guardarEstadisticas(EstadisticasGenerales stats) {
    try {
      repositorio.guardar(stats.getNombreModulo(), stats);
    } catch (PersistenciaException e) {
      System.err.println("Error al guardar estadísticas: " + e.getMessage());
    }
  }

  public EstadisticasGenerales obtenerPorModulo(String nombreModulo) {
    try {
      return repositorio.cargar(nombreModulo);
    } catch (PersistenciaException e) {
      System.err.println("Error al cargar estadísticas de " + nombreModulo + ": " + e.getMessage());
      return null;
    }
  }

  public List<EstadisticasGenerales> obtenerTodas() {
    try {
      return repositorio.cargarTodas();
    } catch (PersistenciaException e) {
      System.err.println("Error al cargar todas las estadísticas: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  public int calcularPuntajeTotalCurso() {
    int total = 0;
    for (EstadisticasGenerales stats : obtenerTodas()) {
      total += stats.getPuntajeTotal();
    }
    return total;
  }
}
