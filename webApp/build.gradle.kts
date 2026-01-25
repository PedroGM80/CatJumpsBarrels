import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "catjumpbarrels.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime para game loop
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        wasmJsMain.dependencies {
            // Módulos del proyecto
            implementation(project(":core"))
            implementation(project(":model"))
            implementation(project(":domain"))
            implementation(project(":input"))
            implementation(project(":data"))
            implementation(project(":presentation"))
            implementation(project(":composeApp"))
        }
    }
}
