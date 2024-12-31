plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.compose")
}

@Suppress("PropertyName")
val VERSION = "4.7.0"

group = "us.huseli"
version = VERSION

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "us.huseli.retaintheme"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-common-ktx:2.8.5")
    // Gson:
    implementation("com.google.code.gson:gson:2.11.0")
    // Text diff:
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "us.huseli"
                artifactId = "retaintheme"
                version = VERSION

                from(components["release"])
            }
        }
    }
}
