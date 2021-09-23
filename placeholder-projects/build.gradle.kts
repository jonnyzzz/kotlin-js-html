subprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  group = "org.jetbrains.k-js-html"
  version = "1.0"
}

val fullDistBuild by tasks.creating {


  project.subprojects.map {
    dependsOn(":${it.path}:fullDistBuildInner")
  }
}
