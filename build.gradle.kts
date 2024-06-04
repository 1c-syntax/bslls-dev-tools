import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.*

plugins {
  java
  jacoco
  id("maven-publish")
  kotlin("jvm") version "2.0.0"
  id("java-gradle-plugin")
  id("org.cadixdev.licenser") version "0.6.1"
  id("com.gradle.plugin-publish") version "1.2.1"
}

gradlePlugin {
  website.set("https://github.com/1c-syntax/bslls-dev-tools")
  vcsUrl.set("https://github.com/1c-syntax/bslls-dev-tools.git")
  plugins {
    create("bslls-dev-tools") {
      id = "io.github.1c-syntax.bslls-dev-tools"
      implementationClass = "com.github._1c_syntax.bsllsdevtools.BSLDeveloperToolsPlugin"
      displayName = "BSLLS Development tools gradle plugin"
      description = "BSLLS Development tools gradle plugin"
      tags.set(listOf("bslls", "dev-tools", "bsl-language-server", "1c-syntax"))
    }
  }
}

group = "io.github.1c-syntax"
version = "0.8.0"

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  compileOnly(gradleApi())
  implementation(kotlin("stdlib-jdk8"))
  implementation("commons-io", "commons-io", "2.6")

  testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.1")
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.6.1")
  testImplementation("org.assertj", "assertj-core", "3.18.1")
}

kotlin {
  compilerOptions {
    apiVersion.set(KotlinVersion.KOTLIN_2_0)
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed")
  }

  reports {
    html.required.set(true)
  }
}

tasks.check {
  dependsOn(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  reports {
    xml.required.set(true)
    xml.outputLocation.set(File("$buildDir/reports/jacoco/test/jacoco.xml"))
  }
}

license {
  header(rootProject.file("license/HEADER.txt"))
  newLine(false)
  ext["year"] = "2020-" + Calendar.getInstance().get(Calendar.YEAR)
  ext["name"] = "Valery Maximov <maximovvalery@gmail.com>"
  ext["project"] = "BSLLS Development tools gradle plugin"
  exclude("**/*.bin")
  exclude("**/*.html")
  exclude("**/*.properties")
  exclude("**/*.xml")
  exclude("**/*.json")
  exclude("**/*.os")
  exclude("**/*.bsl")
  exclude("**/*.orig")
  exclude("**/Template*")
}
