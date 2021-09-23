import Build.addSubprojectsTasks

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
        outputFileName = "script.js"
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("main")
  project.addSubprojectsTasks(mainSourceSet)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
}
