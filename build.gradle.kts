// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add Firebase classpath if it’s not already included
        classpath("com.google.gms:google-services:4.4.2")  // Make sure it matches the version you're using
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.3")  // If using crashlytics
    }
}