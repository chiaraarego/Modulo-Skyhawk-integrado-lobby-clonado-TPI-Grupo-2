class GameController
{
  Skyhawk skyhawk;                          // el avion del jugador
  ArrayList<Enemigo> enemigos;              // los aviones enemigos que caen
  ArrayList<ProyectilSkyhawk> balasJugador; // las balas que dispara el avion
  ArrayList<ProyectilEnemigo> balasEnemigo; // las balas que disparan los enemigos
  int puntaje;
  int enemigosDerribados;

  GameController()
  {
    // "Cargar la partida": estado inicial
    puntaje = 0;
    enemigosDerribados = 0;
    // "Cargar el avion": centrado, en la parte de abajo
    skyhawk = new Skyhawk(width / 2, height - 80);
    // "Cargar los enemigos": una lista de aviones que caen desde arriba
    enemigos = new ArrayList<Enemigo>();
    crearEnemigos();
    // Las balas arrancan vacias
    balasJugador = new ArrayList<ProyectilSkyhawk>();
    balasEnemigo = new ArrayList<ProyectilEnemigo>();
  }

  // Crea varios enemigos repartidos arriba de la pantalla.
  void crearEnemigos()
  {
    for (int i = 0; i < 5; i++)
    {
      int x = 100 + i * 100;   // separados: 100, 200, 300, 400, 500
      int y = -i * 120;        // escalonados arriba (negativo = todavia afuera)
      enemigos.add(new Enemigo(x, y));
    }
  }

  // Crea una bala nueva en la punta del avion.
  // La llama keyPressed() (en el archivo principal) cuando se aprieta espacio.
  void dispararSkyhawk()
  {
    int xBala = skyhawk.x;
    int yBala = skyhawk.y - 20;   // sale desde la punta del avion
    balasJugador.add(new ProyectilSkyhawk(xBala, yBala));
  }

  // Devuelve true si el avion del jugador sigue vivo (lo usa el game over).
  boolean jugadorVivo()
  {
    return skyhawk.estaViva();
  }

  void actualizar()
  {
    leerTeclado();        // mover el avion segun las teclas apretadas
    skyhawk.actualizar();

    // Mover los enemigos (todos bajan) y, de vez en cuando, hacerlos disparar.
    for (Enemigo e : enemigos)
    {
      e.actualizar();
      if (e.intentaDisparar())
      {
        // La bala sale desde abajo del enemigo (e.y + 20) y baja por la pantalla.
        balasEnemigo.add(new ProyectilEnemigo(e.x, e.y + 20));
      }
    }

    // Mover las balas del jugador
    for (ProyectilSkyhawk bala : balasJugador)
    {
      bala.actualizar();
    }

    // Mover las balas de los enemigos
    for (ProyectilEnemigo bala : balasEnemigo)
    {
      bala.actualizar();
    }

    detectarColisiones();
  }

  // Revisa los choques: balas del jugador contra enemigos, y enemigos contra el avion.
  void detectarColisiones()
  {
    // 1) Balas del jugador contra enemigos
    ArrayList<ProyectilSkyhawk> balasQueSiguen = new ArrayList<ProyectilSkyhawk>();
    for (ProyectilSkyhawk bala : balasJugador)
    {
      boolean golpeo = false;
      for (Enemigo e : enemigos)
      {
        if (e.colisionaCon(bala.x, bala.y))
        {
          e.recibirDanio(1);
          golpeo = true;
          if (!e.estaViva())   // si se quedo sin vida, lo derribamos
          {
            puntaje = puntaje + 10;
            enemigosDerribados = enemigosDerribados + 1;
            e.reaparecer();    // aparece uno nuevo arriba
          }
        }
      }
      // La bala se queda solo si no golpeo a nadie y sigue dentro de la pantalla.
      if (!golpeo && bala.y >= 0)
      {
        balasQueSiguen.add(bala);
      }
    }
    balasJugador = balasQueSiguen;

    // 2) Enemigos contra el avion (choque)
    for (Enemigo e : enemigos)
    {
      if (skyhawk.colisionaCon(e.x, e.y))
      {
        skyhawk.recibirDanio(1);
        e.reaparecer();   // el enemigo que choco desaparece (aparece uno nuevo)
      }
    }

    // 3) Balas de los enemigos contra el avion del jugador
    ArrayList<ProyectilEnemigo> balasEnemQueSiguen = new ArrayList<ProyectilEnemigo>();
    for (ProyectilEnemigo bala : balasEnemigo)
    {
      boolean golpeo = false;
      if (skyhawk.colisionaCon(bala.x, bala.y))
      {
        skyhawk.recibirDanio(1);
        golpeo = true;
      }
      // La bala se queda solo si no golpeo al avion y sigue dentro de la pantalla.
      if (!golpeo && bala.y <= height)
      {
        balasEnemQueSiguen.add(bala);
      }
    }
    balasEnemigo = balasEnemQueSiguen;
  }

  // Mira que tecla esta apretada y mueve el avion en esa direccion.
  // Se aceptan tanto WASD como las flechas del teclado.
  void leerTeclado()
  {
    if (keyPressed)
    {
      // Teclas W A S D (letras normales)
      if (key == 'w')
      {
        skyhawk.mover(Direccion.ARRIBA);
      }
      if (key == 's')
      {
        skyhawk.mover(Direccion.ABAJO);
      }
      if (key == 'a')
      {
        skyhawk.mover(Direccion.IZQUIERDA);
      }
      if (key == 'd')
      {
        skyhawk.mover(Direccion.DERECHA);
      }

      // Flechas: son teclas "especiales". Processing avisa poniendo
      // key == CODED, y dice cual fue en la variable keyCode.
      if (key == CODED)
      {
        if (keyCode == UP)
        {
          skyhawk.mover(Direccion.ARRIBA);
        }
        if (keyCode == DOWN)
        {
          skyhawk.mover(Direccion.ABAJO);
        }
        if (keyCode == LEFT)
        {
          skyhawk.mover(Direccion.IZQUIERDA);
        }
        if (keyCode == RIGHT)
        {
          skyhawk.mover(Direccion.DERECHA);
        }
      }
    }
  }

  void dibujar()
  {
    skyhawk.dibujar();
    // Dibujar cada enemigo
    for (Enemigo e : enemigos)
    {
      e.dibujar();
    }
    // Dibujar cada bala del jugador
    for (ProyectilSkyhawk bala : balasJugador)
    {
      bala.dibujar();
    }
    // Dibujar cada bala de los enemigos
    for (ProyectilEnemigo bala : balasEnemigo)
    {
      bala.dibujar();
    }
    // HUD: puntaje y vida
    fill(255);
    textAlign(LEFT, TOP);
    textSize(16);
    text("Puntaje: " + puntaje, 10, 10);
    text("Vida: " + skyhawk.vida, 10, 30);
  }
}
