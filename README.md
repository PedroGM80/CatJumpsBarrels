# Cat Jump Barrels 🐱

A retro arcade platformer inspired by the Atari 2600 version of Donkey Kong, developed with Kotlin and Compose Multiplatform.

![Game Screenshot](screenShot.png)

## 🎮 Description

Cat Jump Barrels is a nostalgic platformer where you control a cat who must dodge barrels thrown by a boss while climbing platforms and ladders to rescue a fish at the top. Built with modern Kotlin and Clean Architecture principles, featuring authentic 8-bit synthesized audio and retro graphics.

## ✨ Features

### Gameplay
- **Classic platformer mechanics** with authentic retro feel
- **Dynamic barrel physics** with ladder descent and platform rolling
- **Score system** with bonuses for jumping over barrels (+100 points)
- **Life system** with 3 lives and temporary invincibility after damage
- **Multiple platforms and ladders** for strategic gameplay
- **Victory condition** by reaching the fish at the top (+1000 points)

### Audio
- **🎵 8-bit synthesized sounds** - No WAV files, pure square wave synthesis
- **Background music** - Catchy NES-style melody for the main menu
- **Sound effects** for jump, score, death, victory, pause, and barrel throws
- **Retro chiptune** inspired by Super Mario Bros and Zelda classics

### UI & Menus
- **Main menu** with New Game, High Scores, Credits, and Exit options
- **High score system** - Save top 40 scores with player names (Room database)
- **Quit confirmation** - Press ESC for quit menu during gameplay
- **Credits screen** with attributions and technologies used
- **Fullscreen scaling** - Game scales proportionally to fit any screen size

### Visuals
- **Smooth animations** for cat, boss, and barrel sprites
- **Particle effects** for deaths and scoring
- **Score popups** with fade animations
- **Retro pixel art** style with industrial theme
- **HUD display** showing score, high score, level, and lives

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
7. Compete for a spot in the top 40 high scores!

## 🎲 Game Mechanics

- **Jump over barrels**: +100 points (triggers score popup and particles)
- **Rescue the fish**: +1000 points
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

### Run in development mode

```bash
./gradlew :composeApp:run
```

### Build distributable packages

```bash
# Windows (MSI)
./gradlew :composeApp:packageMsi

# macOS (DMG)
./gradlew :composeApp:packageDmg

# Linux (DEB)
./gradlew :composeApp:packageDeb
```

## 📁 Project Structure

The project follows **Clean Architecture** with modular organization:

```
CatJumpsBarrels/
├── composeApp/          # Main application module
│   └── src/jvmMain/kotlin/dev/pgm/game/
│       ├── audio/       # RetroSoundGenerator & MusicPlayer
│       ├── data/        # Room database & repositories
│       ├── di/          # Koin dependency injection
│       ├── domain/      # Use cases (business logic)
│       ├── input/       # InputHandler
│       ├── model/       # Game entities & state
│       ├── presentation/ # ViewModels, UI, navigation
│       └── Game.kt      # Main entry point
│
├── model/               # Shared model module
│   └── src/commonMain/kotlin/dev/pgm/game/model/
│       ├── core/        # GameConstants, GameState
│       ├── entities/    # Player, Barrel, Boss, Platform, etc.
│       └── utils/       # Particle, ScorePopup
│
├── domain/              # Shared domain module
│   └── src/commonMain/kotlin/dev/pgm/game/domain/
│       ├── usecase/     # Game logic use cases
│       ├── factory/     # ParticleFactory
│       └── services/    # TimeProvider
│
├── data/                # Shared data module
│   └── src/commonMain/kotlin/dev/pgm/game/data/
│       ├── database/    # Room database
│       └── repository/  # HighScoreRepository
│
└── presentation/        # Shared presentation module
    └── src/commonMain/kotlin/dev/pgm/game/presentation/
        ├── ui/          # MainMenu, Credits, HighScores
        ├── viewmodel/   # ViewModels
        └── theme/       # Fonts & styling
```

## 🏗️ Architecture

### Clean Architecture Layers

1. **Presentation Layer**
   - ViewModels (GameViewModelComplete, MainMenuViewModel)
   - UI Composables (GameScreen, MainMenuScreen, etc.)
   - Navigation (NavigationGraph)
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

### Synthesized Sound Effects
All sounds are generated programmatically using Java Sound API:

- **Square wave synthesis** for authentic 8-bit sound
- **Frequency sweeps** for jumps and deaths
- **ADSR envelopes** for smooth attack and release
- **No external audio files** - everything is synthesized in real-time

### Background Music
The menu features a catchy chiptune melody:

- **120 BPM tempo** with bouncy rhythm
- **Arpeggios** (C-E-G patterns) typical of NES games
- **Walking bass** with sixteenth note rhythms
- **Melodic structure**: A-A'-B-End for catchiness
- **Inspired by** Super Mario Bros and The Legend of Zelda

## 🎨 Technologies

- **Kotlin** 2.1.0 - Main programming language
- **Compose Multiplatform** 1.7.3 - Declarative UI framework
- **Koin** - Dependency injection
- **Room** - SQLite database for persistent storage
- **Coroutines** - Asynchronous game loop and async operations
- **Navigation Compose** - Screen navigation
- **Gradle** - Build system and dependency management

## 🎓 Development

### Key Features of Development Setup
- **Hot reload** for rapid UI iteration
- **Modular architecture** for maintainability
- **Type-safe navigation** with NavigationGraph
- **State management** with StateFlow
- **SOLID principles** throughout codebase
- **Native compilation** for optimal performance

### Graphics System
- Uses Jetpack Compose Canvas for rendering
- Custom sprite animations with frame-based system
- Particle effects with physics simulation
- Scaling engine for fullscreen support (600x700 design resolution)

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

## 🤝 Contributing

This is a personal learning project demonstrating:
- Clean Architecture implementation in Kotlin
- Compose Multiplatform game development
- Audio synthesis and chiptune music composition
- Retro game design patterns

---

**Version**: 1.0.0

Developed with ❤️ using Kotlin and Compose Multiplatform

🎮 Enjoy the retro gaming experience! 🐱
