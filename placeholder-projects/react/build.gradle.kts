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

  fun kotlinWrapper(target: String) = "org.jetbrains.kotlin-wrappers:kotlin-$target"
  val kotlinWrappersVersion = "0.0.1-pre.213-kotlin-1.5.10"

  jsMain.dependencies {
    implementation(project(":pure"))
    implementation(Dependencies.serialization)

    implementation(project.dependencies.enforcedPlatform(kotlinWrapper("wrappers-bom:${kotlinWrappersVersion}")))
    implementation(kotlinWrapper("react"))
    implementation(kotlinWrapper("react-dom"))
    implementation(kotlinWrapper("styled"))
    implementation(kotlinWrapper("react-router-dom"))
    implementation(kotlinWrapper("redux"))
    implementation(kotlinWrapper("react-redux"))

    addNpmDependencies()
  }
}
