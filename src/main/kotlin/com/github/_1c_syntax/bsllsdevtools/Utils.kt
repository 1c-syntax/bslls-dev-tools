/*
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

object Utils {
  @JvmStatic
  fun wrapSpaces(string: String, len: Int): String {
    val extraLen = len - string.length
    val left = extraLen / 2
    val right = extraLen - left

    return " ".repeat(left) + string + " ".repeat(right)
  }

  @JvmStatic
  fun computeMaxLens(keysMaxLenMap: HashMap<String, Int>, values: HashMap<String, String>) {
    values.forEach {
      val len = keysMaxLenMap[it.key]
      val brCount = (it.value.length - it.value.replace("<br>", "").length) / 4
      keysMaxLenMap[it.key] = maxOf(len!!, it.value.length + (brCount * 6))
    }
  }

  @JvmStatic
  fun computeMaxLens(maxKeysLenMap: HashMap<String, Int>, values: ArrayList<HashMap<String, String>>) {
    values.forEach {
      computeMaxLens(maxKeysLenMap, it)
    }
  }

  @JvmStatic
  fun createKeysLenMap(keys: HashMap<String, String>): HashMap<String, Int> {
    val keysLenMap = HashMap<String, Int>()
    keys.forEach {
      keysLenMap[it.key] = 0
    }

    return keysLenMap
  }
}
