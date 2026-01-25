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
            implementation(project(":domain"))
            implementation(project(":input"))

            // Compose Core (compatible con wasmJs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        jvmMain.dependencies {
            // Lifecycle (JVM only)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Coil (for images - JVM only)
            implementation(libs.coil.compose)
            implementation(libs.coil.compose.core)

            // Navigation (JVM only)
            implementation(libs.navigation.compose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.swing)
        }

        wasmJsMain.dependencies {
            // Web-specific dependencies
        }
    }
}
