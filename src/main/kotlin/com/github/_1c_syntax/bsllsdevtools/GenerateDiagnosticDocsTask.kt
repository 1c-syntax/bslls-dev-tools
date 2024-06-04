/*
 * This file is a part of BSLLS Development tools gradle plugin.
 *
 * Copyright (c) 2020-2024
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
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

open class GenerateDiagnosticDocsTask constructor() : DefaultTask() {

  init {
    group = "build"
    description = "Generate diagnostic docs"
    dependsOn(":classes")
    outputs.upToDateWhen { false }
  }

  @Option(option = "build", description = "Generate diagnostic docs in docs folder")
  private var build = false

  private val templateDocHeader = "# <Description> (<DiagnosticKey>)\n\n<Metadata>\n<Params>" +
    "<!-- Блоки выше заполняются автоматически, не трогать -->\n"
  private val templateDocFooter =
    "## <Helpers>\n\n" +
      "<!-- Блоки ниже заполняются автоматически, не трогать -->\n" +
      "### <DiagnosticIgnorance>\n\n```bsl\n// BSLLS:<DiagnosticKey>-off\n// BSLLS:<DiagnosticKey>-on\n```\n\n" +
      "### <ParameterConfig>\n\n```json\n\"<DiagnosticKey>\": <DiagnosticConfig>\n```\n"

  fun setBuild(build: Boolean) {
    this.build = build
  }

  @TaskAction
  fun run() {
    var outputDir = project.projectDir;

    if (!build) {
      Utils.createDocFolder(outputDir, "build/docs/diagnostics", true)
      Utils.createDocFolder(outputDir, "build/docs/en/diagnostics", true)
    }

    val diagnosticsMetadata = BSLLSSourceReader.getDiagnosticsMetadata(project)

    diagnosticsMetadata.forEach {
      generateDocFile(outputDir, "ru", it.key, it.value)
      generateDocFile(outputDir, "en", it.key, it.value)
    }
  }

  private fun generateDocFile(outputDir: File, lang: String, key: String, metadata: HashMap<String, Any>) {
    val sourcePath = Utils.diagnosticDocPath(outputDir, lang, key)

    var destinationPath = sourcePath
    if (!build) {
      destinationPath = if (lang == "ru") {
        File(outputDir, "build/docs/diagnostics/${key}.md")
      } else {
        File(outputDir, "build/docs/en/diagnostics/${key}.md")
      }
    }

    val text = sourcePath.readText(charset("UTF-8"))

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

    destinationPath.writeText(newText, charset("UTF-8"))
  }

  private fun makeDiagnosticMetadata(lang: String, metadata: HashMap<String, Any>): String {
    val keys = hashMapOf(
      "Type" to "",
      "Scope" to "",
      "Severity" to "",
      "Activated" to "",
      "Minutes" to "",
      "Tags" to ""
    )

    val keysMaxLen = Utils.createKeysLenMap(keys)

    val headerValues = HashMap(keys)
    val tableValues = HashMap(keys)

    if (lang == "ru") {
      headerValues["Type"] = "Тип"
      headerValues["Scope"] = "Поддерживаются<br>языки"
      headerValues["Severity"] = "Важность"
      headerValues["Activated"] = "Включена<br>по умолчанию"
      headerValues["Minutes"] = "Время на<br>исправление (мин)"
      headerValues["Tags"] = "Теги"

      tableValues["Type"] = "`${typeRuMap.getOrDefault(metadata.getOrDefault("type", ""), "")}`"
      tableValues["Severity"] = "`${severityRuMap.getOrDefault(metadata.getOrDefault("severity", ""), "")}`"
      tableValues["Activated"] = "`${
        if (metadata.getOrDefault("activatedByDefault", "").toString()
            .lowercase() != "false"
        ) "Да" else "Нет"
      }`"
    } else {
      headerValues["Type"] = "Type"
      headerValues["Scope"] = "Scope"
      headerValues["Severity"] = "Severity"
      headerValues["Activated"] = "Activated<br>by default"
      headerValues["Minutes"] = "Minutes<br>to fix"
      headerValues["Tags"] = "Tags"

      tableValues["Type"] = "`${typeEnMap.getOrDefault(metadata.getOrDefault("type", ""), "")}`"
      tableValues["Severity"] = "`${severityEnMap.getOrDefault(metadata.getOrDefault("severity", ""), "")}`"
      tableValues["Activated"] = "`${
        if (metadata.getOrDefault("activatedByDefault", "").toString()
            .lowercase() != "false"
        ) "Yes" else "No"
      }`"
    }

    tableValues["Scope"] = "`${
      if (metadata.getOrDefault("scope", "")
          .toString() == "ALL"
      ) "BSL`<br>`OS" else metadata.getOrDefault("scope", "").toString()
    }`"
    tableValues["Minutes"] = "`${metadata.getOrDefault("minutesToFix", "")}`"
    tableValues["Tags"] = metadata.getOrDefault("tags", "").toString().lowercase()
      .replace("[", "`")
      .replace("]", "`")
      .replace(", ", "`<br>`")

    // запоминаем максимальные длины строк + конецевые пробелы
    Utils.computeMaxLens(keysMaxLen, headerValues)
    Utils.computeMaxLens(keysMaxLen, tableValues)

    // получаем строки с учетом длины
    val header = computeMetaLineString(keysMaxLen, headerValues)
    val line = computeMetaLineString(keysMaxLen, tableValues)
    val order = computeMetaOrderString(keysMaxLen)

    return "$header\n$order\n$line\n"
  }

  private fun makeDiagnosticParams(lang: String, metadata: HashMap<String, Any>): String {
    val params = metadata.getOrDefault("parameters", arrayListOf<HashMap<String, String>>()) as ArrayList<*>
    if (params.isEmpty()) {
      return ""
    }

    val paramsBody: String

    val keys = hashMapOf(
      "Name" to "",
      "Type" to "",
      "Description" to "",
      "Def" to ""
    )

    val keysMaxLen = Utils.createKeysLenMap(keys)
    val headerValues = HashMap(keys)
    val tableValues = ArrayList<HashMap<String, String>>()

    if (lang == "ru") {
      paramsBody = "## Параметры\n\n"

      headerValues["Name"] = "Имя"
      headerValues["Type"] = "Тип"
      headerValues["Description"] = "Описание"
      headerValues["Def"] = "Значение<br>по умолчанию"
    } else {
      paramsBody = "## Parameters\n\n"

      headerValues["Name"] = "Name"
      headerValues["Type"] = "Type"
      headerValues["Description"] = "Description"
      headerValues["Def"] = "Default value"
    }

    params.forEach {
      if (it is HashMap<*, *>) {

        val tableValue = HashMap(keys)
        tableValue["Name"] = "`${it.getOrDefault("name", "")}`"

        var typeValue = it.getOrDefault("type", "").toString()
        if (lang == "ru") {
          typeValue = typeParamRuMap.getOrDefault(typeValue, typeValue)
          tableValue["Description"] = "`${it.getOrDefault("description_ru", "")}`"
        } else {
          tableValue["Description"] = "`${it.getOrDefault("description_en", "")}`"
        }

        tableValue["Type"] = "`${typeValue}`"
        tableValue["Def"] = "`${it.getOrDefault("defaultValue", "")}`"

        tableValues.add(tableValue)
      }
    }

    // запоминаем максимальные длины строк + концевые пробелы
    Utils.computeMaxLens(keysMaxLen, headerValues)
    Utils.computeMaxLens(keysMaxLen, tableValues)

    // получаем строки с учетом длины
    val header = computeParamsLineString(keysMaxLen, headerValues)
    var lines = ""
    tableValues.forEach {
      lines += computeParamsLineString(keysMaxLen, it) + "\n"
    }

    val order = computeParamsOrderString(keysMaxLen)

    return "$paramsBody\n$header\n$order\n$lines"
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
            "${qoutes}${
              it.getOrDefault("defaultValue", "")
                .toString().replace("\\", "\\\\")
            }${qoutes}"
          configDelimiter = ",\n"
        }
      }
      configBody = "{\n${configBody}\n}"
      return configBody
    }

    return ""
  }

  private fun computeMetaLineString(maxKeysLen: HashMap<String, Int>, values: HashMap<String, String>): String {
    return "| ${values["Type"]?.let { maxKeysLen["Type"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Scope"]?.let { maxKeysLen["Scope"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Severity"]?.let { maxKeysLen["Severity"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Activated"]?.let { maxKeysLen["Activated"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Minutes"]?.let { maxKeysLen["Minutes"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Tags"]?.let { maxKeysLen["Tags"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "|"
  }

  private fun computeMetaOrderString(maxKeysLen: HashMap<String, Int>): String {
    val dash = "-"
    return "|:${dash.repeat(maxKeysLen["Type"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Scope"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Severity"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Activated"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Minutes"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Tags"]!!)}:" +
      "|"
  }

  private fun computeParamsLineString(maxKeysLen: HashMap<String, Int>, values: HashMap<String, String>): String {
    return "| ${values["Name"]?.let { maxKeysLen["Name"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Type"]?.let { maxKeysLen["Type"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Description"]?.let { maxKeysLen["Description"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "| ${values["Def"]?.let { maxKeysLen["Def"]?.let { len -> Utils.wrapSpaces(it, len) } }} " +
      "|"
  }

  private fun computeParamsOrderString(maxKeysLen: HashMap<String, Int>): String {
    val dash = "-"
    return "|:${dash.repeat(maxKeysLen["Name"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Type"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Description"]!!)}:" +
      "|:${dash.repeat(maxKeysLen["Def"]!!)}:" +
      "|"
  }

}
