subprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  group = "org.jetbrains.k-js-html"
  version = "1.0"
}

val environments = listOf("pure", "react", "compose")
val subBuilds = environments.map { "$it:fullDistBuildInner" }

subBuilds.zipWithNext()
  .forEach { (fst, snd) -> project.tasks.getByPath(fst).mustRunAfter(project.tasks.getByPath(snd)) }

tasks.register("fullDistBuild") {
  subBuilds.forEach { dependsOn(it) }
}
