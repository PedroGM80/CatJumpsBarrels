# Auditoria de Arquitectura - CatJumpsBarrels

## ESTADO: FASE 1 COMPLETADA (2025-01-21)

### Cambios realizados:
- **Eliminada duplicación de código**: ~2000 líneas removidas de `composeApp`
- **Módulos limpiados**: `domain/`, `model/`, `input/`, `data/` eliminados de composeApp
- **ViewModel unificado**: Una sola versión en `:presentation` con callback de sonidos
- **InputHandler mejorado**: Agregado parámetro `onEscape` separado de `onPause`
- **Compilación exitosa**: Todos los módulos compilan correctamente
- **Juego funcional**: Verificado que el juego se ejecuta sin errores

---

## Resumen Ejecutivo

~~El proyecto implementa Clean Architecture con modulos separados, pero tiene **problemas criticos de duplicacion de codigo** y **violaciones de principios SOLID** que deben corregirse.~~

**ACTUALIZADO**: La duplicacion de codigo ha sido eliminada. Quedan pendientes otras mejoras menores.

---

## 1. PROBLEMAS CRITICOS

### 1.1 Duplicacion Masiva de Codigo (DRY Violation)

**Severidad: CRITICA**

El modulo `composeApp` contiene copias completas de casi todo el codigo de los otros modulos:

```
composeApp/src/jvmMain/kotlin/dev/pgm/game/
├── domain/           <- DUPLICADO de :domain
│   ├── factory/
│   ├── services/
│   └── usecase/      <- 6 UseCases duplicados
├── model/            <- DUPLICADO de :model
│   ├── core/
│   ├── entities/
│   └── utils/
├── input/            <- DUPLICADO de :input
└── presentation/     <- Parcialmente duplicado de :presentation
```

**Impacto:**
- Bugs deben corregirse en 2+ lugares
- Inconsistencias entre versiones (ya vimos una con el ViewModel)
- Aumenta complejidad de mantenimiento
- Confunde a nuevos desarrolladores

**Solucion:** Eliminar TODO el codigo duplicado de `composeApp`. Este modulo solo debe contener:
- `main()` y configuracion de aplicacion
- Recursos especificos de plataforma (audio, imagenes)
- Inicializacion de Koin

### 1.2 ViewModel como Singleton (Anti-pattern)

**Severidad: ALTA**

```kotlin
// PresentationModule.kt
single { GameViewModelComplete(...) }  // PROBLEMA
```

Los ViewModels NO deben ser singletons porque:
- Mantienen estado entre navegaciones
- Causan bugs como el del dialogo que aparecia dos veces
- Violan el ciclo de vida esperado de ViewModels

**Solucion:**
```kotlin
viewModel { GameViewModelComplete(...) }  // Correcto
// O usar viewModelOf si usas Koin 3.5+
```

### 1.3 GameState con Logica de Creacion (SRP Violation)

**Severidad: MEDIA**

`GameState.kt` tiene 200+ lineas de logica de creacion de niveles en su `companion object`:

```kotlin
data class GameState(...) {
    companion object {
        fun initial(screenSize: IntSize): GameState {
            // 150+ lineas de logica de creacion de plataformas, escaleras, etc.
        }
    }
}
```

**Problema:** Un data class no deberia contener logica de negocio compleja.

**Solucion:** Crear `LevelFactory` o `LevelBuilder`:
```kotlin
class LevelFactory {
    fun createLevel1(screenSize: IntSize): GameState
}
```

---

## 2. PROBLEMAS DE ARQUITECTURA

### 2.1 Dependencias de Compose UI en Capa Model

**Severidad: MEDIA**

```kotlin
// model/build.gradle.kts
implementation(compose.ui)  // PROBLEMA
```

El modulo `model` depende de Compose para `Offset`, `IntSize`, `Color`.

**Problema:** La capa de modelo no deberia depender de frameworks de UI.

**Solucion:** Crear tipos propios:
```kotlin
// model/
data class Position(val x: Float, val y: Float)
data class Size(val width: Int, val height: Int)

// En presentation/ crear mappers
fun Position.toOffset() = Offset(x, y)
fun Offset.toPosition() = Position(x, y)
```

### 2.2 Modulo Input Innecesario

**Severidad: BAJA**

El modulo `input` solo contiene 2 archivos pequenos:
- `GameInput.kt` (6 lineas)
- `InputHandler.kt` (50 lineas)

**Solucion:** Mover a `presentation` o `model` segun corresponda.

### 2.3 Modulo Core Casi Vacio

**Severidad: BAJA**

```
core/
└── utils/
    └── GameRect.kt
└── resources/
    └── ImageLoader.kt
```

Solo 2 archivos. Este modulo no justifica su existencia.

**Solucion:** Mover `GameRect` a `model/utils/` y `ImageLoader` a `data/resources/`.

### 2.4 SpawnBarrelUseCase con Estado Mutable

**Severidad: MEDIA**

```kotlin
class SpawnBarrelUseCase {
    private var lastSpawnTime = 0L           // Estado mutable
    private var barrelSpawnedInCurrentThrow = false
    
    fun reset() { ... }  // Necesita reset manual
}
```

**Problema:** Los UseCases deben ser stateless. El estado deberia estar en `GameState`.

**Solucion:**
```kotlin
data class GameState(
    ...
    val lastBarrelSpawnTime: Long = 0L,
    val barrelSpawnedInCurrentThrow: Boolean = false
)

class SpawnBarrelUseCase {
    operator fun invoke(state: GameState, currentTime: Long): GameState {
        // Usa y retorna estado desde GameState
    }
}
```

---

## 3. ESTRUCTURA DE MODULOS PROPUESTA

### Estructura Actual (Problematica)
```
├── core/          (casi vacio)
├── model/         (depende de Compose)
├── input/         (muy pequeno)
├── domain/        
├── data/          
├── presentation/  
└── composeApp/    (DUPLICA TODO)
```

### Estructura Propuesta
```
├── domain/                    <- Logica de negocio pura
│   ├── model/                 <- Entidades sin dependencias
│   │   ├── entities/
│   │   ├── vo/               <- Value Objects (Position, Size, etc)
│   │   └── GameState.kt
│   ├── usecase/
│   ├── repository/           <- Interfaces
│   └── factory/
│
├── data/                      <- Implementaciones
│   ├── repository/
│   ├── database/
│   └── mapper/
│
├── presentation/              <- UI y ViewModels
│   ├── viewmodel/
│   ├── ui/
│   ├── input/                <- InputHandler
│   ├── mapper/               <- Domain <-> UI mappers
│   └── theme/
│
└── app/                       <- Solo punto de entrada
    ├── di/                   <- Koin modules
    ├── audio/                <- Platform-specific
    └── Main.kt
```

---

## 4. VIOLACIONES SOLID DETECTADAS

### 4.1 Single Responsibility Principle (SRP)

| Clase | Problema | Solucion |
|-------|----------|----------|
| `GameState` | Contiene logica de creacion de niveles | Extraer a `LevelFactory` |
| `GameViewModelComplete` | Maneja game loop + UI events + high scores | Separar en multiples ViewModels o usar Orchestrator |
| `CheckCollisionsUseCase` | Detecta colisiones Y maneja muerte Y crea particulas | Separar en `CollisionDetector` + `DeathHandler` |

### 4.2 Open/Closed Principle (OCP)

| Clase | Problema | Solucion |
|-------|----------|----------|
| `ParticleFactory` | Switch gigante para tipos | Usar Strategy pattern |
| `GameConstants` | No es extensible para nuevos niveles | Crear `LevelConfig` |

### 4.3 Dependency Inversion Principle (DIP)

| Clase | Problema | Solucion |
|-------|----------|----------|
| `Player` | Usa `System.currentTimeMillis()` directamente | Inyectar `TimeProvider` |
| `HighScoreRepositoryImpl` | Hardcodea path del archivo | Inyectar `FilePathProvider` |

---

## 5. PLAN DE REFACTORIZACION

### Fase 1: Eliminar Duplicacion (URGENTE)
1. Eliminar `composeApp/src/jvmMain/kotlin/dev/pgm/game/domain/`
2. Eliminar `composeApp/src/jvmMain/kotlin/dev/pgm/game/model/`
3. Eliminar `composeApp/src/jvmMain/kotlin/dev/pgm/game/input/`
4. Actualizar imports en archivos restantes
5. Verificar que todo compila desde los modulos correctos

### Fase 2: Corregir ViewModels
1. Cambiar `single` a `viewModel` en Koin
2. Implementar `resetForNewGame()` correctamente
3. Considerar usar `SavedStateHandle` para persistencia

### Fase 3: Limpiar Modulos
1. Eliminar modulo `core` (mover contenido)
2. Eliminar modulo `input` (mover a presentation)
3. Actualizar dependencias en build.gradle.kts

### Fase 4: Desacoplar de Compose
1. Crear `Position`, `Size`, `Velocity` en domain
2. Crear mappers en presentation
3. Eliminar dependencia de `compose.ui` en model

### Fase 5: Refactorizar GameState
1. Mover estado de spawn a GameState
2. Extraer creacion de niveles a LevelFactory
3. Hacer SpawnBarrelUseCase stateless

---

## 6. METRICAS ACTUALES

| Metrica | Valor | Objetivo |
|---------|-------|----------|
| Archivos duplicados | ~25 | 0 |
| Lineas duplicadas | ~2000 | 0 |
| Dependencias circulares | 0 | 0 |
| Modulos con < 3 archivos | 2 | 0 |
| UseCases con estado | 1 | 0 |
| ViewModels singleton | 3 | 0 |

---

## 7. CONCLUSIONES

El proyecto tiene una **buena base arquitectonica** con separacion de capas y uso de UseCases. Sin embargo, la **duplicacion masiva de codigo** es un problema critico que debe resolverse inmediatamente.

Prioridades:
1. **CRITICO**: Eliminar codigo duplicado en composeApp
2. **ALTO**: Corregir ViewModels singleton
3. **MEDIO**: Refactorizar GameState y SpawnBarrelUseCase
4. **BAJO**: Consolidar modulos pequenos
