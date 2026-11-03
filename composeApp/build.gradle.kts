import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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
    ?: System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.let { it + 100 }
    ?: project.findProperty("versionCode")?.toString()?.toIntOrNull()
    ?: 101

val appVer = System.getenv("APP_VERSION")?.takeIf { it.isNotBlank() }
    ?: project.findProperty("versionName")?.toString()?.takeIf { it.isNotBlank() }
    ?: "1.0"

// Nacteni trvaleho podpisoveho klice (z ios-keys / keystore.properties / env),
// aby sel novy build v telefonu vzdy preinstalovat bez nutnosti odinstalace
val keystoreProps = Properties()
val propCandidates = listOf(
    rootProject.file("keystore.properties"),
    rootProject.file("android_keystore.props"),
    rootProject.file("keys/android_keystore.props"),
    project.file("keystore.properties"),
    project.file("android_keystore.props")
)
propCandidates.firstOrNull { it.isFile }?.inputStream()?.use { keystoreProps.load(it) }

fun propOrEnv(vararg keys: String): String? {
    for (k in keys) {
        val fromProps = keystoreProps.getProperty(k)?.trim()?.takeIf { it.isNotEmpty() }
        if (fromProps != null) return fromProps
        val fromEnv = System.getenv(k)?.trim()?.takeIf { it.isNotEmpty() }
        if (fromEnv != null) return fromEnv
    }
    return null
}

val configuredStorePath = propOrEnv("storeFile", "STORE_FILE", "KEYSTORE_FILE", "ANDROID_KEYSTORE_PATH")
val storeCandidates = buildList {
    if (configuredStorePath != null) {
        add(rootProject.file(configuredStorePath))
        add(project.file(configuredStorePath))
    }
    add(rootProject.file("release.keystore"))
    add(rootProject.file("keystore.jks"))
    add(rootProject.file("android.keystore"))
    add(rootProject.file("keys/android.keystore"))
    add(project.file("release.keystore"))
    add(project.file("keystore.jks"))
}
val resolvedKeystoreFile = storeCandidates.firstOrNull { it.isFile && it.length() > 0L }
val resolvedStorePassword = propOrEnv("storePassword", "STORE_PASSWORD", "KEYSTORE_PASSWORD", "ANDROID_KEYSTORE_PASSWORD")
val resolvedKeyAlias = propOrEnv("keyAlias", "KEY_ALIAS", "ALIAS", "ANDROID_KEY_ALIAS")
val resolvedKeyPassword = propOrEnv("keyPassword", "KEY_PASSWORD", "ANDROID_KEY_PASSWORD") ?: resolvedStorePassword

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

    signingConfigs {
        if (
            resolvedKeystoreFile != null &&
            !resolvedStorePassword.isNullOrBlank() &&
            !resolvedKeyAlias.isNullOrBlank() &&
            !resolvedKeyPassword.isNullOrBlank()
        ) {
            create("release") {
                storeFile = resolvedKeystoreFile
                storePassword = resolvedStorePassword
                keyAlias = resolvedKeyAlias
                keyPassword = resolvedKeyPassword
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        val persistentSigning = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        getByName("debug") {
            signingConfig = persistentSigning
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = persistentSigning
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
