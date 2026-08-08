import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

val appVersion = (System.getenv("GITHUB_REF_NAME") ?: "2.2.0").removePrefix("v")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

kotlin {
    jvm()
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

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
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.neotoast)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.koin)
            implementation(libs.apk.parser)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.logback.classic)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "id.neotica.neostore.admin"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.neotica.neostore.admin"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = appVersion
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
        }
    }
}


compose.desktop {
    application {
        mainClass = "id.neotica.neostore.admin.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Neostore Admin"
            packageVersion = appVersion

            macOS {
                iconFile.set { rootProject.file("media/neostore-admin.icns") }
            }
            windows {
                iconFile.set { rootProject.file("media/neostore-admin.ico") }
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
    buildConfigField("String", "BASE_URL_BUCKET_PUBLIC", getCleanConfig("BASE_URL_BUCKET_PUBLIC"))
    buildConfigField("String", "APP_VERSION", "\"$appVersion\"")
}