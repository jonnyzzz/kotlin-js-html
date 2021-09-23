import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.register
import java.io.File

object Build {

  fun Project.addTasks(sourceSet: Named) {
    tasks.register("replaceInputFile") {
      doFirst {
        replaceInputFile(sourceSet.name)
      }
    }

    tasks.getByName("assemble") {
      mustRunAfter(":${project.name}:replaceInputFile")
    }

    tasks.register<Copy>("copyAssemble") {
      dependsOn(":${project.name}:assemble")
      from(layout.buildDirectory.dir("distributions/${project.name}.js"))
      from(layout.buildDirectory.dir("distributions/${project.name}.js.map"))
      into(getOutputDir())
    }

    tasks.register<GradleBuild>("stage") {
      tasks =
        listOf("replaceInputFile", "assemble", "copyAssemble").map { ":${project.name}:$it" }
    }
  }

  private fun Project.replaceInputFile(sourceSet: String) {
    val inputScript = getInputFile().let(::File).readText()
    val placeholderFile = layout.projectDirectory.file("src/$sourceSet/kotlin/Script.kt").asFile
    val import = "import"
    val inputText = placeholderFile.readText()
    val startIndex = inputText.indexOf("\n", startIndex = inputText.lastIndexOf(import)) + 1
    val resultScript = inputText.replaceRange(startIndex, inputText.length, "\n$inputScript")
    placeholderFile.writeText(resultScript)
  }

  private fun getOutputDir() = getEnv(envName = "OUTPUT_DIR", defaultValue = "./").let { File(it) }.let {
    if (!it.isDirectory) throw IllegalStateException("Output dir $it is not a directory")
    if (!it.exists()) throw IllegalStateException("Output dir $it doesn't exist")
    it.absolutePath
  }

  private fun getInputFile() = getEnv(envName = "INPUT_FILE").let(::File).let {
    if (it.isDirectory) throw IllegalStateException("Input file $it is a directory")
    if (!it.exists()) throw IllegalStateException("Input file $it doesn't exist")
    it.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String =
    System.getenv(envName) ?: defaultValue ?: throw IllegalStateException("Not default value for $envName")
}
