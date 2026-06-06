class Skyhawk extends Nave
{
  private int velocidad;   // cuantos pixeles avanza en cada paso
  private PImage sprite;   // el dibujo del avion

  Skyhawk(int xPos, int yPos)
  {
    super(xPos, yPos);
    this.velocidad = 5;   // velocidad inicial al cargarse
    this.vida = 3;        // vida inicial
    this.sprite = loadImage("SkyhawkSprite.png");   // se carga una sola vez
    this.sprite.resize(90, 60);   // la achico una vez para que dibujarla sea rapido
  }

  void actualizar()
  {
    // El movimiento lo dispara el GameController cuando lee el teclado
    // (llama a mover(...)). Por eso aca todavia no hay nada propio.
  }

  // Crea una bala nueva en la punta del avion (arriba) y la devuelve.
  // El GameController es el que la guarda en su lista de balas.
  ProyectilSkyhawk disparar()
  {
    return new ProyectilSkyhawk(this.x, this.y - 20);
  }

  // Mueve el avion un paso en la direccion indicada,
  // sin dejar que se salga de la pantalla.
  void mover(Direccion direccion)
  {
    if (direccion == Direccion.ARRIBA)
    {
      this.y = this.y - this.velocidad;
    }
    if (direccion == Direccion.ABAJO)
    {
      this.y = this.y + this.velocidad;
    }
    if (direccion == Direccion.IZQUIERDA)
    {
      this.x = this.x - this.velocidad;
    }
    if (direccion == Direccion.DERECHA)
    {
      this.x = this.x + this.velocidad;
    }

    // Limites de la pantalla: si se paso de un borde, lo dejo justo en el borde.
    if (this.x < 0)
    {
      this.x = 0;
    }
    if (this.x > width)
    {
      this.x = width;
    }
    if (this.y < 0)
    {
      this.y = 0;
    }
    if (this.y > height)
    {
      this.y = height;
    }
  }

  void dibujar()
  {
    // Dibujamos el avion con su sprite, centrado en (x, y).
    image(this.sprite, this.x, this.y);
  }
}
