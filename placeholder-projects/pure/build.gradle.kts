import Build.addSubprojectsTasks

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
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
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    addNpmDependencies()
  }
}
