// Clase base de las balas. Guarda la posicion y la velocidad.
// Las subclases (ProyectilSkyhawk, ProyectilEnemigo) dicen para donde se mueve
// cada una y de que color se dibuja.
class Proyectil
{
  int x;
  int y;
  int velocidad;

  Proyectil(int xPos, int yPos)
  {
    this.x = xPos;
    this.y = yPos;
    this.velocidad = 8;
  }

  // Las subclases sobrescriben estos metodos.
  void actualizar()
  {
  }

  void dibujar()
  {
  }
}
