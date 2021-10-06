object Dependencies {

  const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2"

  fun getImportsFor(mode: String): List<String> = when (mode) {
    "compose" -> pure + compose
    "react" -> pure + react
    else -> pure
  }.toList()

  private val pure = setOf(
    "kotlinx.browser.*",
    "kotlinx.html.*",
    "kotlinx.html.dom.*",
  )

  private val compose = setOf(
    "androidx.compose.runtime.*",
    "kotlinx.browser.document",
    "kotlinx.browser.window",
    "kotlinx.coroutines.delay",
    "org.jetbrains.compose.web.attributes.InputType",
    "org.jetbrains.compose.web.attributes.checked",
    "org.jetbrains.compose.web.attributes.disabled",
    "org.jetbrains.compose.web.css.marginTop",
    "org.jetbrains.compose.web.css.px",
    "org.jetbrains.compose.web.dom.*",
    "org.jetbrains.compose.web.renderComposable",
    "org.w3c.dom.HTMLElement",
    "org.w3c.dom.events.KeyboardEvent",
    "org.w3c.dom.get",
    "kotlin.js.Date",
  )

  private val react = pure + setOf(
    "react.dom.render",
    "kotlinx.browser.document",
    "kotlinx.browser.window",
    "kotlinx.css.*",
    "kotlinx.html.InputType",
    "kotlinx.html.js.onChangeFunction",
    "org.w3c.dom.HTMLInputElement",
    "react.RBuilder",
    "react.RComponent",
    "react.RProps",
    "react.RState",
    "react.dom.attrs",
    "styled.StyleSheet",
    "styled.css",
    "styled.styledDiv",
    "styled.styledInput",
  )
}
