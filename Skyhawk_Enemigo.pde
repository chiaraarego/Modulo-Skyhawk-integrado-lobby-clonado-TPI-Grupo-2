class Enemigo extends Nave
{
  int velocidad;   // cuantos pixeles baja en cada paso
  PImage sprite;   // el dibujo del enemigo

  Enemigo(int xPos, int yPos)
  {
    super(xPos, yPos);
    this.velocidad = 2;   // velocidad de caida
    this.vida = 1;        // los enemigos mueren de un solo tiro
    this.sprite = loadImage("EnemigoSprite.png");   // se carga una sola vez
    this.sprite.resize(90, 60);   // la achico una vez para que dibujarla sea rapido
  }

  void actualizar()
  {
    // El enemigo baja por la pantalla.
    this.y = this.y + this.velocidad;

    // Si salio por abajo, vuelve a aparecer arriba.
    if (this.y > height)
    {
      reaparecer();
    }
  }

  // Decide al azar si este enemigo dispara en este cuadro.
  // random(1) da un numero entre 0 y 1; como pedimos que sea menor a 0.01,
  // dispara mas o menos 1 de cada 100 cuadros (poca frecuencia para que sea jugable).
  // El GameController es el que crea la bala (igual que con el disparo del jugador).
  boolean intentaDisparar()
  {
    return random(1) < 0.01;
  }

  // Lo manda de vuelta arriba, en una columna al azar y con la vida llena.
  // Se usa cuando sale por abajo y tambien cuando lo derriban.
  void reaparecer()
  {
    this.y = 0;
    this.x = int(random(width));   // random(width) da un numero al azar entre 0 y width
    this.vida = 1;
  }

  void dibujar()
  {
    // El sprite ya viene girado para que mire hacia abajo. Centrado en (x, y).
    image(this.sprite, this.x, this.y);
  }
}
