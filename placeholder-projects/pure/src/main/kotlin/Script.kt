import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.div
import kotlinx.html.dom.append

fun main() {
    window.onload = {
        document.body?.append {
            div {
                +"Hello from JS"
            }
        }
    }
}