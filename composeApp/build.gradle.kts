import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.neotoast)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.koin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "id.neotica.neostore.admin.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "id.neotica.neostore.admin"
            packageVersion = "1.0.0"
        }
    }
}

buildConfig {
    packageName("id.neotica.neostore.admin.config")

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    val baseUrl = if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
        properties.getProperty("BASE_URL") ?: ""
    } else {
        System.getenv("BASE_URL") ?: ""
    }

    val cleanUrl = baseUrl.removeSurrounding("\"").removeSurrounding("'")

    buildConfigField("String", "BASE_URL", "\"$cleanUrl\"")
}