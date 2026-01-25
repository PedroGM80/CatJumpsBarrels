plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    js {
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
        
        jsMain.dependencies {
            // Browser APIs para localStorage
            implementation(libs.kotlinx.browser)
        }
    }
}
