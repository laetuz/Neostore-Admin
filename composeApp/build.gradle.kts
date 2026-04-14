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
            implementation("net.dongliu:apk-parser:2.6.10")
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
            packageName = "Neostore Admin"
            packageVersion = "1.5.1"

            macOS {
                iconFile.set { rootProject.file("media/neostore-admin.icns") }
            }
        }
    }
}

buildConfig {
    packageName("id.neotica.neostore.admin.config")

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    fun getCleanConfig(key: String): String {
        val rawValue = properties.getProperty(key) ?: System.getenv(key) ?: ""

        val cleanValue = rawValue.removeSurrounding("\"").removeSurrounding("'")
        return "\"$cleanValue\""
    }

    buildConfigField("String", "BASE_URL", getCleanConfig("BASE_URL"))
    buildConfigField("String", "BASE_URL_BUCKET", getCleanConfig("BASE_URL_BUCKET"))
}