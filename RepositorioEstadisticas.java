import java.util.List;

public interface RepositorioEstadisticas {
  void guardar(String nombreModulo, EstadisticasGenerales stats) throws PersistenciaException;
  EstadisticasGenerales cargar(String nombreModulo) throws PersistenciaException;
  List<EstadisticasGenerales> cargarTodas() throws PersistenciaException;
}
