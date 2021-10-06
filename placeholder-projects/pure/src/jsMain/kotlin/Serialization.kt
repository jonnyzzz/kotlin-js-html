import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

inline fun <reified T> fetchJson(
  url: String,
  jsonFormat: Json = Json { ignoreUnknownKeys = true },
  crossinline data: (T) -> dynamic /* Unit or Promise */,
): Promise<Unit> {
  return window.fetch(url, RequestInit(method = "GET", referrerPolicy = "no-referrer"))
    .then(Response::text)
    .then { text ->
      console.log("received: $text")
      val items = try {
        jsonFormat.decodeFromString<T>(text)
      } catch (t: Throwable) {
        console.error("Failed to parse data. ${t.message}", t)
        return@then
      }
      data(items)
    }
    .catch { console.error("Failed to fetch data. ${it.message}", it) }
}
