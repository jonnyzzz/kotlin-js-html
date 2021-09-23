import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.configure
import java.io.File

data class BuildStatus(var project: String? = null, var dependencies: List<String> = emptyList())

object Build {

  private val REACT_REGEX = noArgsFunctionCallRegex("useReact")

  private val COMPOSE_REGEX = noArgsFunctionCallRegex("useCompose")

  private val MAIN_REGEX = Regex("fun[ \\t]main[ \\t]*[(][ \\t]*[)]")

  private val DEPENDENCY_REGEX = Regex("useNpmPackage[ \\t]*[(][ \\t]*\"(?:[^\"\\\\]|\\\\.)*\"[ \\t]*[)]")

  fun Project.addSubprojectsTasks(sourceSet: Named, distTask: String) {
    val fullDistBuildInnerTask = tasks.register("fullDistBuildInner")
    if (!isRelevantProject()) return

    val buildStatus = BuildStatus()

    val manageInputFileTask = tasks.register("manageInputFile") {
      doFirst {
        manageInputFile(sourceSet.name, buildStatus)
      }
    }

    tasks.getByName(distTask) {
      dependsOn(manageInputFileTask)
    }

    val copyResultTask = tasks.register("copyResult", Sync::class.java) {
      dependsOn(distTask)

      from(File(buildDir, "distributions"))
      into(getOutputDir())
    }

    fullDistBuildInnerTask.configure {
      dependsOn(manageInputFileTask)
      dependsOn(distTask)
      dependsOn(copyResultTask)
    }
  }

  fun Project.getOutputFileName(): String = "script.js"

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

  private fun modifyInputFile(content: String) =
    content.let(::wrapInMainCallIfNeeded).let(::removeProjectDefinition)

  private fun File.overwriteCopyTo(target: File) = copyTo(target, overwrite = true)

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

  fun Project.isRelevantProject() : Boolean {
    val inputScript = getInputFile()?.let(::File)?.readText() ?: return false

    val projName = determinePlaceholderProject(inputScript)
    println("Selected project name: $projName")
    return projName == this.name
  }

  private fun Project.manageInputFile(sourceSet: String, buildStatus: BuildStatus) {
    val inputScript = getInputFile()?.let(::File)?.readText() ?: return

    buildStatus.project = determinePlaceholderProject(inputScript)
    if (buildStatus.project != name) return

    buildStatus.dependencies = extractAllDependencies(inputScript)
    val placeholderFile = layout.projectDirectory.file("src/$sourceSet/kotlin/Script.kt").asFile
    val inputText = placeholderFile.readText()
    val startIndex = firstIndexAfterImports(inputText)
    val modifiedInputScript = modifyInputFile(inputScript)
    val resultScript = inputText.replaceRange(startIndex, inputText.length, "\n$modifiedInputScript")
    placeholderFile.writeText(resultScript)
  }

  private fun firstIndexAfterImports(text: String): Int {
    val lastImport = text.lastIndexOf("import")
    if (lastImport == -1) return 0
    return text.indexOf("\n", startIndex = lastImport) + 1
  }

  private fun getOutputDir(): String {
    val let = getEnv(envName = "OUTPUT_DIR", defaultValue = "./")?.let(::File)?.absoluteFile ?: throw IllegalStateException("OUTPUT_DIR not defined")
    let.mkdirs()
    return let.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String? = System.getenv(envName) ?: defaultValue
}
