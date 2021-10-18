plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.5.21")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
