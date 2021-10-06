import kotlin.test.Test
import kotlin.test.assertEquals


internal class FileTransformationTest {

  @Test
  fun `pure - test wrapping into main when not present`() {
    val input = """
      fun main() = Unit
      """
    val output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main() = Unit
      """
    testTransformation(input, output)
  }

  @Test
  fun `react - test wrapping into main when not present`() {
    val input = """
      useReact()
      fun main() = Unit
      """
    val output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*
      import react.dom.render
      import kotlinx.browser.document
      import kotlinx.browser.window
      import kotlinx.css.*
      import kotlinx.html.InputType
      import kotlinx.html.js.onChangeFunction
      import org.w3c.dom.HTMLInputElement
      import react.RBuilder
      import react.RComponent
      import react.RProps
      import react.RState
      import react.dom.attrs
      import styled.StyleSheet
      import styled.css
      import styled.styledDiv
      import styled.styledInput

      fun main() = Unit
      """
    testTransformation(input, output)
  }

  @Test
  fun `compose - test wrapping into main when not present`() {
    val input = """
      useCompose()
      fun main() = Unit
      """
    val output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*
      import androidx.compose.runtime.*
      import kotlinx.browser.document
      import kotlinx.browser.window
      import kotlinx.coroutines.delay
      import org.jetbrains.compose.web.attributes.InputType
      import org.jetbrains.compose.web.attributes.checked
      import org.jetbrains.compose.web.attributes.disabled
      import org.jetbrains.compose.web.css.marginTop
      import org.jetbrains.compose.web.css.px
      import org.jetbrains.compose.web.dom.*
      import org.jetbrains.compose.web.renderComposable
      import org.w3c.dom.HTMLElement
      import org.w3c.dom.events.KeyboardEvent
      import org.w3c.dom.get
      import kotlin.js.Date

      fun main() = Unit
      """
    testTransformation(input, output)
  }

  private fun testTransformation(
    input: String,
    output: String,
    dependencies: Iterable<NpmDependency> = emptyList()
  ) {
    val trimmedInput = input.trimIndent()
    val trimmedOutput = output.trimIndent()
    val actualDependencies = extractAllDependencies(trimmedInput)
    assertEquals(dependencies.toSet(), actualDependencies.toSet(), "Extracted dependencies don't match")
    val actualOutput = modifyInputFile(trimmedInput)
    assertEquals(trimmedOutput, actualOutput, "Transformed output files don't match")
  }
}
