import Build.addSubprojectsTasks
import Build.getOutputFileName

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
        outputFileName = getOutputFileName()
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("main")
  project.addSubprojectsTasks(mainSourceSet)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
}
