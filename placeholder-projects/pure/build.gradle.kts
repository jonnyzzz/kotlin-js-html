import Build.addSubprojectsTasks
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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
  val mainSourceSet: KotlinSourceSet = sourceSets.getByName("main")
  addSubprojectsTasks(mainSourceSet)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
}
