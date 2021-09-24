rootProject.name = "placeholder-projects"

pluginManagement {
  buildscript {
    repositories {
      google()
      gradlePluginPortal()
      maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
      maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
      classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
      classpath("org.jetbrains.compose:compose-gradle-plugin:1.0.0-alpha3")
      classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.21")
    }
  }
}

include("compose")
include("pure")
include("react")
