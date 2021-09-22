plugins {
    kotlin("js") version "1.5.10"
}

group = "org.jetbrains.k-js-html"
version = "1.0"

val kotlinxHtmlV = "0.7.2"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlV")
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