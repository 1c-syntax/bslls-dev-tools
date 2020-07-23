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
import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.typeParamRuMap
import com.github._1c_syntax.bsllsdevtools.BSLLSSourceReader.Companion.typeRuMap
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UpdateDiagnosticDocsTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Updates diagnostic docs after changes"
    dependsOn(":jar")
    outputs.upToDateWhen { false }
  }

  private val templateDocHeader = "# <Description> (<DiagnosticKey>)\n\n<Metadata>\n<Params>" +
    "<!-- Блоки выше заполняются автоматически, не трогать -->\n"
  private val templateDocFooter =
    "## <Helpers>\n\n" +
      "<!-- Блоки ниже заполняются автоматически, не трогать -->\n" +
      "### <DiagnosticIgnorance>\n\n```bsl\n// BSLLS:<DiagnosticKey>-off\n// BSLLS:<DiagnosticKey>-on\n```\n\n" +
      "### <ParameterConfig>\n\n```json\n\"<DiagnosticKey>\": <DiagnosticConfig>\n```\n"
  private val templateDocMetadata =
    "| <TypeHeader> | <ScopeHeader> | <SeverityHeader> | <ActivatedHeader> | <MinutesHeader> | <TagsHeader> |\n" +
      "| :-: | :-: | :-: | :-: | :-: | :-: |\n" +
      "| `<Type>` | `<Scope>` | `<Severity>` | `<Activated>` | `<Minutes>` | <Tags> |\n"
  private val templateDocHeaderParams =
    "| <NameHeader> | <TypeHeader> | <DescriptionHeader> | <DefHeader> |\n" +
      "| :-: | :-: | :-- | :-: |\n"
  private val templateDocLineParams = "| `<Name>` | `<Type>` | ```<Description>``` | ```<Def>``` |\n"

  @OutputDirectory
  val outputDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun run() {
    val diagnosticsMetadata = BSLLSSourceReader.getDiagnosticsMetadata(project)
    diagnosticsMetadata.forEach {
      updateDocFile("ru", it.key, it.value)
      updateDocFile("en", it.key, it.value)
    }
  }

  private fun updateDocFile(lang: String, key: String, metadata: HashMap<String, Any>) {
    val docPath = if (lang == "ru") {
      File(outputDir.get().asFile.path, "docs/diagnostics/${key}.md")
    } else {
      File(outputDir.get().asFile.path, "docs/en/diagnostics/${key}.md")
    }

    val text = docPath.readText(charset("UTF-8"))

    var header = "## Описание диагностики"
    var footer = "## Сниппеты"
    val headerText = templateDocHeader
      .replace("<Description>", metadata["description_$lang"].toString())
      .replace("<Metadata>", makeDiagnosticMetadata(lang, metadata))
      .replace("<Params>", makeDiagnosticParams(lang, metadata))
      .replace("<DiagnosticKey>", key)

    var footerText = templateDocFooter
      .replace("<DiagnosticKey>", key)
      .replace("<DiagnosticConfig>", makeDiagnosticConfigExample(metadata))

    if (lang == "ru") {
      footerText = footerText
        .replace("<Helpers>", "Сниппеты")
        .replace("<DiagnosticIgnorance>", "Экранирование кода")
        .replace("<ParameterConfig>", "Параметр конфигурационного файла")
    } else {
      footerText = footerText
        .replace("<Helpers>", "Snippets")
        .replace("<DiagnosticIgnorance>", "Diagnostic ignorance in code")
        .replace("<ParameterConfig>", "Parameter for config")
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
      "${newText.trimEnd()}\n\n$footerText"
    } else {
      "${newText.substring(0, index - 1).trimEnd()}\n\n$footerText"
    }

    docPath.writeText(newText, charset("UTF-8"))
  }

  private fun makeDiagnosticMetadata(lang: String, metadata: HashMap<String, Any>): String {
    var metadataBody = templateDocMetadata
      .replace("<Minutes>", metadata.getOrDefault("minutesToFix", "").toString())
      .replace(
        "<Tags>", metadata.getOrDefault("tags", "").toString().toLowerCase()
          .replace("[", "`")
          .replace("]", "`")
          .replace(", ", "`<br/>`")
      )
      .replace(
        "<Scope>",
        if (metadata.getOrDefault("scope", "")
            .toString() == "ALL"
        ) "BSL`<br/>`OS" else metadata.getOrDefault("scope", "").toString()
      )

    if (lang == "ru") {
      metadataBody = metadataBody
        .replace("<TypeHeader>", "Тип")
        .replace("<ScopeHeader>", "Поддерживаются<br/>языки")
        .replace("<SeverityHeader>", "Важность")
        .replace("<ActivatedHeader>", "Включена<br/>по умолчанию")
        .replace("<MinutesHeader>", "Время на<br/>исправление (мин)")
        .replace("<TagsHeader>", "Тэги")
        .replace(
          "<Type>", typeRuMap
            .getOrDefault(metadata.getOrDefault("type", ""), "")
        )
        .replace(
          "<Severity>",
          severityRuMap
            .getOrDefault(metadata.getOrDefault("severity", ""), "")
        )
        .replace(
          "<Activated>",
          if (metadata.getOrDefault("activatedByDefault", "").toString()
              .toLowerCase() != "false"
          ) "Да" else "Нет"
        )
    } else {
      metadataBody = metadataBody
        .replace("<TypeHeader>", "Type")
        .replace("<ScopeHeader>", "Scope")
        .replace("<SeverityHeader>", "Severity")
        .replace("<ActivatedHeader>", "Activated<br/>by default")
        .replace("<MinutesHeader>", "Minutes<br/>to fix")
        .replace("<TagsHeader>", "Tags")
        .replace(
          "<Type>", typeEnMap
            .getOrDefault(metadata.getOrDefault("type", ""), "")
        )
        .replace(
          "<Severity>",
          severityEnMap
            .getOrDefault(metadata.getOrDefault("severity", ""), "")
        )
        .replace(
          "<Activated>",
          if (metadata.getOrDefault("activatedByDefault", "").toString()
              .toLowerCase() != "false"
          ) "Yes" else "No"
        )
    }

    return metadataBody
  }

  private fun makeDiagnosticParams(lang: String, metadata: HashMap<String, Any>): String {
    val params = metadata.getOrDefault("parameters", arrayListOf<HashMap<String, String>>()) as ArrayList<*>
    if (params.isEmpty()) {
      return ""
    }

    var paramsBody = templateDocHeaderParams

    if (lang == "ru") {
      paramsBody = "## Параметры \n\n" + paramsBody
        .replace("<NameHeader>", "�?мя")
        .replace("<TypeHeader>", "Тип")
        .replace("<DescriptionHeader>", "Описание")
        .replace("<DefHeader>", "Значение по умолчанию")

      params.forEach {
        if (it is HashMap<*, *>) {
          var typeValue = it.getOrDefault("type", "").toString()
          typeValue = typeParamRuMap.getOrDefault(typeValue, typeValue)
          val paramName = it.getOrDefault("name", "").toString()
          paramsBody += templateDocLineParams
            .replace("<Name>", paramName)
            .replace("<Type>", typeValue)
            .replace(
              "<Description>", it
                .getOrDefault("description_ru", "").toString()
            )
            .replace(
              "<Def>", it
                .getOrDefault("defaultValue", "").toString()
            )
        }
      }

    } else {
      paramsBody = "## Parameters \n\n" + paramsBody
        .replace("<NameHeader>", "Name")
        .replace("<TypeHeader>", "Type")
        .replace("<DescriptionHeader>", "Description")
        .replace("<DefHeader>", "Default value")

      params.forEach {
        if (it is HashMap<*, *>) {
          val paramName = it.getOrDefault("name", "").toString()
          paramsBody += templateDocLineParams
            .replace("<Name>", paramName)
            .replace("<Type>", it.getOrDefault("type", "").toString())
            .replace(
              "<Description>", it
                .getOrDefault("description_en", "").toString()
            )
            .replace("<Def>", it.getOrDefault("defaultValue", "").toString())
        }
      }
    }
    return paramsBody + "\n"
  }

  private fun makeDiagnosticConfigExample(metadata: HashMap<String, Any>): String {
    val params = metadata.getOrDefault("parameters", arrayListOf<HashMap<String, String>>())
    if (params is ArrayList<*>) {

      if (params.isEmpty()) {
        return "false"
      }

      var configBody = ""
      var configDelimiter = ""
      params.forEach {
        if (it is HashMap<*, *>) {
          val qoutes = if (it.getOrDefault("type", "") == "Boolean"
            || it.getOrDefault("type", "") == "Integer"
            || it.getOrDefault("type", "") == "Float"
          ) "" else "\""
          configBody += "$configDelimiter    \"${it.getOrDefault("name", "")}\": " +
            "${qoutes}${it.getOrDefault("defaultValue", "")
              .toString().replace("\\", "\\\\")}${qoutes}"
          configDelimiter = ",\n"
        }
      }
      configBody = "{\n${configBody}\n}"
      return configBody
    }

    return ""
  }

}
