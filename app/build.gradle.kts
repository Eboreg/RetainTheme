plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

@Suppress("PropertyName")
val VERSION = "4.5.1"

group = "us.huseli"
version = VERSION

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "us.huseli.retaintheme"
    compileSdk = 34

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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-common-ktx:2.8.1")
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
