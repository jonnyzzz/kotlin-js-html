import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.js.json

inline fun <reified T> fetchJson(url: String, crossinline data: (T) -> dynamic /*Unit or Promise*/) {
  window.fetch(json("url" to url, "referrerPolicy" to "no-referrer")).then {
    it.text()
  }.then { text ->
    val items = try {
      Json {
        this.ignoreUnknownKeys = true
      }.decodeFromString<T>(text)
    } catch (t: Throwable) {
      console.error("Failed to parse data. ${t.message}", t)
      return@then
    }

    data(items)
  }
}
