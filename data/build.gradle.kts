import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
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
            // JVM-specific dependencies if needed
        }
        
        wasmJsMain.dependencies {
            // Browser APIs para localStorage
            implementation(libs.kotlinx.browser)
        }
    }
}
