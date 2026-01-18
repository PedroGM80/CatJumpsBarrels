# Cat Jump Barrels

A classic arcade platformer game developed with Kotlin and Compose Multiplatform.

![Game Screenshot](screenShot.png)

## Description

Cat Jump Barrels is a retro arcade platformer where you control a character who must dodge barrels thrown by a gorilla while climbing platforms and ladders to rescue the princess at the top. The game is built with modern Kotlin and Compose Multiplatform technologies, enabling native execution across multiple platforms.

## Features

- Classic platformer physics and mechanics
- Score system with bonuses for jumping over barrels
- Life system with temporary invincibility after taking damage
- Interactive ladders and multiple platforms
- Smooth animations and vector graphics
- Multi-platform support (Windows, macOS, Linux)

## Controls

| Key | Action |
|-----|--------|
| `A` / `←` | Move left |
| `D` / `→` | Move right |
| `W` / `↑` | Climb up ladder |
| `S` / `↓` | Climb down ladder |
| `SPACE` | Jump |
| `P` / `ESC` | Pause |
| `R` | Restart (after Game Over or Victory) |

## How to Play

1. Control the blue character at the bottom of the screen
2. Dodge the barrels thrown by the gorilla
3. Climb ladders to reach higher platforms
4. Jump over barrels to earn 100 points
5. Reach the princess at the top to win

## Game Mechanics

- **Jump over barrels**: +100 points
- **Rescue the princess**: +1000 points
- **Lives**: 3 (with temporary invincibility after losing one)
- **Ladders**: You can jump while on ladders

## Requirements

- JDK 17 or higher
- Gradle 8.x

## Installation and Execution

### Run in development mode

```bash
./gradlew :composeApp:run
```

### Build distributable

```bash
# Windows (MSI)
./gradlew :composeApp:packageMsi

# macOS (DMG)
./gradlew :composeApp:packageDmg

# Linux (DEB)
./gradlew :composeApp:packageDeb
```

## Project Structure

```
composeApp/src/jvmMain/kotlin/dev/pgm/game/
├── main.kt          # Entry point
├── Game.kt          # Main composable and input handling
├── GameModel.kt     # Data models (Player, Barrel, Platform, etc.)
├── GameEngine.kt    # Game logic (physics, collisions, AI)
├── GameRenderer.kt  # Graphics rendering
├── ImageLoader.kt   # Sprite loading system (optional)
└── Platform.kt      # Platform information
```

## Customizing Graphics

The game uses vector graphics by default. To use PNG sprites:

1. Place images in `composeApp/src/jvmMain/resources/sprites/`
2. Modify `ImageLoader.kt` and `GameRenderer.kt`

## Technologies

- **Kotlin** 2.1.0 - Main programming language
- **Compose Multiplatform** 1.7.3 - Declarative UI framework
- **Coroutines** - Asynchronous game loop management
- **Gradle** - Build system and dependency management

## Architecture

The project follows Clean Architecture principles with separation of concerns:

- **Presentation Layer**: Composables and input handling (`Game.kt`)
- **Domain Layer**: Game logic and business rules (`GameEngine.kt`)
- **Data Layer**: Data models (`GameModel.kt`)
- **Rendering Layer**: Graphics rendering system (`GameRenderer.kt`)

## Development

This project uses Compose Multiplatform, which enables:
- Hot reload development for rapid iteration
- Shared code across platforms
- Modern declarative UI with Jetpack Compose
- Native compilation for each target platform

---

Developed with ❤️ using Kotlin and Compose Multiplatform
