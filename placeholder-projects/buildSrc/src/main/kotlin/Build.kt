import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

object Build {

  private const val OUTPUT_DIR_ENV = "OUTPUT_DIR"

  private const val INPUT_FILE_ENV = "INPUT_FILE"

  fun Project.addSubprojectsTasks(sourceSet: Named, distributionTaskName: String): DependencyModifier {
    val fullDistBuildInnerTask = tasks.register("fullDistBuildInner")
    val inputScript = getInputFile()?.let(::File)?.readText() ?: return {}
    if (!isRelevantProject(inputScript)) return {}

    val manageInputFileTask = tasks.register("manageInputFile") {
      doFirst {
        manageInputFile(inputScript, sourceSet.name)
      }
    }

    tasks.withType(KotlinCompile::class.java) {
      dependsOn(manageInputFileTask)
    }

    val cleanTask = tasks.getByName("clean") {
      dependsOn(manageInputFileTask)
    }

    val distributionTask = tasks.getByName(distributionTaskName) {
      dependsOn(cleanTask)
    }

    val copyResultTask = tasks.register("copyResult", Copy::class.java) {
      dependsOn(distributionTask)

      from(File(buildDir, "distributions").resolve("${project.name}.js"))
      from(File(buildDir, "distributions").resolve("${project.name}.js.map"))
      into(getOutputDir())
    }

    fullDistBuildInnerTask.configure {
      dependsOn(copyResultTask)
      dependsOn(distributionTask)
      dependsOn(cleanTask)
      dependsOn(manageInputFileTask)
    }

    return {
      extractAllDependencies(inputScript).forEach {
        implementation(npm(it.name, it.version))
      }
    }
  }

  private fun Project.isRelevantProject(inputScript: String): Boolean {
    val projectName = determinePlaceholderProject(inputScript)
    return projectName == name
  }

  private fun getInputFile() = getEnv(INPUT_FILE_ENV)?.let(::File)
    ?.takeIf { !it.isDirectory && it.exists() }?.absolutePath

  private fun Project.manageInputFile(inputScript: String, sourceSet: String) {
    val placeholderFile = layout.projectDirectory.file("src/$sourceSet/kotlin/Script.kt").asFile
    val resultScript = modifyInputFile(inputScript)
    placeholderFile.writeText(resultScript)
  }

  private fun getOutputDir(): String {
    val let = getEnv(OUTPUT_DIR_ENV, defaultValue = "./")?.let(::File)?.absoluteFile
      ?: throw IllegalStateException("$OUTPUT_DIR_ENV not defined")
    let.mkdirs()
    return let.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String? = System.getenv(envName) ?: defaultValue
}

typealias DependencyModifier = KotlinDependencyHandler.() -> Unit
