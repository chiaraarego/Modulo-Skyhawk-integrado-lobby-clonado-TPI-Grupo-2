// Skyhawk_Registro_Estadistica_Sky.pde
// Registra las estadisticas de UNA partida de Skyhawk mientras se juega.
// GameController lo crea y lo va actualizando; AvionSkyhawk lo lee al finalizar
// para construir el EstadisticasGenerales que entiende el lobby.
class Registro_Estadistica_Sky {

  private int puntaje;
  private int enemigosEliminados;
  private int partidasJugadas;
  private long tiempoJugado;          // en segundos
  private String situacionPartida;    // "DERROTA" / "VICTORIA"

  Registro_Estadistica_Sky() {
    puntaje = 0;
    enemigosEliminados = 0;
    partidasJugadas = 1;              // este registro representa una partida
    tiempoJugado = 0;
    situacionPartida = "DERROTA";     // survival: la partida siempre termina en derrota
  }

  // ---- registro DURANTE la partida (lo llama GameController) ----
  void registrarEnemigoEliminado(int puntos) {
    puntaje += puntos;
    enemigosEliminados++;
  }

  // ---- registro AL FINALIZAR (lo llama AvionSkyhawk) ----
  void registrarTiempo(long segundos) {
    tiempoJugado = segundos;
  }

  // ---- getters (los lee AvionSkyhawk al armar EstadisticasGenerales) ----
  int getPuntaje()             { return puntaje; }
  int getEnemigosEliminados()  { return enemigosEliminados; }
  int getPartidasJugadas()     { return partidasJugadas; }
  long getTiempoJugado()       { return tiempoJugado; }
  String getSituacionPartida() { return situacionPartida; }
}
