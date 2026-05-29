# Cómo ejecutar 1982 desde Processing IDE

## Requisitos previos

- **Processing IDE 4.x o superior** instalado
- No se requiere ninguna otra dependencia

Descargar Processing desde [processing.org](https://processing.org/download)

---

## Controles del juego

| Tecla | Acción |
|-------|--------|
| **ENTER** | Seleccionar / Confirmar |
| **ESC** | Volver / Pausar |
| **W** / **S** o flechas | Navegar menús |
| **Q** | Finalizar partida |

También se puede usar el **mouse** para hacer clic en los botones de los menús.

---

## Cómo ejecutar

### Paso 1: Clonar o descargar el branch

```bash
git clone -b processing-ide https://github.com/marcosbenson/Modulo_1_Algoritmos_1.git
```

O descargar el ZIP desde GitHub seleccionando el branch `processing-ide`.

### Paso 2: Abrir en Processing IDE

1. Abrir Processing IDE
2. Ir a `Archivo` → `Abrir`
3. Navegar hasta la carpeta descargada
4. Seleccionar el archivo `Game1982.pde`

### Paso 3: Ejecutar

Hacer clic en el botón **▶ Play** o usar `Sketch` → `Run`.

---

## Estadísticas

Las estadísticas se guardan automáticamente en formato JSON en la carpeta
`estadisticas/` dentro del directorio del sketch. Persisten entre sesiones.

---

## Cómo integrar un módulo de avión

1. Crear una clase Java que implemente la interfaz `ModuloJuego`
2. Implementar todos sus métodos incluyendo `dibujar()`, `actualizar()` y `reset()`
3. Copiar el archivo `.java` a la carpeta del sketch
4. Registrar el módulo en `Game1982.pde`:

```java
homeJuego.registrarModulo(new TuAvion());
```

5. Eliminar `ModuloPrueba.java` si ya no se necesita

---

## Estructura del proyecto

```
Game1982.pde                     <- punto de entrada
ModuloJuego.java                 <- interfaz principal a implementar
EstadoJuego.java                 <- interfaz del patron State
IModuloObserver.java             <- interfaz del patron Observer
ContextoJuego.java               <- contexto inmutable del juego
ModuloEvento.java                <- eventos entre modulo y observer
JuegoException.java              <- base de excepciones
EstadoInvalidoException.java     <- operacion invalida segun estado
ModuloNoEncontradoException.java <- modulo no registrado
PersistenciaException.java       <- error de lectura/escritura
NoIniciadoState.java             <- estados concretos patron State
IniciandoState.java
EnEjecucionState.java
PausadoState.java
FinalizadoState.java
ErrorState.java
GestorModulos.java               <- gestores
GestorEstadisticas.java
ControladorNavegacion.java
Pantalla.java
RepositorioEstadisticas.java     <- persistencia
RepositorioEstadisticasArchivo.java
EstadisticasGenerales.java
Boton.java                       <- UI
PantallaInicio.java
PantallaSeleccion.java
PantallaEstadisticas.java
HomeJuego.java                   <- orquestador principal
ModuloPrueba.java                <- modulo de prueba (removible)
data/                            <- fuente pixel art
```

---

## Problemas comunes

| Problema | Solución |
|----------|----------|
| Pantalla negra sin texto | Verificar que data/PressStart2P-Regular.ttf existe |
| El juego no cierra | Cerrar la ventana con la X |
| ESC no funciona | Hacer clic primero en la ventana del juego |
| Error al guardar estadísticas | Verificar que Processing tiene permisos de escritura en la carpeta del sketch |