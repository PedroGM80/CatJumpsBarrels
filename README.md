# Cat Jump Barrels

Un clon de Donkey Kong desarrollado con Kotlin y Compose Multiplatform.

## Controles

| Tecla | Accion |
|-------|--------|
| `A` / `←` | Mover izquierda |
| `D` / `→` | Mover derecha |
| `W` / `↑` | Subir escalera |
| `S` / `↓` | Bajar escalera |
| `ESPACIO` | Saltar |
| `P` / `ESC` | Pausar |
| `R` | Reiniciar (tras Game Over o Victoria) |

## Como jugar

1. Controla al personaje azul en la parte inferior
2. Esquiva los barriles que lanza el gorila
3. Sube por las escaleras para llegar a las plataformas superiores
4. Salta sobre los barriles para ganar 100 puntos
5. Llega hasta la princesa en la parte superior para ganar

## Mecanicas

- **Saltar barriles**: +100 puntos
- **Rescatar princesa**: +1000 puntos
- **Vidas**: 3 (con invencibilidad temporal tras perder una)
- **Escaleras**: Puedes saltar desde las escaleras

## Requisitos

- JDK 17 o superior
- Gradle 8.x

## Ejecutar

```bash
./gradlew :composeApp:run
```

## Compilar distribuible

```bash
# Windows (MSI)
./gradlew :composeApp:packageMsi

# macOS (DMG)
./gradlew :composeApp:packageDmg

# Linux (DEB)
./gradlew :composeApp:packageDeb
```

## Estructura del proyecto

```
composeApp/src/jvmMain/kotlin/dev/pgm/game/
├── main.kt          # Punto de entrada
├── Game.kt          # Composable principal y manejo de input
├── GameModel.kt     # Modelos de datos (Player, Barrel, Platform, etc.)
├── GameEngine.kt    # Logica del juego (fisica, colisiones, IA)
├── GameRenderer.kt  # Renderizado grafico
├── ImageLoader.kt   # Sistema de carga de sprites (opcional)
└── Platform.kt      # Info de plataforma
```

## Personalizar graficos

El juego usa graficos vectoriales por defecto. Para usar sprites PNG:

1. Coloca imagenes en `composeApp/src/jvmMain/resources/sprites/`
2. Modifica `ImageLoader.kt` y `GameRenderer.kt`

## Tecnologias

- Kotlin 2.1.0
- Compose Multiplatform 1.7.3
- Coroutines para el game loop
