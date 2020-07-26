/**
 * This file is a part of BSLLS Development tools gradle plugin.
 *
 * Copyright © 2020-2020
 * Valery Maximov <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSLLS Development tools gradle plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSLLS Development tools gradle plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSLLS Development tools gradle plugin.
 */
package com.github._1c_syntax.bsllsdevtools

import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.nio.charset.StandardCharsets

open class NewDiagnosticTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Creating new diagnostics files"
    dependsOn(":jar")
  }

  @Option(option = "key", description = "Diagnostic key (required)")
  private var key = ""

  @Option(option = "nameRu", description = "Diagnostic name in Russian (optional)")
  private var nameRu = "<Имя диагностики>"

  @Option(option = "nameEn", description = "Diagnostic name in English (optional)")
  private var nameEn = "<Diagnostic name>"

  fun setKey(key: String) {
    this.key = key
  }

  fun setNameRu(nameRu: String) {
    this.nameRu = nameRu
  }

  fun setNameEn(nameEn: String) {
    this.nameEn = nameEn
  }

  @OutputDirectory
  val outputDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun run() {
    if (key.isEmpty()) {
      throw Throwable("Empty diagnostic key")
    }
    val tags = BSLLSSourceReader.getDiagnosticTags(project)
    println(
      "Diagnostic tags: \n" + tags.map {
        it.key
      }.joinToString("\n")
    )

    println("Enter diagnostic tags separated by space (max 3).")

    val inputStr = readLine()
    if (inputStr.isNullOrBlank()) {
      throw Throwable("Empty diagnostic tags")
    }
    var diagnosticTags = inputStr.split(' ').joinToString(",\n    DiagnosticTag.")
    diagnosticTags = "tags = {\n    DiagnosticTag.$diagnosticTags\n  }\n"

    logger.quiet("Creating new diagnostics files with the key '{}'", key)
    val srcPath = File(outputDir.get().asFile.path, "src")
    val packPath = "com/github/_1c_syntax/bsl/languageserver/diagnostics"
    val docPath = File(outputDir.get().asFile.path, "docs")

    createFile(
      "${docPath}/diagnostics/${key}.md",
      getText("Template_ru.md")
        .replace("<Имя диагностики>", nameRu)
        .replace("<DiagnosticKey>", key)
    )

    createFile(
      "${docPath}/en/diagnostics/${key}.md",
      getText("Template_en.md")
        .replace("<Diagnostic name>", nameEn)
        .replace("<DiagnosticKey>", key)
    )

    createFile(
      "${srcPath}/main/java/${packPath}/${key}Diagnostic.java",
      getText("TemplateDiagnostic.java")
        .replace("Template", key)
        .replace("\$diagnosticTags", diagnosticTags)
    )

    createFile(
      "${srcPath}/test/java/${packPath}/${key}DiagnosticTest.java",
      getText("TemplateDiagnosticTest.java")
        .replace("Template", key)
    )

    createFile(
      "${srcPath}/main/resources/${packPath}/${key}Diagnostic_ru.properties",
      getText("TemplateDiagnostic_ru.properties")
        .replace("<Имя диагностики>", nameRu)
    )

    createFile(
      "${srcPath}/main/resources/${packPath}/${key}Diagnostic_en.properties",
      getText("TemplateDiagnostic_en.properties")
        .replace("<Diagnostic name>", nameEn)
    )

    createFile("${srcPath}/test/resources/diagnostics/${key}Diagnostic.bsl", "\n")
  }

  private fun createFile(path: String, text: String) {
    val f = File(path)
    f.writeText(text, charset("UTF-8"))
    logger.quiet("  Created file '{}'", f.absoluteFile)
  }

  private fun getText(name: String): String {
    return IOUtils.resourceToString(
      name,
      StandardCharsets.UTF_8,
      this.javaClass.classLoader
    )
  }
}