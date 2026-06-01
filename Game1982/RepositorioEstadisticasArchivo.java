import java.io.*;
import java.util.*;

public class RepositorioEstadisticasArchivo implements RepositorioEstadisticas {
  private final String carpeta;

  public RepositorioEstadisticasArchivo(String carpeta) {
    this.carpeta = carpeta;
    File dir = new File(carpeta);
    if (!dir.exists()) { dir.mkdirs(); }
  }

  // Convierte EstadisticasGenerales a JSON string
  private String aJson(EstadisticasGenerales s) {
    return "{\n" +
      "  \"nombreModulo\": \"" + s.getNombreModulo() + "\",\n" +
      "  \"puntajeTotal\": " + s.getPuntajeTotal() + ",\n" +
      "  \"partidasJugadas\": " + s.getPartidasJugadas() + ",\n" +
      "  \"partidasGanadas\": " + s.getPartidasGanadas() + ",\n" +
      "  \"partidasPerdidas\": " + s.getPartidasPerdidas() + ",\n" +
      "  \"enemigosDestruidos\": " + s.getEnemigosDestruidos() + ",\n" +
      "  \"tiempoJugadoSegundos\": " + s.getTiempoJugadoSegundos() + "\n" +
      "}";
  }

  // Parsea un JSON string a EstadisticasGenerales
  private EstadisticasGenerales desdeJson(String json) throws PersistenciaException {
    try {
      String nombreModulo = extraerString(json, "nombreModulo");
      int puntajeTotal = extraerInt(json, "puntajeTotal");
      int partidasJugadas = extraerInt(json, "partidasJugadas");
      int partidasGanadas = extraerInt(json, "partidasGanadas");
      int partidasPerdidas = extraerInt(json, "partidasPerdidas");
      int enemigosDestruidos = extraerInt(json, "enemigosDestruidos");
      long tiempoJugadoSegundos = extraerLong(json, "tiempoJugadoSegundos");
      return new EstadisticasGenerales(nombreModulo, puntajeTotal,
          partidasJugadas, partidasGanadas, partidasPerdidas,
          enemigosDestruidos, tiempoJugadoSegundos);
    } catch (Exception e) {
      throw new PersistenciaException("Error al parsear JSON: " + e.getMessage(), e);
    }
  }

  private String extraerString(String json, String clave) {
    String patron = "\"" + clave + "\": \"";
    int inicio = json.indexOf(patron) + patron.length();
    int fin = json.indexOf("\"", inicio);
    return json.substring(inicio, fin);
  }

  private int extraerInt(String json, String clave) {
    String patron = "\"" + clave + "\": ";
    int inicio = json.indexOf(patron) + patron.length();
    int fin = inicio;
    while (fin < json.length() && (Character.isDigit(json.charAt(fin)) || json.charAt(fin) == '-')) fin++;
    return Integer.parseInt(json.substring(inicio, fin));
  }

  private long extraerLong(String json, String clave) {
    String patron = "\"" + clave + "\": ";
    int inicio = json.indexOf(patron) + patron.length();
    int fin = inicio;
    while (fin < json.length() && (Character.isDigit(json.charAt(fin)) || json.charAt(fin) == '-')) fin++;
    return Long.parseLong(json.substring(inicio, fin));
  }

  public void guardar(String nombreModulo, EstadisticasGenerales stats)
      throws PersistenciaException {
    // Acumular con stats anteriores si existen
    EstadisticasGenerales acumulada = stats;
    try {
      EstadisticasGenerales anterior = cargar(nombreModulo);
      acumulada = new EstadisticasGenerales(
        nombreModulo,
        anterior.getPuntajeTotal() + stats.getPuntajeTotal(),
        anterior.getPartidasJugadas() + stats.getPartidasJugadas(),
        anterior.getPartidasGanadas() + stats.getPartidasGanadas(),
        anterior.getPartidasPerdidas() + stats.getPartidasPerdidas(),
        anterior.getEnemigosDestruidos() + stats.getEnemigosDestruidos(),
        anterior.getTiempoJugadoSegundos() + stats.getTiempoJugadoSegundos()
      );
    } catch (PersistenciaException e) {
      // No habia archivo anterior, usar stats actuales
    }

    File archivo = new File(carpeta + File.separator + nombreModulo + ".json");
    try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
      pw.print(aJson(acumulada));
    } catch (IOException e) {
      throw new PersistenciaException(
          "Error al guardar estadísticas de " + nombreModulo, e);
    }
  }

  public EstadisticasGenerales cargar(String nombreModulo)
      throws PersistenciaException {
    File archivo = new File(carpeta + File.separator + nombreModulo + ".json");
    if (!archivo.exists()) {
      throw new PersistenciaException(
          "No se encontraron estadísticas para: " + nombreModulo);
    }
    try {
      StringBuilder sb = new StringBuilder();
      BufferedReader br = new BufferedReader(new FileReader(archivo));
      String linea;
      while ((linea = br.readLine()) != null) sb.append(linea).append("\n");
      br.close();
      return desdeJson(sb.toString());
    } catch (IOException e) {
      throw new PersistenciaException(
          "Error al cargar estadísticas de " + nombreModulo, e);
    }
  }

  public List<EstadisticasGenerales> cargarTodas()
      throws PersistenciaException {
    List<EstadisticasGenerales> resultado = new ArrayList<>();
    File dir = new File(carpeta);
    if (!dir.exists()) return resultado;
    File[] archivos = dir.listFiles((d, n) -> n.endsWith(".json"));
    if (archivos == null) return resultado;
    for (File archivo : archivos) {
      try {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;
        while ((linea = br.readLine()) != null) sb.append(linea).append("\n");
        br.close();
        resultado.add(desdeJson(sb.toString()));
      } catch (IOException e) {
        throw new PersistenciaException(
            "Error al cargar archivo: " + archivo.getName(), e);
      }
    }
    return resultado;
  }
}
