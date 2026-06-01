// La bala que disparan los enemigos: baja por la pantalla.
// La crea el GameController cuando un enemigo decide disparar (Enemigo.intentaDisparar()).
class ProyectilEnemigo extends Proyectil
{
  ProyectilEnemigo(int xPos, int yPos)
  {
    super(xPos, yPos);
  }

  void actualizar()
  {
    this.y = this.y + this.velocidad;   // baja
  }

  void dibujar()
  {
    fill(255, 140, 0);              // naranja
    rect(this.x, this.y, 4, 12);
  }
}
