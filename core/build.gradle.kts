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
            // Solo tipos geométricos de Compose
            implementation(libs.compose.ui)
        }
    }
}
