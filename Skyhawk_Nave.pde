class Nave
{
  protected int x;
  protected int y;
  protected int vida;

  Nave(int xPos, int yPos)
  {
    this.x = xPos;
    this.y = yPos;
  // vida inicial al cargarse
  }

  // Las subclases (Skyhawk, Enemigo) sobrescriben estos metodos.
  void actualizar()
  {
  }

  void dibujar()
  {
  }

  // Devuelve true si el punto (otroX, otroY) esta cerca de esta nave,
  // o sea, si se estan tocando. Usamos dist() para medir la distancia.
  boolean colisionaCon(int otroX, int otroY)
  {
    return dist(this.x, this.y, otroX, otroY) < 30;
  }

  // Le resta vida a la nave.
  void recibirDanio(int cuanto)
  {
    this.vida = this.vida - cuanto;
  }

  // Devuelve true si todavia le queda vida.
  boolean estaViva()
  {
    return this.vida > 0;
  }
}
