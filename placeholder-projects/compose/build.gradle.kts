import Build.addSubprojectsTasks

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
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

  val jsMain by sourceSets.getting

  val addNpmDependencies = project.addSubprojectsTasks(jsMain, "jsBrowserProductionWebpack")

  jsMain.dependencies {
    implementation(project(":pure"))
    implementation(Dependencies.serialization)

    implementation(compose.web.core)
    implementation(compose.runtime)

    addNpmDependencies()
  }
}
