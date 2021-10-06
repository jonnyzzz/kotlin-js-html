import kotlinx.browser.*
import kotlinx.html.*
import kotlinx.html.dom.*



@JsModule("is-sorted")
@JsNonModule
external fun <T> sorted(a: Array<T>): Boolean

fun main() {
  window.alert("Hello Kotlin/JS in HTML world")
  val data = arrayOf(3,1,2)
  window.alert("$data is ${if (sorted(data)) "" else "not "}sorted")
}
