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

import org.gradle.api.Plugin
import org.gradle.api.Project

class BSLDeveloperToolsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.tasks.register("updateDiagnosticDocs", UpdateDiagnosticDocsTask::class.java) {
      it.outputDir.set(project.layout.projectDirectory)
    }
    project.tasks.register("updateDiagnosticsIndex", UpdateDiagnosticsIndexTask::class.java) {
      it.outputDir.set(project.layout.projectDirectory)
    }
    project.tasks.register("updateJsonSchema", UpdateJsonSchemaTask::class.java) {
      it.outputDir.set(project.layout.projectDirectory)
    }
    project.tasks.register("newDiagnostic", NewDiagnosticTask::class.java) {
      it.outputDir.set(project.layout.projectDirectory)
    }

    project.tasks.register("precommit") {
      it.description = "Run all precommit tasks"
      it.group = "Developer tools"
      it.dependsOn(":check")
      it.dependsOn(":updateDiagnosticDocs")
      it.dependsOn(":updateDiagnosticsIndex")
      it.dependsOn(":updateJsonSchema")
    }
  }
}
