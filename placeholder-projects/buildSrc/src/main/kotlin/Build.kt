import org.gradle.api.Named
import org.gradle.api.Project
import java.io.File

data class BuildStatus(var project: String? = null, var dependencies: List<String> = emptyList())

object Build {

  private val REACT_REGEX = noArgsFunctionCallRegex("useReact")

  private val COMPOSE_REGEX = noArgsFunctionCallRegex("useCompose")

  private val MAIN_REGEX = Regex("fun[ \\t]main[ \\t]*[(][ \\t]*[)]")

  private val DEPENDENCY_REGEX = Regex("useNpmPackage[ \\t]*[(][ \\t]*\"(?:[^\"\\\\]|\\\\.)*\"[ \\t]*[)]")

  fun Project.addSubprojectsTasks(sourceSet: Named) {
    val distTask = getDistTaskName(sourceSet)
    val buildStatus = BuildStatus()

    tasks.register("manageInputFile") {
      doFirst {
        manageInputFile(sourceSet.name, buildStatus)
      }
    }

    tasks.getByName(distTask) {
      mustRunAfter("manageInputFile")
    }

    tasks.register("copyResult") {
      dependsOn(distTask)
      doFirst {
        if (buildStatus.project != project.name) return@doFirst

        layout.buildDirectory.file("distributions/script.js").get().asFile
          .overwriteCopyTo(File(getOutputDir(), "script.js"))
        layout.buildDirectory.file("distributions/script.js.map").get().asFile
          .overwriteCopyTo(File(getOutputDir(), "script.js.map"))
      }
    }

    tasks.register("fullDistBuild") {
      dependsOn("manageInputFile")
      dependsOn(distTask)
      dependsOn("copyResult")
    }
  }

  fun determinePlaceholderProject(content: String): String = when {
    COMPOSE_REGEX.containsMatchIn(content) -> "compose"
    REACT_REGEX.containsMatchIn(content) -> "react"
    else -> "pure"
  }

  fun extractAllDependencies(content: String): List<String> =
    DEPENDENCY_REGEX.findAll(content).map { result ->
      content.substring(result.range)
        .drop("useNpmPackage".length)
        .dropWhile(Char::isWhitespace)
        .drop("(".length)
        .dropWhile(Char::isWhitespace)
        .dropLast(")".length)
        .dropLastWhile(Char::isWhitespace)
    }.toList()

  fun modifyInputFile(content: String) =
    content.let(::wrapInMainCallIfNeeded).let(::removeProjectDefinition)

  fun readInputFile(): String = getInputFile().let(::File).readText()

  fun writeInputFile(content: String) = getInputFile().let(::File).writeText(content)

  private fun getDistTaskName(sourceSet: Named): String =
    (sourceSet.name.dropLastWhile { it.isLowerCase() }.dropLast(1) + "BrowserDistribution").decapitalize()

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

  private fun getInputFile() = getEnv(envName = "INPUT_FILE").let(::File).let {
    if (it.isDirectory) throw IllegalStateException("Input file $it is a directory")
    if (!it.exists()) throw IllegalStateException("Input file $it doesn't exist")
    it.absolutePath
  }

  private fun Project.manageInputFile(sourceSet: String, buildStatus: BuildStatus) {
    val inputScript = getInputFile().let(::File).readText()

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

  private fun getOutputDir(): String = getEnv(envName = "OUTPUT_DIR", defaultValue = "./").let { File(it) }.let {
    if (!it.isDirectory) throw IllegalStateException("Output dir $it is not a directory")
    if (!it.exists()) throw IllegalStateException("Output dir $it doesn't exist")
    it.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String =
    System.getenv(envName) ?: defaultValue ?: throw IllegalStateException("Not default value for $envName")
}
