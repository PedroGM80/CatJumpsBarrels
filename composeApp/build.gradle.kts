import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    
    js {
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

            // Compose Core (Multiplatform)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material) // Material 2 for commonMain
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.coil.compose)
                implementation(libs.coil.compose.core)
                implementation(libs.navigation.compose)
                implementation(libs.kotlinx.coroutines.swing)
                // DateTime - MUST be implementation (not runtimeOnly) because model uses jvm() 
                // while composeApp uses jvm("desktop"), causing transitive deps to not resolve
                implementation(libs.kotlinx.datetime)
            }
        }
        jsMain.dependencies {
            // No specific dependencies needed here for core compose after moving them to commonMain
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
