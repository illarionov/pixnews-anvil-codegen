/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.testutils

import com.squareup.kotlinpoet.ClassName

public fun ClassLoader.loadClass(clazz: ClassName): Class<*> = this.loadClass(clazz.canonicalName)

@Suppress("UNCHECKED_CAST")
public fun <T> Annotation.getElementValue(elementName: String): T =
    this::class.java.declaredMethods.single { it.name == elementName }.invoke(this) as T
