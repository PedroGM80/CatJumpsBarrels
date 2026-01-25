import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":model"))
            implementation(project(":input"))

            // Compose UI para Offset, Color
            implementation(compose.ui)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin for DI
            implementation(libs.koin.core)
        }
    }
}
