import Build.addSubprojectsTasks

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
      }
    }
  }

  val jsMain by sourceSets.getting {
    dependencies {
      implementation(compose.web.core)
      implementation(compose.runtime)
      implementation(project(":pure"))
    }
  }
  project.addSubprojectsTasks(jsMain, "jsBrowserProductionWebpack")
}
