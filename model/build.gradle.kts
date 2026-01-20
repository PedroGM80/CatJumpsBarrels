plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            // Removed :data dependency to avoid circular dependency with Room setup

            // Compose UI para Offset, Color, ImageBitmap, IntSize
            implementation(compose.ui)
            implementation(compose.runtime)

            // Coroutines para CatAnimation
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        }
    }
}
