import java.util.*

plugins {
  java
  jacoco
  id("maven-publish")
  kotlin("jvm") version "1.9.20"
  id("java-gradle-plugin")
  id("org.cadixdev.licenser") version "0.6.1"
  id("com.gradle.plugin-publish") version "1.2.1"
}

pluginBundle {
  website = "https://github.com/1c-syntax/bslls-dev-tools"
  vcsUrl = "https://github.com/1c-syntax/bslls-dev-tools.git"
  tags = listOf("bslls", "dev-tools")
}

group = "io.github.1c-syntax"
version = "0.7.3"

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
      id = "io.github.1c-syntax.bslls-dev-tools"
      implementationClass = "com.github._1c_syntax.bsllsdevtools.BSLDeveloperToolsPlugin"
      displayName = "BSLLS Development tools gradle plugin"
      description = "BSLLS Development tools gradle plugin"
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
