// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    dependencies {
        // Updated Hilt Android Gradle plugin version to match app-level dependencies
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    }
    repositories {
        google()
        mavenCentral()
    }
}
