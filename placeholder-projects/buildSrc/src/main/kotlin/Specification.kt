object Specification {

  private const val WS = "[ \\t]"

  private const val WS_GEQZ = "$WS*"

  private const val WS_GZ = "$WS+"

  const val DEPENDENCY_DECLARATION = "useNpmPackage"

  val REACT_REGEX = noArgsSingleLineFunctionCallRegex("useReact")

  val COMPOSE_REGEX = noArgsSingleLineFunctionCallRegex("useCompose")

  val MAIN_REGEX = Regex("fun${WS_GZ}main${WS_GEQZ}[(]${WS_GEQZ}[)]")

  val DEPENDENCY_REGEX = Regex("$DEPENDENCY_DECLARATION${WS_GEQZ}[(]${WS_GEQZ}\"(?:[^\"\\\\]|\\\\.)*\"${WS_GEQZ}[)]")

  private fun noArgsSingleLineFunctionCallRegex(fName: String) = Regex("$fName${WS_GEQZ}[(]${WS_GEQZ}[)]${WS_GEQZ}[\n]")
}
