import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":model"))
            implementation(project(":domain"))

            // Koin
            implementation(libs.koin.core)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }
        
        jvmMain.dependencies {
            // Compose para ImageBitmap y recursos (si se necesita)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
        
        wasmJsMain.dependencies {
            // Dependencias específicas de Web si se necesitan
        }
    }
}
