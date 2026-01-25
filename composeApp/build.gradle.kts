import org.jetbrains.compose.desktop.application.dsl.TargetFormat
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
            // Módulos del proyecto
            implementation(project(":core"))
            implementation(project(":model"))
            implementation(project(":domain"))
            implementation(project(":input"))
            implementation(project(":data"))
            implementation(project(":presentation"))

            // Compose Core (compatible con wasmJs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.compose.core)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.coroutines.swing)
        }
        wasmJsMain.dependencies {
            // Web-specific Compose dependencies
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.pgm.game.presentation.ui.GameKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.pgm.game"
            packageVersion = "1.0.0"
        }
    }
}
