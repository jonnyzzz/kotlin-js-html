plugins {
  kotlin("js")
}

allprojects {
  apply(plugin = "org.jetbrains.kotlin.js")

  group = "org.jetbrains.k-js-html"
  version = "1.0"

  kotlin {
    js(LEGACY) {
      binaries.executable()
      browser {
        commonWebpackConfig {
          cssSupport.enabled = true
        }
      }
    }
  }

  tasks.register("replaceInputFile") {
    doFirst {
      replaceInputFile(project.name)
    }
  }

  tasks.getByName("browserDistribution") {
    mustRunAfter(":${project.name}:replaceInputFile")
  }

  tasks.register<Copy>("copyBrowserDistribution") {
    dependsOn(":${project.name}:browserDistribution")
    from(layout.buildDirectory.dir("distributions/${project.name}.js"))
    from(layout.buildDirectory.dir("distributions/${project.name}.js.map"))
    into(getOutputDir())
  }

  tasks.register<GradleBuild>("stage") {
    tasks = listOf("replaceInputFile", "browserDistribution", "copyBrowserDistribution").map { ":${project.name}:$it" }
  }
}

fun replaceInputFile(subprojectName: String) {
  val inputScript = getInputFile().let(::File).readText()
  val placeholderFile = layout.projectDirectory.file("$subprojectName/src/main/kotlin/Script.kt").asFile
  val import = "import"
  val inputText = placeholderFile.readText()
  val startIndex = inputText.indexOf("\n", startIndex = inputText.lastIndexOf(import)) + 1
  val resultScript = inputText.replaceRange(startIndex, inputText.length, "\n$inputScript")
  placeholderFile.writeText(resultScript)
}

fun getOutputDir() = getEnv(envName = "OUTPUT_DIR", defaultValue = "./").let(::File).let {
  if (!it.isDirectory) throw IllegalStateException("Output dir $it is not a directory")
  if (!it.exists()) throw IllegalStateException("Output dir $it doesn't exist")
  it.absolutePath
}

fun getInputFile() = getEnv(envName = "INPUT_FILE").let(::File).let {
  if (it.isDirectory) throw IllegalStateException("Input file $it is a directory")
  if (!it.exists()) throw IllegalStateException("Input file $it doesn't exist")
  it.absolutePath
}

fun getEnv(envName: String, defaultValue: String? = null) =
  System.getenv(envName) ?: defaultValue ?: throw IllegalStateException("Not default value for $envName")
