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
  implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.206-kotlin-1.5.10")
  implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.206-kotlin-1.5.10")
  implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-pre.206-kotlin-1.5.10")
  implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:5.2.0-pre.206-kotlin-1.5.10")
  implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.0.5-pre.206-kotlin-1.5.10")
  implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.3-pre.206-kotlin-1.5.10")
}
