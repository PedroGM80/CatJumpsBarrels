# Clean Architecture - Cat Jumps Barrels
## Inspirado en la arquitectura limpia de Antonio Leiva

Este documento describe la implementación de Clean Architecture en el proyecto Cat Jumps Barrels, siguiendo los principios y patrones popularizados por Antonio Leiva para proyectos Kotlin/Android.

## 📐 Estructura de Capas

```
dev.pgm.game/
├── domain/                 # Capa de Dominio (Reglas de Negocio)
│   └── usecase/
│       ├── UpdatePlayerUseCase.kt
│       ├── UpdateBarrelsUseCase.kt
│       ├── UpdateSingleBarrelUseCase.kt
│       └── CheckCollisionsUseCase.kt
│
├── di/                     # Inyección de Dependencias
│   └── GameModule.kt       # DI Manual (sin librerías)
│
└── presentation/           # Capa de Presentación
    └── viewmodel/
        └── GameViewModel.kt
```

## 🎯 Principios Aplicados

### 1. **Separation of Concerns**
- Cada capa tiene una responsabilidad única y bien definida
- El dominio no conoce detalles de implementación de UI o datos
- La presentación solo orquesta y muestra resultados

### 2. **Dependency Rule**
- Las dependencias apuntan hacia adentro (hacia el dominio)
- El dominio no depende de nada externo
- La presentación depende del dominio

### 3. **Single Responsibility Principle**
- Cada UseCase tiene una responsabilidad única:
  - `UpdatePlayerUseCase`: Lógica del jugador
  - `UpdateBarrelsUseCase`: Procesamiento de barriles
  - `CheckCollisionsUseCase`: Detección de colisiones

## 📦 Componentes Principales

### Domain Layer (Dominio)

#### UseCases
Los Use Cases encapsulan la lógica de negocio del juego:

**UpdatePlayerUseCase**
- Responsabilidad: Actualizar estado del jugador basado en input
- Input: `GameState`, `GameInput`, `deltaTime`
- Output: `GameState` actualizado
- Maneja: Movimiento, saltos, escalada, colisiones con plataformas

**UpdateBarrelsUseCase**
- Responsabilidad: Procesar todos los barriles
- Optimización: Usa coroutines para procesamiento paralelo
- Delega a: `UpdateSingleBarrelUseCase` para cada barril

**CheckCollisionsUseCase**
- Responsabilidad: Detectar y manejar colisiones jugador-barril
- Maneja: Saltos sobre barriles, muerte del jugador, puntuación

### DI Layer (Inyección de Dependencias)

#### GameModule
- Patrón: **Service Locator** manual (sin librerías)
- Inspiración: Enfoque de Antonio Leiva para proyectos pequeños
- Ventajas:
  - Sin dependencias externas
  - Control total sobre creación de objetos
  - Fácil de entender y depurar

```kotlin
object GameModule {
    fun provideGameViewModel(): GameViewModel {
        return GameViewModel(
            updatePlayerUseCase = provideUpdatePlayerUseCase(),
            updateBarrelsUseCase = provideUpdateBarrelsUseCase(),
            checkCollisionsUseCase = provideCheckCollisionsUseCase()
        )
    }
}
```

### Presentation Layer (Presentación)

#### GameViewModel
- Patrón: **MVVM** (Model-View-ViewModel)
- Responsabilidades:
  - Orquestar Use Cases
  - Gestionar estado del juego (StateFlow)
  - Exponer API simple para la UI

```kotlin
class GameViewModel(
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val updateBarrelsUseCase: UpdateBarrelsUseCase,
    private val checkCollisionsUseCase: CheckCollisionsUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(...)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun tick(deltaTime: Float, input: GameInput) {
        // Orquesta todos los UseCases
    }
}
```

## 🚧 Estado Actual

### ✅ Completado
- [x] Estructura de capas Domain/DI/Presentation
- [x] UseCases principales creados
- [x] GameViewModel con StateFlow
- [x] GameModule para DI manual
- [x] Integración con coroutines

### ⚠️ Errores de Compilación Pendientes

1. **GameState.initial vs createInitialState**
   - Usar: `GameState.initial(screenSize)` en lugar de `createInitialState()`

2. **GameRect.intersects**
   - Necesita implementarse el método de extensión `intersects()`

3. **Color en Particles**
   - Ajustar formato de Color para Compose

4. **Barrel.speed**
   - Revisar modelo Barrel para velocidad

### 📋 Próximos Pasos

1. **Corregir errores de compilación**
   - Ajustar referencias a GameState
   - Implementar métodos faltantes en GameRect
   - Corregir tipos de Color

2. **Migrar lógica restante a UseCases**
   - `SpawnBarrelsUseCase`
   - `UpdateParticlesUseCase`
   - `UpdateAnimationUseCase`
   - `CheckWinConditionUseCase`

3. **Integrar ViewModel en Game.kt**
   ```kotlin
   @Composable
   fun CatJumpBarrels() {
       val viewModel = remember { GameModule.provideGameViewModel() }
       val gameState by viewModel.gameState.collectAsState()

       // Resto del código UI...
   }
   ```

4. **Eliminar código legacy**
   - Deprecar GameEngine.kt (migrar a UseCases)
   - Simplificar Game.kt (solo UI)

## 🎓 Beneficios de Clean Architecture

### Mantenibilidad
- Código organizado por responsabilidades
- Fácil encontrar y modificar funcionalidades

### Testabilidad
- UseCases son fáciles de testear (puro Kotlin)
- No dependencias de Android/Compose en dominio
- Mockeo simple de dependencias

### Escalabilidad
- Agregar nuevos UseCases es sencillo
- Fácil agregar nuevas capas (ej: Data/Repository)

### Rendimiento
- Procesamiento paralelo con coroutines
- Separación clara permite optimizar cada capa

## 📚 Referencias

- [Antonio Leiva - Clean Architecture](https://antonioleiva.com/clean-architecture-android/)
- [Uncle Bob - Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

---

**Siguiente paso**: Corregir errores de compilación y completar migración de lógica a UseCases.
