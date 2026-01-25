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
            implementation(project(":core"))

            // Compose UI para Offset, Color, ImageBitmap, IntSize
            implementation(compose.ui)
            implementation(compose.runtime)

            // Coroutines para CatAnimation
            implementation(libs.kotlinx.coroutines.core)

            // DateTime para timestamps multiplatform
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
