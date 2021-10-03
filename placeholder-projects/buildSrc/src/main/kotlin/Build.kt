import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

object Build {

  private const val DEPENDENCY_DECLARATION = "useNpmPackage"

  private const val OUTPUT_DIR_ENV = "OUTPUT_DIR"

  private const val WS = "[ \\t]*"

  private val REACT_REGEX = noArgsFunctionCallRegex("useReact")

  private val COMPOSE_REGEX = noArgsFunctionCallRegex("useCompose")

  private val MAIN_REGEX = Regex("fun${WS}main${WS}[(]${WS}[)]")

  private val DEPENDENCY_REGEX = Regex("$DEPENDENCY_DECLARATION${WS}[(]${WS}\"(?:[^\"\\\\]|\\\\.)*\"${WS}[)]")

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

  private fun determinePlaceholderProject(content: String): String = when {
    COMPOSE_REGEX.containsMatchIn(content) -> "compose"
    REACT_REGEX.containsMatchIn(content) -> "react"
    else -> "pure"
  }

  private fun extractAllDependencies(content: String): List<NpmDependency> =
    DEPENDENCY_REGEX.findAll(content).mapNotNull { result ->
      content.substring(result.range)
        .removePrefix(DEPENDENCY_DECLARATION)
        .dropWhile(Char::isWhitespace)
        .removePrefix("(")
        .dropWhile(Char::isWhitespace)
        .removePrefix("\"")
        .removeSuffix(")")
        .dropLastWhile(Char::isWhitespace)
        .removeSuffix("\"")
        .let(::extractNpmDependency)
    }.toList()

  private fun Project.modifyInputFile(content: String) =
    addImports(removeProjectDefinition(wrapInMainCallIfNeeded(content)))

  private fun Project.addImports(content: String) =
    Dependencies.getImportsFor(name).joinToString(separator = "\n", postfix = "\n$content") { "import $it" }

  private fun wrapInMainCallIfNeeded(content: String): String =
    if (MAIN_REGEX.containsMatchIn(content)) content
    else "fun main() {\n$content\n}"

  private fun removeProjectDefinition(content: String): String =
    content
      .replace(COMPOSE_REGEX, "\n")
      .replace(REACT_REGEX, "\n")
      .replace(DEPENDENCY_REGEX, "\n")

  private fun noArgsFunctionCallRegex(fName: String) = Regex("$fName${WS}[(]${WS}[)]")

  private fun getInputFile() = getEnv(envName = "INPUT_FILE")?.let(::File)
    ?.takeIf { !it.isDirectory && it.exists() }?.absolutePath

  private fun Project.isRelevantProject(inputScript: String): Boolean {
    val projectName = determinePlaceholderProject(inputScript)
    return projectName == name
  }

  private fun Project.manageInputFile(inputScript: String, sourceSet: String) {
    val placeholderFile = layout.projectDirectory.file("src/$sourceSet/kotlin/Script.kt").asFile
    val resultScript = modifyInputFile(inputScript)
    placeholderFile.writeText(resultScript)
  }

  private fun getOutputDir(): String {
    val let = getEnv(envName = OUTPUT_DIR_ENV, defaultValue = "./")?.let(::File)?.absoluteFile
      ?: throw IllegalStateException("$OUTPUT_DIR_ENV not defined")
    let.mkdirs()
    return let.absolutePath
  }

  private fun getEnv(envName: String, defaultValue: String? = null): String? = System.getenv(envName) ?: defaultValue
}

data class NpmDependency(val name: String, val version: String)

typealias DependencyModifier = KotlinDependencyHandler.() -> Unit

private fun extractNpmDependency(dependencyDefinition: String): NpmDependency? =
  dependencyDefinition.split(":").takeIf { it.size == 2 }?.let { NpmDependency(it[0], it[1]) }
