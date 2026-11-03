import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation("androidx.activity:activity-compose:1.9.3")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}

val ciBuildNumber = System.getenv("BUILD_NUM")?.toIntOrNull()
    ?: project.findProperty("versionCode")?.toString()?.toIntOrNull()
    ?: 101

val appVer = System.getenv("APP_VERSION")?.takeIf { it.isNotBlank() }
    ?: project.findProperty("versionName")?.toString()?.takeIf { it.isNotBlank() }
    ?: "1.0"

android {
    namespace = "cz.mares.jenpockej"
    compileSdk = 36

    defaultConfig {
        applicationId = "cz.mares.jenpockej"
        minSdk = 24
        targetSdk = 36
        versionCode = ciBuildNumber
        versionName = "$appVer.$ciBuildNumber"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
