## Branch: processing-ide

Este branch contiene el proyecto adaptado para correr desde **Processing IDE**. Podés encontrar información sobre cómo ejecutar el juego en `COMO_EJECUTAR.md`<br>
El branch `main` contiene la versión original con estructura Maven y paquetes Java.

---

# Modulo 1 - Lobby del Juego
## Módulos del juego (por grupo)

| Módulo | Descripción |
|--------|-------------|
| Lobby General | Home y pantalla de inicio del juego |
| Avión Skyhawk | Armada Argentina |
| Avión Pucará | Ejército Argentino |
| Avión Aermacchi MB-339 | Piloteado por Owen Crippa |
| Avión Mirage | Fuerza Aérea Argentina |
| Avión Super Etendard | Armada Argentina |

---

## Cronograma de entregas

| Fecha | Tipo | Entregables |
|-------|------|-------------|
| 05/05/2026 | Seguimiento I | Documentación de Análisis |
| 19/05/2026 | Seguimiento II | Documentación de Diseño |
| 09/06/2026 | Final | Todos los entregables + defensa oral |

> Los seguimientos parciales no son obligatorios pero impactan en el criterio de organización del grupo.

---

## Cómo ejecutar

Ver [COMO_EJECUTAR.md](COMO_EJECUTAR.md) para instrucciones detalladas.

**Resumen:**
1. Abrir Processing IDE
2. Ir a `Archivo` → `Abrir` → seleccionar `Game1982.pde`
3. Presionar **▶ Play**

---

## Estructura del proyecto

```
Game1982.pde                     <- punto de entrada (Processing IDE)
│
├── Contratos e interfaces
│   ├── ModuloJuego.java         <- INTERFAZ PRINCIPAL que todos los aviones deben implementar
│   ├── EstadoJuego.java         <- interfaz del patron State
│   ├── IModuloObserver.java     <- interfaz del patron Observer
│   ├── ContextoJuego.java       <- datos que el Home le pasa al avion (inmutable)
│   └── ModuloEvento.java        <- eventos entre modulo y observer
│
├── Excepciones
│   ├── JuegoException.java      <- excepcion base del juego
│   ├── EstadoInvalidoException.java
│   ├── ModuloNoEncontradoException.java
│   └── PersistenciaException.java
│
├── Estados (patron State)
│   ├── NoIniciadoState.java     <- creado pero no iniciado, solo permite iniciar()
│   ├── IniciandoState.java      <- cargando recursos, estado de transicion
│   ├── EnEjecucionState.java    <- corriendo, permite pausar() y finalizar()
│   ├── PausadoState.java        <- pausado, permite reanudar() y finalizar()
│   ├── FinalizadoState.java     <- estado terminal, no permite nada
│   └── ErrorState.java          <- error, solo permite finalizar() para limpieza
│
├── Gestores y navegacion
│   ├── GestorModulos.java       <- administra los aviones registrados
│   ├── GestorEstadisticas.java  <- procesa y acumula estadisticas
│   ├── ControladorNavegacion.java <- maneja la navegacion entre pantallas
│   └── Pantalla.java            <- enum: INICIO, SELECCION, ESTADISTICAS, JUEGO
│
├── Persistencia
│   ├── RepositorioEstadisticas.java        <- interfaz de persistencia
│   ├── RepositorioEstadisticasArchivo.java <- implementacion en JSON
│   └── EstadisticasGenerales.java          <- objeto que el avion devuelve al finalizar
│
├── UI
│   ├── Boton.java               <- boton reutilizable con deteccion de hover y clic
│   ├── PantallaInicio.java      <- muestra "1982" y boton START
│   ├── PantallaSeleccion.java   <- lista de aviones disponibles
│   └── PantallaEstadisticas.java <- ranking y puntajes acumulados
│
├── Orquestador
│   └── HomeJuego.java           <- coordina todo el Home, gestores y ciclo de vida
│
├── Modulo de prueba
│   └── ModuloPrueba.java        <- modulo de prueba removible (ver abajo)
│
└── data/
    └── PressStart2P-Regular.ttf <- fuente pixel art
```

---

## Requisitos del módulo de avión

Cada grupo debe entregar una clase Java que implemente la interfaz `ModuloJuego`.
El grupo del Lobby se encarga de la integración al proyecto.

### Métodos obligatorios

```java
// Identidad
String getNombreModulo();   // identificador único, sin espacios, coordinado con todos los grupos
String getDescripcion();
String getNombreAvion();    // nombre que aparece en pantalla

// Ciclo de vida
void inicializarContexto(ContextoJuego ctx); // guardar el contexto para usar ancho/alto de pantalla
void iniciar()   throws EstadoInvalidoException;
void pausar()    throws EstadoInvalidoException;
void reanudar()  throws EstadoInvalidoException;
void finalizar() throws EstadoInvalidoException;
void reset();    // limpiar estado interno para poder rejugar

// Estado y estadísticas
EstadoJuego getEstado();
EstadisticasGenerales getEstadisticasGenerales(); // nunca devolver null

// Observer
void agregarObserver(IModuloObserver observer);
void removerObserver(IModuloObserver observer);

// Dibujo — llamados 60 veces por segundo
void actualizar(PApplet app);
void dibujar(PApplet app);
```

### Requisitos de implementación

**Ciclo de vida:** cada método debe seguir el orden: validar con estado actual → cambiar estado → notificar observer. Si no notificás `FINALIZADO`, el Home nunca vuelve al menú ni guarda las estadísticas.

**Estados:** usar los estados provistos (`NoIniciadoState`, `IniciandoState`, etc.). La transición de `INICIANDO` a `EN_EJECUCION` la hace el propio módulo en `actualizar()` cuando termina de cargar.

**actualizar():** cortar la lógica si el estado no es `EN_EJECUCION` — sino el juego sigue corriendo pausado.

**dibujar():** manejar los tres estados activos: `INICIANDO`, `EN_EJECUCION` y `PAUSADO`. Si no mostrás algo en `PAUSADO` la pantalla queda congelada sin aviso.

**reset():** volver a `NoIniciadoState` y limpiar la lista de observers — sino la segunda partida tiene observers duplicados.

**getEstadisticasGenerales():** nunca devolver `null` — el Home lo llama al finalizar para guardar.

**Teclas:** no manejar ESC ni Q — el Home los intercepta para pausar, reanudar y finalizar.

**Recursos:** usar nombres de archivo únicos que identifiquen tu módulo (ej: `skyhawk_fondo.png`) para evitar conflictos con otros grupos al integrar.

**Tamaño de pantalla:** usar `contexto.getAnchoPantalla()` y `contexto.getAltoPantalla()` en lugar de valores hardcodeados.

> Ver `ModuloPrueba.java` como ejemplo de implementación completa.---

## Módulo de prueba

`ModuloPrueba.java` es un módulo de integración que simula una partida de 5 segundos.
Sirve para probar el Home sin necesitar los módulos reales.
**Es removible** — cuando todos los aviones estén integrados se puede eliminar.

---

## Estadísticas

- Se guardan en formato **JSON** en la carpeta `estadisticas/` del sketch
- **Persisten entre sesiones**
- Se **acumulan** — cada partida suma al historial del módulo
- Un archivo `.json` por módulo: `Skyhawk.json`, `Pucara.json`, etc.

---

## Criterios de evaluación

### Evaluación grupal

| Aspecto | Regular (1) | Bueno (2) | Muy Bueno (3) | Excelente (4) |
|---------|-------------|-----------|---------------|---------------|
| **Análisis del problema** | No se interpreta correctamente o no hay documentación | Documentación suficiente y acorde a lo solicitado | Completa y argumentada, relacionando temas de clase | Evidencia bibliografía e investigación adicional |
| **Diseño en POO** | Incompleto o no acorde al análisis | Solución básica en POO | Completo, con conceptos de diseño, extensible y mantenible | Usa patrones de diseño, reflexiona sobre principios y atributos de calidad |
| **Implementación** | No compila o arroja errores inesperados | Resuelve el problema sin errores en compilación/ejecución | Incorpora buenas prácticas y está alineado al diseño | Conocimiento profundo del lenguaje, eficiencia o pruebas unitarias |
| **Organización y presentación** | Fuera de término o formato inconsistente | Formato consistente y presentación preparada | Usa herramientas de colaboración, versionado y organización | Gestiona el ciclo de vida del trabajo |

### Evaluación individual

| Aspecto | Regular (1) | Bueno (2) | Muy Bueno (3) | Excelente (4) |
|---------|-------------|-----------|---------------|---------------|
| **Participación en el grupo** | Sin evidencia de participación | Plantea ideas o escucha a los demás | Participa activamente en discusiones y resolución | Contribuye proactivamente y fomenta la colaboración |
| **Defensa del trabajo** | No asiste o no participa | Expone su rol y aportes con claridad | Responde con seguridad las preguntas | Relaciona conceptos de la materia y argumenta decisiones de diseño |

---

## Presentación (defensa oral)

Contenidos recomendados:
- Presentación de integrantes y responsabilidades
- Breve descripción del análisis del problema
- Detalle de la solución diseñada (al menos 1 caso de uso)
- Problemas encontrados y cómo se resolvieron
- Posibles mejoras pendientes
- Estrategias para incorporar nueva funcionalidad
- Demostración funcional con un caso de uso
- Conclusiones

> **Todos los integrantes deben estar presentes.** Los docentes pueden preguntar a cualquier integrante sobre cualquier aspecto del trabajo.
