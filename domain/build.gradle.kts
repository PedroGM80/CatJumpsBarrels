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
            implementation(project(":model"))
            implementation(project(":input"))

            // Compose UI para Offset, Color
            implementation(compose.ui)

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

            // Koin for DI
            implementation("io.insert-koin:koin-core:3.5.3")
        }
    }
}
