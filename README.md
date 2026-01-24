# Cat Jump Barrels 🐱

A retro arcade platformer inspired by the Atari 2600 version of Donkey Kong, built with modern Kotlin and Compose Multiplatform.

![Game Screenshot](screenShot.png)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7.3-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📑 Table of Contents

- [Description](#-description)
- [Features](#-features)
- [Controls](#-controls)
- [How to Play](#-how-to-play)
- [Game Mechanics](#-game-mechanics)
- [Requirements](#-requirements)
- [Installation and Execution](#-installation-and-execution)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [Audio System](#-audio-system)
- [Technologies](#-technologies)
- [Development](#-development)
- [Troubleshooting](#-troubleshooting)
- [Credits](#-credits)
- [Recent Changes](#-recent-changes)
- [Contributing](#-contributing)

## 🎮 Description

Cat Jump Barrels is a nostalgic platformer where you control a cat who must dodge barrels thrown by a boss while climbing platforms and ladders to rescue a fish at the top. Built with modern Kotlin and Clean Architecture principles, featuring authentic synthesized audio and retro arcade visuals.

## ✨ Features

### Gameplay
- **Classic platformer mechanics** with authentic retro feel
- **Dynamic barrel physics** with ladder descent and platform rolling
- **Progressive difficulty** - Barrel speed increases with each level
- **Score system** with bonuses for jumping over barrels (+100 points)
- **Life system** with 3 lives and temporary invincibility after damage
- **Multiple platforms and ladders** for strategic gameplay
- **Victory condition** by reaching the fish at the top (+1000 points)

### Audio
- **🎵 Smooth synthesized music** - Triangle and sine wave synthesis for pleasant melodies
- **ADSR envelopes** - Smooth attack, decay, sustain, release for professional sound
- **Menu music** - Relaxed 100 BPM melody with melody, bass, and pad layers
- **Sound effects** for jump, score, death, victory, pause, and barrel throws
- **Automatic music control** - Music stops when leaving menu

### UI & Menus
- **Retro arcade UI** - Grid backgrounds, CRT scanlines, glow effects
- **Animated titles** with pulsing glow effects
- **Styled buttons** with hover/press animations and color accents
- **Main menu** with New Game, High Scores, Credits, and Exit options
- **High score system** - Save top 40 scores with player names (Room database)
- **Credits screen** with compact card layout
- **High score dialog** with trophy animation and styled input
- **Quit confirmation** - Press ESC for quit menu during gameplay
- **Fullscreen scaling** - Game scales proportionally to fit any screen size

### Visuals
- **Smooth sprite animations** - 72 hand-crafted frames for cat (idle, run, jump, fall, climb, hurt, dead)
- **Boss animations** - 7 frames for enemy behavior (idle, throw)
- **Particle effects** for deaths and scoring with physics simulation
- **Score popups** with fade animations and vertical movement
- **Retro pixel art** style with industrial theme
- **Dynamic HUD** showing score, high score, level, and lives with retro fonts
- **Adaptive scaling** - Game renders at 600x700 and scales to fit any window size

## 🎯 Controls

| Key | Action |
|-----|--------|
| `W` / `↑` | Move up / Climb ladder |
| `A` / `←` | Move left |
| `S` / `↓` | Move down / Descend ladder |
| `D` / `→` | Move right |
| `SPACE` | Jump |
| `P` | Pause game |
| `ESC` | Open quit menu |
| `R` | Restart (after Game Over) |

## 📖 How to Play

1. Navigate the main menu to start a new game
2. Control the cat at the bottom of the screen
3. Dodge barrels thrown by the boss at the top
4. Use ladders to climb between platforms
5. Jump over barrels to earn 100 points each
6. Reach the fish at the top to win and earn 1000 bonus points
7. Each level increases barrel speed - how far can you go?
8. Compete for a spot in the top 40 high scores!

## 🎲 Game Mechanics

- **Jump over barrels**: +100 points (destroys barrel, triggers particles)
- **Rescue the fish**: +1000 points, advance to next level
- **Level progression**: Barrel speed increases 15% per level
- **Invincibility**: 2 seconds after death or reaching objective
- **Lives**: 3 hearts with invincibility period after taking damage
- **Barrel behavior**:
  - Random chance to descend ladders (20%)
  - Rolling physics affected by platform slopes
  - Can fall off platform edges
- **Ladders**: Can be climbed from bottom or entered from platforms

## 🛠️ Requirements

- **JDK 17** or higher
- **Gradle 8.x**
- **OS**: Windows, macOS, or Linux

## 🚀 Installation and Execution

### Prerequisites
Ensure you have the following installed:
- **JDK 17** or higher ([Download](https://adoptium.net/))
- **Gradle 8.x** (included via wrapper)
- **Git** (for cloning the repository)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/CatJumpsBarrels.git
cd CatJumpsBarrels
```

2. **Run in development mode**
```bash
# Linux/macOS
./gradlew :composeApp:run

# Windows
gradlew.bat :composeApp:run
```

### Build Distributable Packages

Create native installers for your platform:

```bash
# Windows (MSI installer)
./gradlew :composeApp:packageMsi

# macOS (DMG installer)
./gradlew :composeApp:packageDmg

# Linux (DEB package)
./gradlew :composeApp:packageDeb

# Universal JAR (all platforms)
./gradlew :composeApp:packageUberJarForCurrentOS
```

Installers will be created in `composeApp/build/compose/binaries/main/`

### Development Commands

```bash
# Clean build
./gradlew clean

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies

# Build without running
./gradlew :composeApp:build
```

## 📁 Project Structure

The project follows **Clean Architecture** with modular organization:

```
CatJumpsBarrels/
├── composeApp/          # Main application module (JVM Desktop)
│   └── src/jvmMain/
│       ├── kotlin/dev/pgm/game/
│       │   ├── audio/        # MusicPlayer & RetroSoundGenerator
│       │   ├── di/           # Koin dependency injection
│       │   ├── presentation/ # UI & rendering
│       │   │   ├── navigation/ # NavigationGraph
│       │   │   ├── renderer/ # GameRenderer (Canvas drawing)
│       │   │   └── ui/       # Composable screens
│       │   ├── resources/    # AnimationProvider
│       │   └── Game.kt       # Main entry point
│       │
│       └── composeResources/drawable/  # Sprite assets
│           ├── cat_*.png     # 72 cat animation frames
│           ├── dog_*.png     # 7 boss animation frames
│           ├── barrel_*.png  # Barrel sprites
│           └── *.png         # Backgrounds, platforms, textures
│
├── core/                # Shared core utilities
│   └── src/commonMain/kotlin/dev/pgm/game/core/
│       └── utils/       # GameRect and geometry helpers
│
├── model/               # Shared domain model
│   └── src/commonMain/kotlin/dev/pgm/game/model/
│       ├── core/        # GameConstants, GameState
│       ├── entities/    # Player, Barrel, Boss, Platform, Ladder, etc.
│       └── utils/       # Particle, ScorePopup
│
├── domain/              # Business logic (Use Cases)
│   └── src/commonMain/kotlin/dev/pgm/game/domain/
│       ├── usecase/     # Game logic use cases
│       ├── factory/     # ParticleFactory
│       └── di/          # Domain DI module
│
├── data/                # Data persistence layer
│   └── src/jvmMain/kotlin/dev/pgm/game/data/
│       ├── database/    # Room database
│       └── repository/  # Repository implementations
│
└── presentation/        # Shared presentation logic
    └── src/commonMain/kotlin/dev/pgm/game/presentation/
        ├── ui/          # MainMenu, Credits, HighScores screens
        ├── viewmodel/   # GameViewModelComplete
        └── theme/       # GameFonts & styling
```

## 🏗️ Architecture

### Clean Architecture Layers

1. **Presentation Layer**
   - ViewModels (GameViewModelComplete, MainMenuViewModel)
   - UI Composables (GameScreen, MainMenuScreen, etc.)
   - Navigation (NavigationGraph with music control)
   - Input handling (InputHandler)

2. **Domain Layer**
   - Use Cases:
     - `UpdatePlayerUseCase` - Player movement and physics
     - `UpdateBarrelsUseCase` - Barrel behavior
     - `CheckCollisionsUseCase` - Collision detection
     - `SpawnBarrelUseCase` - Barrel spawning logic
     - `CheckHighScoreUseCase` & `SaveHighScoreUseCase`
   - Factories (ParticleFactory)
   - Services (TimeProvider)

3. **Data Layer**
   - Room database for persistent high scores
   - Repository pattern (HighScoreRepository)
   - Data models and entities

4. **Model Layer**
   - Game entities (Player, Barrel, Boss, Platform, Ladder, WinObjective)
   - Game state management
   - Constants and configuration

### Dependency Injection

The project uses **Koin** for dependency injection:

```kotlin
// Example module setup
val gameModule = module {
    single { HighScoreRepository(get()) }
    factory { UpdatePlayerUseCase(get(), get()) }
    viewModel { GameViewModelComplete(get(), get(), ...) }
}
```

## 🎵 Audio System

### Music System (MusicPlayer)
The menu features a pleasant synthesized melody:

- **100 BPM tempo** - Relaxed and enjoyable
- **Three layers**: Melody (triangle wave), Bass (sine wave), Pad (soft saw)
- **ADSR envelopes** - Smooth attack/decay/sustain/release with smoothstep curves
- **Pentatonic scale** - Always sounds harmonious
- **Harmonic richness** - Subtle overtones for warmth
- **Automatic control** - Stops when leaving menu, starts when entering

### Sound Effects (RetroSoundGenerator)
All sounds are generated programmatically using Java Sound API:

- **Square wave synthesis** for authentic 8-bit sound
- **Frequency sweeps** for jumps and deaths
- **No external audio files** - everything is synthesized in real-time

## 🎨 Technologies

### Core Framework
- **Kotlin** 2.1.0 - Modern, null-safe programming language
- **Compose Multiplatform** 1.7.3 - Declarative UI framework for Desktop
- **Jetpack Compose Desktop** - Native desktop UI rendering
- **Gradle** 8.x - Build system and dependency management

### Architecture & Patterns
- **Clean Architecture** - Separation of concerns with layers
- **MVVM Pattern** - ViewModel-based state management
- **Repository Pattern** - Data abstraction layer
- **Use Case Pattern** - Single responsibility business logic
- **Dependency Injection** - Koin 3.x for IoC container

### Data & Persistence
- **Room Database** - Type-safe SQLite ORM for high scores
- **StateFlow** - Reactive state management
- **Kotlin Coroutines** - Async operations and game loop

### UI & Navigation
- **Navigation Compose** - Type-safe screen navigation
- **Compose Resources** - Multiplatform resource management
- **Custom Fonts** - Press Start 2P retro font integration

### Audio
- **Java Sound API** - Low-level audio synthesis
- **Custom DSP** - Triangle, sine, and square wave generators
- **ADSR Envelopes** - Professional sound shaping
- **Programmatic Music** - Multi-layer composition engine

## 🎓 Development

### Key Development Features

**Code Organization**:
- **Modular architecture** - Independent Gradle modules
- **Clean separation** - Clear boundaries between layers
- **Type-safe** - Kotlin's null safety throughout
- **SOLID principles** - Single responsibility, dependency inversion

**Performance Optimizations**:
- **Lightweight UI** - No particle effects in menus for smooth performance
- **Efficient rendering** - Canvas-based drawing with minimal allocations
- **State caching** - AnimationProvider loads resources once
- **Native compilation** - Optimized JVM bytecode

## 🐛 Troubleshooting

### Common Issues

**Game doesn't start / Black screen**
- Ensure JDK 17+ is installed: `java -version`
- Check animations loaded: Look for "Error loading frame" in console
- Clear Gradle cache: `./gradlew clean`

**Performance issues**
- The game runs at 60 FPS by default
- Check system resources (CPU/GPU usage)
- Reduce window size for better performance on older hardware

**No sound**
- Verify audio output device is working
- Check system volume and game is not muted
- Audio uses Java Sound API (works on all platforms)

**High scores not saving**
- Database is stored in user home directory
- Check file permissions in `~/.catjumpsbarrels/`
- Room database initializes on first run

## 📝 Credits

### Attributions
- **Font**: Press Start 2P by Google Fonts
- **Inspired by**: Donkey Kong Atari 2600 (1982)
- **Atari 2600 Port**: Coleco / Garry Kitchen
- **Original characters & concept**: Nintendo

### Technologies
- Kotlin Multiplatform
- Jetpack Compose Desktop
- Clean Architecture
- Koin DI
- Coroutines
- Room Database

## 📋 Recent Changes

### Version 1.2.0 (Latest)
- **Improved music system** - Smooth triangle/sine waves instead of harsh square waves
- **Pleasant menu melody** - 100 BPM with melody, bass, and pad layers
- **Music control** - Automatically stops when leaving main menu
- **Victory improvements** - Player gets invincibility when reaching objective
- **Progressive difficulty** - Barrel speed increases with each level
- **Redesigned UI** - All menu screens with retro arcade style
  - Grid backgrounds and CRT scanlines
  - Animated titles with glow effects
  - Styled buttons with color accents
  - Compact credits layout
  - Improved high score dialog with trophy animation
- **Performance** - Removed particle effects from menus

### Version 1.1.0
- **Refactored animation system** - Centralized sprite loading with AnimationProvider
- **Normalized asset filenames** - Renamed 72 sprite files for consistency
- **Fixed barrel spawning** - Corrected animation frame check
- **Improved resource management** - Async loading with Compose Multiplatform Resources

### Version 1.0.0
- Initial release with complete gameplay
- Main menu, high scores, and credits screens
- 8-bit synthesized audio system
- Full Clean Architecture implementation

## 🤝 Contributing

This is a personal learning project demonstrating:
- **Clean Architecture** implementation in Kotlin
- **Compose Multiplatform** game development
- **Audio synthesis** and music composition
- **Retro game design** patterns and mechanics
- **Modular architecture** with dependency injection (Koin)

Feel free to fork and experiment with the code!

## 📄 License

This project is open source and available under the MIT License.

---

**Version**: 1.2.0
**Last Updated**: January 2026

Developed with ❤️ using Kotlin and Compose Multiplatform

🎮 Enjoy the retro gaming experience! 🐱
