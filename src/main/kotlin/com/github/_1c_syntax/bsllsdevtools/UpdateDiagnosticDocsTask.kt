/*
 * This file is a part of BSLLS Development tools gradle plugin.
 *
 * Copyright (c) 2020-2022
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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UpdateDiagnosticDocsTask constructor() : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Updates diagnostic docs after changes"
    dependsOn(":classes")
    outputs.upToDateWhen { false }
  }

  private val templateDocHeader = "# <Description> (<DiagnosticKey>)\n\n" +
    "<!-- Блоки выше заполняются автоматически, не трогать -->\n"

  @TaskAction
  fun run() {
    var outputDir = project.projectDir;

    val diagnosticsMetadata = BSLLSSourceReader.getDiagnosticsMetadata(project)
    diagnosticsMetadata.forEach {
      updateDocFile(outputDir, "ru", it.key, it.value)
      updateDocFile(outputDir, "en", it.key, it.value)
    }
  }

  private fun updateDocFile(outputDir: File, lang: String, key: String, metadata: HashMap<String, Any>) {
    val docPath = Utils.diagnosticDocPath(outputDir, lang, key)
    val text = docPath.readText(charset("UTF-8"))

    var header = "## Описание диагностики"
    var footer = "## Сниппеты"

    val headerText = templateDocHeader
      .replace("<Description>", metadata["description_$lang"].toString())
      .replace("<DiagnosticKey>", key)

    if (lang == "en") {
      header = "## Description"
      footer = "## Snippets"
    }

    var index = text.indexOf(header)
    var newText = if (index < 0) {
      "$headerText$header\n\n$text"
    } else {
      "$headerText${text.substring(index)}"
    }

    index = newText.indexOf(footer)
    newText = if (index < 1) {
      "${newText.trimEnd()}\n"
    } else {
      "${newText.substring(0, index - 1).trimEnd()}\n"
    }

    docPath.writeText(newText, charset("UTF-8"))
  }
}
