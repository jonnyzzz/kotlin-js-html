import Build.addSubprojectsTasks
import Build.getOutputFileName

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
}

kotlin {
  js(IR) {
    useCommonJs()
    binaries.executable()
    browser {
      webpackTask {
        cssSupport.enabled = true
        sourceMaps = true
        outputFileName = getOutputFileName()
      }
    }
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
  project.addSubprojectsTasks(mainSourceSet)
}
