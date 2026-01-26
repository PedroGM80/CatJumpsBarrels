plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))

            // Compose UI para Offset, Color, ImageBitmap, IntSize
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)

            // Coroutines para CatAnimation
            implementation(libs.kotlinx.coroutines.core)

            // DateTime para timestamps multiplatform (api para propagarlo)
            api(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
