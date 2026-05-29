import processing.core.*;

HomeJuego homeJuego;

void setup() {
  size(800, 600);
  frameRate(60);

  try {
    PFont fuentePixel = createFont("PressStart2P-Regular.ttf", 12, true);
    textFont(fuentePixel);
  } catch (Exception e) {
    textFont(createFont("Courier New", 12, true));
  }

  homeJuego = new HomeJuego(this);
  homeJuego.iniciarHome();
  homeJuego.registrarModulo(new ModuloPrueba());
  homeJuego.registrarModulo(new AvionSkyhawk());   // <- nuestro modulo Skyhawk
}

void draw() {
  homeJuego.dibujar();
}

void mousePressed() {
  homeJuego.manejarClick(mouseX, mouseY);
}

void keyPressed() {
  homeJuego.manejarTecla(keyCode, key);
}
