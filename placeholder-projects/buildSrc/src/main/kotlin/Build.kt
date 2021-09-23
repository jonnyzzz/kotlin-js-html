import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import java.io.File

data class BuildStatus(val dependencies: MutableList<String> = mutableListOf())

object Build {

  private val REACT_REGEX = noArgsFunctionCallRegex("useReact")

  private val COMPOSE_REGEX = noArgsFunctionCallRegex("useCompose")

  private val MAIN_REGEX = Regex("fun[ \\t]main[ \\t]*[(][ \\t]*[)]")

  private val DEPENDENCY_REGEX = Regex("useNpmPackage[ \\t]*[(][ \\t]*\"(?:[^\"\\\\]|\\\\.)*\"[ \\t]*[)]")

  fun Project.addSubprojectsTasks(sourceSet: Named, distributionTaskName: String) {
    val fullDistBuildInnerTask = tasks.register("fullDistBuildInner")
    if (!isRelevantProject()) return

    val buildStatus = BuildStatus()

    tasks.register("manageInputFile") {
      doFirst {
        manageInputFile(sourceSet.name, buildStatus)
      }
    }

    tasks.getByName(distributionTaskName) {
      dependsOn("${project.path}:manageInputFile")
    }

    tasks.register("copyResult", Copy::class.java) {
      dependsOn("${project.path}:$distributionTaskName")

      from(File(buildDir, "distributions").resolve("${project.name}.js"))
      from(File(buildDir, "distributions").resolve("${project.name}.js.map"))
      into(getOutputDir())
    }

    fullDistBuildInnerTask.configure {
      dependsOn("${project.path}:copyResult")
      dependsOn("${project.path}:$distributionTaskName")
      dependsOn("${project.path}:manageInputFile")
    }
  }

  private fun determinePlaceholderProject(content: String): String = when {
    COMPOSE_REGEX.containsMatchIn(content) -> "compose"
    REACT_REGEX.containsMatchIn(content) -> "react"
    else -> "pure"
  }

  private fun extractAllDependencies(content: String): List<String> =
    DEPENDENCY_REGEX.findAll(content).map { result ->
      content.substring(result.range)
        .removePrefix("useNpmPackage")
        .dropWhile(Char::isWhitespace)
        .removePrefix("(")
        .dropWhile(Char::isWhitespace)
        .removeSuffix(")")
        .dropLastWhile(Char::isWhitespace)
    }.toList()

  private fun Project.modifyInputFile(content: String) =
    content.let { wrapInMainCallIfNeeded(it) }.let { removeProjectDefinition(it) }.let { addImports(it) }

  private fun Project.addImports(content: String) =
    Dependencies.imports.getOrDefault(name, listOf())
      .joinToString(separator = "\n", postfix = "\n$content") { "import $it" }

  private fun wrapInMainCallIfNeeded(content: String): String =
    if (MAIN_REGEX.containsMatchIn(content)) content
    else "fun main() {\n$content\n}"

  private fun removeProjectDefinition(content: String): String =
    content
      .replace(COMPOSE_REGEX, "\n")
      .replace(REACT_REGEX, "\n")
      .replace(DEPENDENCY_REGEX, "\n")

  private fun noArgsFunctionCallRegex(fName: String) = Regex("$fName[ \\t]*[(][ \\t]*[)]")

  private fun getInputFile() = getEnv(envName = "INPUT_FILE")?.let(::File)
    ?.takeIf { !it.isDirectory && it.exists() }?.absolutePath

  fun Project.isRelevantProject(): Boolean {
    val inputScript = getInputFile()?.let(::File)?.readText() ?: return false

    val projectName = determinePlaceholderProject(inputScript)
    return projectName == name
  }

  private fun Project.manageInputFile(sourceSet: String, buildStatus: BuildStatus) {
    val inputScript = getInputFile()?.let(::File)?.readText() ?: return
    buildStatus.dependencies += extractAllDependencies(inputScript)
    val placeholderFile = layout.projectDirectory.file("src/$sourceSet/kotlin/Script.kt").asFile
    val resultScript = modifyInputFile(inputScript)
    placeholderFile.writeText(resultScript)
  }

  private fun getOutputDir(): String {
    val let = getEnv(envName = "OUTPUT_DIR", defaultValue = "./")?.let(::File)?.absoluteFile
      ?: throw IllegalStateException("OUTPUT_DIR not defined")
    let.mkdirs()
    return let.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String? = System.getenv(envName) ?: defaultValue
}
