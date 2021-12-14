import java.util.*

plugins {
  java
  jacoco
  `maven-publish`
  kotlin("jvm") version "1.6.10"
  id("java-gradle-plugin")
  id("net.kyori.indra.license-header") version "1.3.1"
}

group = "com.github.1c-syntax"
version = "0.4.0"

repositories {
  mavenLocal()
  mavenCentral()
}

val junitVersion = "5.6.1"

dependencies {
  compileOnly(gradleApi())
  implementation("commons-io", "commons-io", "2.6")

  testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
  testImplementation("org.assertj", "assertj-core", "3.18.1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "11"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
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

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
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
