plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            // Compose para ImageBitmap y recursos
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}
