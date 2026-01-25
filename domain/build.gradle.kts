plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":model"))
            implementation(project(":input"))

            // Compose UI para Offset, Color
            implementation(libs.compose.ui)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime para timestamps multiplatform
            implementation(libs.kotlinx.datetime)

            // Koin for DI
            implementation(libs.koin.core)
        }
    }
}
