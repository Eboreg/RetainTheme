// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // id("com.android.tools.build:gradle:8.0.2")
    // id("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        mavenCentral()
    }
}

tasks.register("clean", Delete::class.java) {
    delete(buildDir)
}
