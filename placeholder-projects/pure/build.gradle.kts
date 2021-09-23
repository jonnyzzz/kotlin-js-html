import Build.addTasks

plugins {
  kotlin("js")
}

kotlin {
  js(LEGACY) {
    binaries.executable()
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("main")
  project.addTasks(mainSourceSet)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
}
