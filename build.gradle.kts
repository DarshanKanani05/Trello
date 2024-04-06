import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false



    id ("com.google.gms.google-services") version "4.3.14" apply false
}