import kotlin.test.Test
import kotlin.test.assertEquals


internal class FileTransformationTest {

  @Test
  fun `pure - test wrapping into main when main not present - base`() = testTransformation(
    input = """
      window.alert("Hello Kotlin/JS in HTML world")
      """,
    output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main() {
      window.alert("Hello Kotlin/JS in HTML world")
      }
      """
  )

  @Test
  fun `pure - test wrapping into main when main not present - tricky whitespace`() = testTransformation(
    input = """
      fun funmain() = window.alert("Hello Kotlin/JS in HTML world")
      funmain()
      """,
    output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main() {
      fun funmain() = window.alert("Hello Kotlin/JS in HTML world")
      funmain()
      }
      """
  )

  @Test
  fun `pure - test wrapping into main when main present - expression`() = testTransformation(
    input = """
      fun main() = window.alert("Hello Kotlin/JS in HTML world")
      """,
    output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main() = window.alert("Hello Kotlin/JS in HTML world")
      """
  )

  @Test
  fun `pure - test wrapping into main when main present - block`() = testTransformation(
    input = """
      fun main() {
          window.alert("Hello Kotlin/JS in HTML world")
      }
      """,
    output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main() {
          window.alert("Hello Kotlin/JS in HTML world")
      }
      """
  )

  @Test
  fun `pure - test wrapping into main when main present - unusual declaration`() = testTransformation(
    input = """
      fun main (      )
      {
          window.alert("Hello Kotlin/JS in HTML world")
      }
      """,
    output = """
      import kotlinx.browser.*
      import kotlinx.html.*
      import kotlinx.html.dom.*

      fun main (      )
      {
          window.alert("Hello Kotlin/JS in HTML world")
      }
      """
  )

  @Test
  fun `react - test wrapping into main when present - base`() = testTransformation(
    input = """
      useReact()
      fun main() = Unit
      """,
    output = """
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
  )


  @Test
  fun `compose - test wrapping into main when present - base`() = testTransformation(
    input = """
      useCompose()
      fun main() = Unit
      """,
    output = """
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
  )

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
