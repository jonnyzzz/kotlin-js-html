import Build.addTasks

plugins {
  kotlin("js")
}

kotlin {
  js(IR) {
    useCommonJs()
    binaries.executable()
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
        sourceMaps = true
        showProgress = true
        progressReporter = true
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("main")
  project.addTasks(mainSourceSet)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
}
