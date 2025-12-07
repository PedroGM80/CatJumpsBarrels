# Koin - Inyección de Dependencias Multiplataforma

## 🚀 Configuración

Koin ha sido configurado para el proyecto Cat Jumps Barrels como sistema de inyección de dependencias multiplataforma.

## 📦 Dependencias Agregadas

```kotlin
// build.gradle.kts
implementation("io.insert-koin:koin-core:3.5.3")
implementation("io.insert-koin:koin-compose:1.1.2")
```

## 🎯 Módulos Koin

### gameModule (KoinModule.kt)

```kotlin
val gameModule = module {
    // UseCases
    factoryOf(::UpdatePlayerUseCase)
    factoryOf(::UpdateSingleBarrelUseCase)
    factoryOf(::UpdateBarrelsUseCase)
    factoryOf(::CheckCollisionsUseCase)

    // ViewModel (Singleton)
    singleOf(::GameViewModel)
}
```

## 📝 Cómo Usar

### 1. Inicializar Koin en main()

```kotlin
fun main() = application {
    // Inicializar Koin una sola vez
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Cat Jump Barrels"
    ) {
        App()
    }
}
```

### 2. Inyectar ViewModel en Composables

#### Opción A: Usando koinViewModel()

```kotlin
import org.koin.compose.koinInject
import dev.pgm.game.presentation.viewmodel.GameViewModel

@Composable
fun GameScreen() {
    val viewModel: GameViewModel = koinInject()
    val gameState by viewModel.gameState.collectAsState()

    // Usar gameState...
}
```

#### Opción B: Inyectar directamente

```kotlin
@Composable
fun GameScreen() {
    val viewModel = koinInject<GameViewModel>()

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            viewModel.tick(0.016f, currentInput)
        }
    }

    GameRenderer(viewModel.gameState.value)
}
```

### 3. Inyectar UseCases manualmente (si es necesario)

```kotlin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MiClase : KoinComponent {
    private val updatePlayerUseCase: UpdatePlayerUseCase by inject()

    fun ejecutar() {
        // Usar updatePlayerUseCase...
    }
}
```

## 🔄 Scopes de Koin

### Factory vs Single

- **factoryOf()**: Crea una nueva instancia cada vez
  - Usado para: UseCases (son stateless)

- **singleOf()**: Singleton (una sola instancia)
  - Usado para: ViewModel (mantiene estado del juego)

## 🎨 Ejemplo Completo

```kotlin
// Game.kt
fun main() = application {
    // 1. Inicializar Koin
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Cat Jump Barrels",
        state = WindowState(size = DpSize(850.dp, 750.dp))
    ) {
        MaterialTheme(colors = darkColors()) {
            CatJumpBarrels()
        }
    }
}

@Composable
fun CatJumpBarrels() {
    // 2. Inyectar ViewModel
    val viewModel: GameViewModel = koinInject()
    val gameState by viewModel.gameState.collectAsState()

    var input by remember { mutableStateOf(GameInput()) }

    // 3. Game Loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime

            viewModel.tick(deltaTime, input)

            delay(16) // 60 FPS
        }
    }

    // 4. Input handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { event ->
                input = when (event.key) {
                    Key.DirectionLeft -> input.copy(left = event.type == KeyEventType.KeyDown)
                    Key.DirectionRight -> input.copy(right = event.type == KeyEventType.KeyDown)
                    Key.DirectionUp -> input.copy(up = event.type == KeyEventType.KeyDown)
                    Key.DirectionDown -> input.copy(down = event.type == KeyEventType.KeyDown)
                    Key.Spacebar -> input.copy(jump = event.type == KeyEventType.KeyDown)
                    Key.P -> {
                        if (event.type == KeyEventType.KeyDown) viewModel.togglePause()
                        input
                    }
                    else -> input
                }
                true
            }
    ) {
        // 5. Renderizar juego
        GameRenderer(gameState)
    }
}
```

## ✅ Ventajas de Koin

1. **Multiplataforma**: Funciona en JVM, JS, Native
2. **DSL Simple**: Fácil de leer y entender
3. **Sin Reflexión**: Rápido y eficiente
4. **Integración con Compose**: koinInject() directo en composables
5. **Testeable**: Fácil mockear dependencias

## 🧪 Testing con Koin

```kotlin
@Test
fun testGameViewModel() {
    // Setup Koin para tests
    startKoin {
        modules(gameModule)
    }

    // Obtener dependencias
    val viewModel: GameViewModel = get()

    // Tests...

    // Cleanup
    stopKoin()
}
```

## 📚 Referencias

- [Koin Documentation](https://insert-koin.io/)
- [Koin Compose](https://insert-koin.io/docs/reference/koin-compose/compose)
- [Koin Best Practices](https://insert-koin.io/docs/reference/koin-core/dsl)

---

**Próximo paso**: Reemplazar el código legacy en Game.kt con el ViewModel de Koin.
