/**
 * This file is a part of BSLLS Development tools gradle pugin.
 *
 * Copyright © 2020-2020
 * Valery Maximov <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSLLS Development tools gradle pugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSLLS Development tools gradle pugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSLLS Development tools gradle pugin.
 */
package com.github._1c_syntax.bsllsdevtools

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UpdateJsonSchemaTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Update json schema"
    dependsOn(":jar")
    outputs.upToDateWhen { false }
  }

  private val schemaPath = "src/main/resources/com/github/_1c_syntax/bsl/languageserver/configuration/schema.json"
  private val diagnosticSchemaPath =
    "src/main/resources/com/github/_1c_syntax/bsl/languageserver/configuration/parameters-schema.json"

  @OutputDirectory
  val outputDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun run() {
    val diagnosticsMetadata = BSLLSSourceReader.getDiagnosticsMetadata(project)
    val diagnosticMeta = transformMetadata(diagnosticsMetadata)
    var schemaJson = groovy.json.JsonSlurper()
      .parseText(File(outputDir.get().asFile.path, diagnosticSchemaPath).readText(charset("UTF-8")))
    if (schemaJson is Map<*, *>) {
      val schema = schemaJson.toMap().toMutableMap()
      schema["definitions"] = diagnosticMeta["diagnostics"]
      val resultString = groovy.json.JsonBuilder(schema).toPrettyString()
      File(outputDir.get().asFile.path, diagnosticSchemaPath).writeText(resultString, charset("UTF-8"))
    }

    schemaJson = groovy.json.JsonSlurper()
      .parseText(File(outputDir.get().asFile.path, schemaPath).readText(charset("UTF-8")))
    if (schemaJson is Map<*, *>) {
      val schema = schemaJson.toMap().toMutableMap()
      val schemaDefinitions = schema["definitions"]
      if (schemaDefinitions != null && schemaDefinitions is Map<*, *>) {
        val schemaDefinitionInner = schemaDefinitions.toMap().toMutableMap()
        val schemaParameters = schemaDefinitionInner["parameters"]
        if (schemaParameters != null && schemaParameters is Map<*, *>) {
          val schemaParametersInner = schemaParameters.toMap().toMutableMap()
          schemaParametersInner["properties"] = diagnosticMeta["diagnosticsKeys"]
          schemaDefinitionInner["parameters"] = schemaParametersInner
        }
        schema["definitions"] = schemaDefinitionInner
      }

      val resultString = groovy.json.JsonBuilder(schema).toPrettyString()
      File(outputDir.get().asFile.path, schemaPath).writeText(resultString, charset("UTF-8"))
    }
  }

  private fun transformMetadata(diagnosticsMetadata: HashMap<String, HashMap<String, Any>>): HashMap<String, Any> {
    val result = hashMapOf<String, Any>()
    val diagnostics = sortedMapOf<String, Any>()
    val diagnosticsKeys = sortedMapOf<String, HashMap<String, String>>()

    diagnosticsMetadata.forEach { itd ->
      diagnosticsKeys[itd.key] = hashMapOf("\$ref" to "parameters-schema.json#/definitions/${itd.key}")
      val diagnostic = hashMapOf(
        "\$id" to "#/definitions/${itd.key}",
        "type" to arrayListOf("boolean", "object"),
        "title" to itd.value.getOrDefault("description_en", "").toString(),
        "description" to itd.value.getOrDefault("description_en", "").toString(),
        "default" to itd.value.getOrDefault("activatedByDefault", "false").toString().toBoolean()
      )
      val params = HashMap<String, Any>()

      val parameters =
        itd.value.getOrDefault("parameters", arrayListOf<HashMap<String, String>>()) as ArrayList<*>
      if (parameters.isNotEmpty()) {
        parameters.forEach {
          if (it is HashMap<*, *>) {
            val typeString = it.getOrDefault("type", "").toString().toLowerCase()
              .replace("pattern", "string")
              .replace("float", "number")
            val value = when (typeString) {
              "boolean" -> {
                it.getOrDefault("defaultValue", "false").toString().toBoolean()
              }
              "integer" -> {
                it.getOrDefault("defaultValue", "0").toString().toInt()
              }
              "number" -> {
                it.getOrDefault("defaultValue", "0").toString().toFloat()
              }
              else -> {
                "${it.getOrDefault("defaultValue", "")}"
              }
            }
            val oneParam = hashMapOf(
              "type" to typeString,
              "title" to it.getOrDefault("description_en", "").toString(),
              "description" to it.getOrDefault("description_en", "").toString(),
              "default" to value
            )

            params[it.getOrDefault("name", "").toString()] = oneParam
          }
        }
      }

      if (params.isNotEmpty()) {
        diagnostic["properties"] = params
      }
      diagnostics[itd.key] = diagnostic
    }

    result["diagnostics"] = diagnostics
    result["diagnosticsKeys"] = diagnosticsKeys
    return result
  }
}