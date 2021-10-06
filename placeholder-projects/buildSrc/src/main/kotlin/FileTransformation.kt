import Specification.COMPOSE_REGEX
import Specification.DEPENDENCY_DECLARATION
import Specification.DEPENDENCY_REGEX
import Specification.MAIN_REGEX
import Specification.REACT_REGEX

fun determinePlaceholderProject(content: String): String = when {
  COMPOSE_REGEX.containsMatchIn(content) -> "compose"
  REACT_REGEX.containsMatchIn(content) -> "react"
  else -> "pure"
}

fun extractAllDependencies(content: String): List<NpmDependency> =
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

fun modifyInputFile(content: String): String {
  val projectName = determinePlaceholderProject(content)
  return removeProjectDefinition(content)
    .let(::wrapInMainCallIfNeeded)
    .let { addImports(it, projectName) }
}

private fun addImports(content: String, projectName: String) =
  Dependencies.getImportsFor(projectName).joinToString(separator = "\n", postfix = "\n\n$content") { "import $it" }

private fun removeProjectDefinition(content: String): String =
  content
    .replace(COMPOSE_REGEX, "")
    .replace(REACT_REGEX, "")
    .replace(DEPENDENCY_REGEX, "")

private fun wrapInMainCallIfNeeded(content: String): String =
  if (MAIN_REGEX.containsMatchIn(content)) content
  else "fun main() {\n$content\n}"

private fun extractNpmDependency(dependencyDefinition: String): NpmDependency? =
  dependencyDefinition.split(":").takeIf { it.size == 2 }?.let { NpmDependency(it[0], it[1]) }

data class NpmDependency(val name: String, val version: String)
