plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    // alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// Configure JavaScript/Webpack for offline builds
allprojects {
    afterEvaluate {
        // Skip Yarn setup when using npm
        tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask> {
            onlyIf {
                project.properties.containsKey("skipNpmInstall").not()
            }
        }
    }
}

// Suppress expect/actual classes beta warning
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xexpect-actual-classes"
    }
}
