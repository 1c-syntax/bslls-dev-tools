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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class UpdateDiagnosticsIndexTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

  init {
    group = "Developer tools"
    description = "Update diagnostics index after changes"
    dependsOn(":classes")
    outputs.upToDateWhen { false }
  }

  @OutputDirectory
  val outputDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun run() {
    updateDiagnosticIndex("ru")
    updateDiagnosticIndex("en")
  }

  private fun updateDiagnosticIndex(lang: String) {
    val indexPath = Utils.diagnosticIndexPath(outputDir, lang)
    val text = indexPath.readText(charset("UTF-8"))

    var header = "## Список реализованных диагностик"
    if (lang == "en") {
      header = "## Implemented diagnostics"
    }

    val indexHeader = text.indexOf(header)
    indexPath.writeText(
      "${text.substring(0, indexHeader - 1).trimEnd()}\n${header}\n",
      charset("UTF-8")
    )
  }
}
