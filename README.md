# Kotlin/JS in HTML Project objectives

## JS script embedded in HTML

Create a JavaScript file that should be included in the HTML, so it would look for
any Kotlin script located in the HTML. It should contain some watcher for the new
parts of the HTML file that can appear during its lifetime in order to compile also
the scripts that can appear in HTML after some changes to the document.

The script would send the Kotlin script content to Lambda and receive the actual
status of compilation (like _compiling_, _compilation error_, _compiled_) and the
url with the location of the compiled script (on _compiled_ status).

After receiving the url of compiled Kotlin script (which would be _.js_ and
_.js.map_ files) the script should include them to the end of the HTML document.

**Extra notes**

For now, we would support only single block of ```<script type="language/kotlin">```
in order to simplify the process of its compilation and order in the HTML file -
it may be discussed later if anything better can be done in this area.

## Caching scripts responses using S3 Object λ

The λ is a container application that handles the POST requests with the scripts content,
looks for them if it was already compiled and is located in S3 or if it should be compiled.
It communicates with:

### S3 service that holds response JS files

In case the script was compiled in the past and its compilation result is already available,
the λ would know the url to the proper response files. There can be multiple of them if
needed.

### λ compiling Kotlin/JS script and storing it to S3

This λ contains the placeholder Kotlin/JS project (separate for plain Kotlin/JS, with React
and with Compose) and injects the received file to do the compilation process defined by
single Gradle task (_should upload of the result be part of the task???_)

## Intellij support for the Kotlin/JS in HTML - bonus

Try to include highlighting and understanding of the Kotlin scripts inside of HTML files
as it's done in case of JS files that are embedded in HTML inside
```<script type="application/javascript">``` block. It may include hacking current implementation
of HTML Language implementation in Intellij but seems to be worth including if the feature
would be used by wider audience.

## Sample usage

```html
<script src="https://kotlin.app/shim-1.5.30.js" />
<script type="language/kotlin">
   render(document.getElementById("root")) {
      h1 { +"This will be fun!" }
      div { +"It supports React out of the box" }
   }
</script>
<div id="root"/>
```

## Getting Started

Download links:
- SSH clone URL: [ssh://git@git.jetbrains.team/kt-js-html/kt-js-html.git](ssh://git@git.jetbrains.team/kt-js-html/kt-js-html.git)
- HTTPS clone URL: [https://git.jetbrains.team/kt-js-html/kt-js-html.git](https://git.jetbrains.team/kt-js-html/kt-js-html.git)
