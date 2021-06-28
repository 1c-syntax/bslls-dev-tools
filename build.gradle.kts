import java.util.*

plugins {
  java
  jacoco
  kotlin("jvm") version "1.3.72"
  `maven-publish`
  id("java-gradle-plugin")
  id("com.github.hierynomus.license") version "0.15.0"
}

group = "com.github.1c-syntax"
version = "0.4.0"

repositories {
  mavenLocal()
  mavenCentral()
}

val junitVersion = "5.6.1"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  compileOnly(gradleApi())
  implementation("commons-io", "commons-io", "2.6")

  testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
  testImplementation("org.assertj", "assertj-core", "3.18.1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}

gradlePlugin {
  plugins {
    create("bslls-dev-tools") {
      id = "com.github.1c-syntax.bslls-dev-tools"
      implementationClass = "com.github._1c_syntax.bsllsdevtools.BSLDeveloperToolsPlugin"
    }
  }
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed")
  }

  reports {
    html.isEnabled = true
  }
}

tasks.check {
  dependsOn(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
    xml.destination = File("$buildDir/reports/jacoco/test/jacoco.xml")
  }
}

license {
  header = rootProject.file("license/HEADER.txt")
  ext["year"] = "2020-" + Calendar.getInstance().get(Calendar.YEAR)
  ext["name"] = "Valery Maximov <maximovvalery@gmail.com>"
  ext["project"] = "BSLLS Development tools gradle plugin"
  strictCheck = true
  mapping("java", "SLASHSTAR_STYLE")
  excludes(
    listOf(
      "**/Template*",
      "**/*.bin",
      "**/*.html",
      "**/*.properties",
      "**/*.xml",
      "**/*.json",
      "**/*.os",
      "**/*.bsl",
      "**/*.orig"
    )
  )
}