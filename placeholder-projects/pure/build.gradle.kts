plugins {
    kotlin("js") version "1.5.30"
}

group = "org.jetbrains.k-js-html"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.2")
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}

tasks.register("replaceInputFile") {
    replaceInputFile()
}

tasks.assemble {
    mustRunAfter("replaceInputFile")
}

tasks.register<Copy>("copyAssembled") {
    dependsOn("assemble")
    from(layout.buildDirectory.dir("distributions/${rootProject.name}.js"))
    from(layout.buildDirectory.dir("distributions/${rootProject.name}.js.map"))
    into(getOutputDir())
}

tasks.register<GradleBuild>("stage") {
    tasks = listOf("replaceInputFile", "assemble", "copyAssembled")
}

fun replaceInputFile() {
    val inputScript = getInputFile().let(::File).readText()
    val placeholderFile = layout.projectDirectory.file("src/main/kotlin/Script.kt").asFile
    val main = "fun main()"
    val inputText = placeholderFile.readText()
    val startIndex = inputText.indexOf(main) + main.length + 1
    val resultScript = inputText.replaceRange(startIndex, inputText.length, "{\n$inputScript\n}")
    placeholderFile.writeText(resultScript)
}

fun getOutputDir() = getEnv(envName = "OUTPUT_DIR", defaultValue = "./").let(::File).let {
    if (!it.isDirectory) throw IllegalStateException("Output dir $it is not a directory")
    if (!it.exists()) throw IllegalStateException("Output dir $it doesn't exist")
    it.absolutePath
}

fun getInputFile() = getEnv(envName = "INPUT_FILE", defaultValue = "input").let(::File).let {
    if (it.isDirectory) throw IllegalStateException("Input file $it is a directory")
    if (!it.exists()) throw IllegalStateException("Input file $it doesn't exist")
    it.absolutePath
}

fun getEnv(envName: String, defaultValue: String? = null) =
    System.getenv(envName) ?: defaultValue ?: throw IllegalStateException("Not default value for $envName")
