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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UtilsTest {

  @Test
  fun wrapSpaces() {
    val testString1 = "Test1"
    val testMaxCount1 = 10

    val testString2 = "Test<br>2"
    val testMaxCount2 = 12

    assertThat(Utils.wrapSpaces(testString1, testMaxCount1)).isEqualTo("  Test1   ")
    assertThat(Utils.wrapSpaces(testString2, testMaxCount2)).isEqualTo(" Test<br>2  ")

  }

  @Test
  fun computeMaxLens() {
    val keys = hashMapOf(
      "Key1" to "Val1",
      "Key2" to "Val_22",
      "Key3" to "",
      "Key4" to "Val4<br>Val4<br>"
    )

    val keysMaxLen = Utils.createKeysLenMap(keys)
    Utils.computeMaxLens(keysMaxLen, keys)

    assertThat(keysMaxLen["Key1"]).isEqualTo(4)
    assertThat(keysMaxLen["Key2"]).isEqualTo(6)
    assertThat(keysMaxLen["Key3"]).isEqualTo(0)
    assertThat(keysMaxLen["Key4"]).isEqualTo(28)
  }

  @Test
  fun createKeysLenMap() {
    val keys = hashMapOf(
      "Key1" to "Val1",
      "Key2" to "Val_22",
      "Key3" to "",
      "Key4" to "Val4<br>Val4<br>"
    )

    val keysMaxLen = Utils.createKeysLenMap(keys)
    assertThat(keysMaxLen).hasSize(4)
    keysMaxLen.forEach {
      assertThat(it.value).isEqualTo(0)
    }
  }
}