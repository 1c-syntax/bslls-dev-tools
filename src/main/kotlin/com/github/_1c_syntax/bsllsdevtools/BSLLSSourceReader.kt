/**
 * This file is a part of BSLLS Development tools gradle plugin.
 *
 * Copyright © 2020-2021
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

import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader

class BSLLSSourceReader {
  companion object {

    private const val lsPackageName = "com.github._1c_syntax.bsl.languageserver"
    private const val diagnosticPackageName = "$lsPackageName.diagnostics"
    private const val lsConfigurationClassName = "$lsPackageName.configuration.LanguageServerConfiguration"
    private const val languageClassName = "$lsPackageName.configuration.Language"
    private const val diagnosticInfoClassName = "$diagnosticPackageName.metadata.DiagnosticInfo"
    private const val diagnosticCodeClassName = "$diagnosticPackageName.metadata.DiagnosticCode"
    private const val diagnosticParameterInfoClassName = "$diagnosticPackageName.metadata.DiagnosticParameterInfo"
    private const val diagnosticsClassesFolder =
      "classes/java/main/com/github/_1c_syntax/bsl/languageserver/diagnostics"

    val typeRuMap = hashMapOf(
      "ERROR" to "Ошибка",
      "VULNERABILITY" to "Уязвимость",
      "SECURITY_HOTSPOT" to "Потенциальная уязвимость",
      "CODE_SMELL" to "Дефект кода"
    )

    val typeEnMap = hashMapOf(
      "ERROR" to "Error",
      "VULNERABILITY" to "Vulnerability",
      "SECURITY_HOTSPOT" to "Security Hotspot",
      "CODE_SMELL" to "Code smell"
    )

    val severityRuMap = hashMapOf(
      "BLOCKER" to "Блокирующий",
      "CRITICAL" to "Критичный",
      "MAJOR" to "Важный",
      "MINOR" to "Незначительный",
      "INFO" to "Информационный"
    )

    val severityEnMap = hashMapOf(
      "BLOCKER" to "Blocker",
      "CRITICAL" to "Critical",
      "MAJOR" to "Major",
      "MINOR" to "Minor",
      "INFO" to "Info"
    )

    val typeParamRuMap = hashMapOf(
      "Integer" to "Целое",
      "Boolean" to "Булево",
      "String" to "Строка",
      "Float" to "Число с плавающей точкой"
    )

    fun getDiagnosticTags(project: Project): Map<String, String> {
      return getDiagnosticTags(createClassLoader(project))
    }

    fun getDiagnosticsMetadata(project: Project): HashMap<String, HashMap<String, Any>> {

      val classLoader = createClassLoader(project)
      val lsConfigurationRu = createLSConfiguration(classLoader, "ru")
      val lsConfigurationEn = createLSConfiguration(classLoader, "en")

      val result = hashMapOf<String, HashMap<String, Any>>()
      File(project.buildDir, diagnosticsClassesFolder)
        .walkTopDown()
        .filter {
          it.name.endsWith("Diagnostic.class")
            && !it.name.startsWith("Abstract")
        }
        .forEach {
          val diagnosticClass =
            classLoader.loadClass("${diagnosticPackageName}.${it.nameWithoutExtension}")
          if (!diagnosticClass.toString().startsWith("interface")) {
            val diagnosticInfoRu = createDiagnosticInfo(classLoader, diagnosticClass, lsConfigurationRu)
            val diagnosticInfoEn = createDiagnosticInfo(classLoader, diagnosticClass, lsConfigurationEn)

            val metadata = getDiagnosticInfo(classLoader, diagnosticInfoRu, diagnosticInfoEn)
            if (metadata.isNotEmpty() && metadata["key"] is String) {
              result[metadata["key"] as String] = metadata
            }
          }
        }

      return result
    }

    private fun getDiagnosticInfo(classLoader: ClassLoader, infoRu: Any, infoEn: Any): HashMap<String, Any> {
      val infoClass = classLoader.loadClass(diagnosticInfoClassName)
      val codeClass = classLoader.loadClass(diagnosticCodeClassName)
      val parameterInfoClass = classLoader.loadClass(diagnosticParameterInfoClassName)

      val diagnosticCode = infoClass.getMethod("getCode").invoke(infoRu)
      val code = codeClass.getMethod("getStringValue").invoke(diagnosticCode)

      val metadata = hashMapOf<String, Any>()
      metadata["key"] = code
      metadata["type"] = infoClass.getMethod("getType").invoke(infoRu).toString()
      metadata["severity"] = infoClass.getMethod("getSeverity").invoke(infoRu).toString()
      metadata["scope"] = infoClass.getMethod("getScope").invoke(infoRu).toString()
      metadata["minutesToFix"] = infoClass.getMethod("getMinutesToFix").invoke(infoRu).toString()
      metadata["activatedByDefault"] = infoClass.getMethod("isActivatedByDefault").invoke(infoRu)
      metadata["tags"] = infoClass.getMethod("getTags").invoke(infoRu).toString()
      metadata["description_ru"] = infoClass.getMethod("getName").invoke(infoRu).toString()
      metadata["description_en"] = infoClass.getMethod("getName").invoke(infoEn).toString()

      val params = arrayListOf<HashMap<String, String>>()
      val parameters = infoClass.getMethod("getParameters").invoke(infoRu)
      if (parameters is ArrayList<*>) {
        for (parameter in parameters) {
          val oneParameter = hashMapOf<String, String>()
          val parameterName = parameterInfoClass.getMethod("getName").invoke(parameter).toString()
          oneParameter["name"] = parameterName
          val typeArr =
            parameterInfoClass.getMethod("getType").invoke(parameter).toString().split(".")
          oneParameter["type"] = typeArr[typeArr.size - 1]
          oneParameter["defaultValue"] =
            parameterInfoClass.getMethod("getDefaultValue").invoke(parameter).toString()
          oneParameter["description_ru"] =
            parameterInfoClass.getMethod("getDescription").invoke(parameter).toString()
          oneParameter["description_en"] =
            infoClass.getMethod("getResourceString", classLoader.loadClass("java.lang.String"))
              .invoke(infoEn, parameterName).toString()

          params.add(oneParameter)
        }
      }

      metadata["parameters"] = params
      return metadata
    }

    private fun createDiagnosticInfo(
      classLoader: ClassLoader,
      diagnosticClass: Any,
      lsConfiguration: Any
    ): Any {
      return classLoader.loadClass(diagnosticInfoClassName)
        .declaredConstructors[0].newInstance(diagnosticClass, lsConfiguration)
    }

    private fun createLSConfiguration(classLoader: ClassLoader, lang: String): Any {
      val languageServerConfigurationClass = classLoader.loadClass(lsConfigurationClassName)
      val lsConfiguration = languageServerConfigurationClass.getConstructor()
        .newInstance()

      val languageClass = classLoader.loadClass(languageClassName)
      val language = languageClass.getMethod("valueOf", classLoader.loadClass("java.lang.String"))
        .invoke(languageClass, lang.toUpperCase())

      languageServerConfigurationClass.getDeclaredMethod("setLanguage", languageClass)
        .invoke(lsConfiguration, language)

      return lsConfiguration
    }

    private fun createClassLoader(project: Project): ClassLoader {
      val urls: ArrayList<URL> = ArrayList()
      File(project.buildDir, "classes")
        .walkTopDown()
        .forEach { urls.add(it.toURI().toURL()) }

      File(project.buildDir, "resources")
        .walkTopDown()
        .forEach { urls.add(it.toURI().toURL()) }

      val urlsParent: ArrayList<URL> = ArrayList()
      project.configurations.getByName("runtimeClasspath").files.forEach {
        urlsParent.add(it.toURI().toURL())
      }
      val parentCL = URLClassLoader(urlsParent.toTypedArray())
      return URLClassLoader(urls.toTypedArray(), parentCL)
    }

    private fun getDiagnosticTags(classLoader: ClassLoader): Map<String, String> {
      val tagsClass = classLoader.loadClass("$diagnosticPackageName.metadata.DiagnosticTag")
      val tagsValues = tagsClass.getMethod("values").invoke(tagsClass)
      val tags = hashMapOf<String, String>()
      if (tagsValues is Array<*>) {
        for (tag in tagsValues) {
          tags[tagsClass.getMethod("name").invoke(tag).toString()] =
            tagsClass.getMethod("getDescription").invoke(tag).toString()
        }
      }
      return tags.toSortedMap()
    }
  }
}