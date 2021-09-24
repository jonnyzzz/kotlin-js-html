import Build.addSubprojectsTasks

plugins {
  kotlin("js")
  kotlin("plugin.serialization")
}

kotlin {
  js(IR) {
    useCommonJs()
    binaries.executable()
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
        sourceMaps = true
      }
    }
  }
  val mainSourceSet = sourceSets.getByName("main")
  addSubprojectsTasks(mainSourceSet, "browserProductionWebpack")
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}
