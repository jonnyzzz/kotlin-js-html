import Build.addTasks

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(compose.web.core)
        implementation(compose.runtime)
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("jsMain")
  project.addTasks(mainSourceSet)
}
