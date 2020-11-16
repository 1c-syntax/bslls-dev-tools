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

import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.severityEnMap
import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.severityRuMap
import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.typeEnMap
import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.typeRuMap
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UpdateDiagnosticsIndexTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Update diagnostics index after changes"
    dependsOn(":jar")
    outputs.upToDateWhen { false }
  }

  private val templateIndexLine =
    "\n [<Name>](<Name>.md) | <Description> | <Activated> | <Severity> | <Type> | <Tags> "

  @OutputDirectory
  val outputDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun run() {
    val diagnosticsMetadata = BSLLSSourceReader.getDiagnosticsMetadata(project).toSortedMap()
    updateDiagnosticIndex("ru", diagnosticsMetadata)
    updateDiagnosticIndex("en", diagnosticsMetadata)
  }

  private fun updateDiagnosticIndex(lang: String, diagnosticsMetadata: Map<String, HashMap<String, Any>>) {
    var indexText = ""
    val typeCount = hashMapOf<String, Int>()
    diagnosticsMetadata.forEach {
      val metadata = it.value
      val typeString: String
      val typeKey: String = metadata.getOrDefault("type", "") as String
      val tags = metadata.getOrDefault("tags", "").toString().toLowerCase()
        .replace("[", "`")
        .replace("]", "`")
        .replace(", ", "`<br>`")
      if (lang == "ru") {
        typeString = typeRuMap.getOrDefault(typeKey, "")
        indexText += templateIndexLine
          .replace("<Name>", it.key)
          .replace("<Description>", metadata["description_ru"].toString())
          .replace(
            "<Activated>",
            if (metadata.getOrDefault("activatedByDefault", "").toString()
                .toLowerCase() != "false"
            ) "Да" else "Нет"
          )
          .replace(
            "<Severity>", severityRuMap
              .getOrDefault(metadata.getOrDefault("severity", "") as String, "")
          )
          .replace("<Type>", typeString)
          .replace("<Tags>", tags)
      } else {
        typeString = typeEnMap.getOrDefault(typeKey, "")
        indexText += templateIndexLine
          .replace("<Name>", it.key)
          .replace("<Description>", metadata["description_en"].toString())
          .replace(
            "<Activated>",
            if (metadata.getOrDefault("activatedByDefault", "").toString()
                .toLowerCase() != "false"
            ) "Yes" else "No"
          )
          .replace(
            "<Severity>", severityEnMap
              .getOrDefault(metadata.getOrDefault("severity", "") as String, "")
          )
          .replace("<Type>", typeString)
          .replace("<Tags>", tags)
      }

      typeCount[typeKey] = typeCount.getOrDefault(typeKey, 0) + 1
    }

    val indexPath = if (lang == "ru") {
      File(outputDir.get().asFile.path, "docs/diagnostics/index.md")
    } else {
      File(outputDir.get().asFile.path, "docs/en/diagnostics/index.md")
    }

    val text = indexPath.readText(charset("UTF-8"))

    var header = "## Список реализованных диагностик"
    var total = "Общее количество:"
    var table = "| Ключ | Название | Включена по умолчанию | Важность | Тип | Тэги |"
    if (lang == "en") {
      header = "## Implemented diagnostics"
      total = "Total:"
      table = "| Key | Name| Enabled by default | Severity | Type | Tags |"
    }
    table += "\n| --- | --- | :-: | --- | --- | --- |"

    total += " **${diagnosticsMetadata.size}**\n\n* ${
      typeCount.map {
        if (lang == "ru") {
          typeRuMap.getOrDefault(it.key, "")
        } else {
          typeEnMap.getOrDefault(it.key, "")
        } + ": **${it.value}**"
      }.joinToString("\n* ")
    }\n"
    val indexHeader = text.indexOf(header)
    indexPath.writeText(
      text.substring(0, indexHeader - 1) + "\n${header}\n\n${total}\n\n${table}${indexText}",
      charset("UTF-8")
    )
  }
}
