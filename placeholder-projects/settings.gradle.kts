rootProject.name = "placeholder-projects"

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

pluginManagement {
  buildscript {
    repositories {
      maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
      classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
  }
}

//include("compose")
include("pure")
include("react")
