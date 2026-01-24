plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    // Preparado para Web
    // wasmJs {
    //     browser()
    // }

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
    }
}
