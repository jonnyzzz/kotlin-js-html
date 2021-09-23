import Build.determinePlaceholderProject
import Build.extractAllDependencies
import Build.modifyInputFile
import Build.readInputFile
import Build.writeInputFile

subprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  group = "org.jetbrains.k-js-html"
  version = "1.0"
}

val subprojectsTasks = project.subprojects.map { ":${it.name}:fullDistBuild" }

subprojectsTasks.zipWithNext()
  .forEach { (fst, snd) -> project.tasks.getByPath(fst).mustRunAfter(project.tasks.getByPath(snd)) }

tasks.register<GradleBuild>("fullDistBuild") {
  tasks = subprojectsTasks
}
