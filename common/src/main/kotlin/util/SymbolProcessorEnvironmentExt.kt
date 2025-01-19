/*
 * Copyright (c) 2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.common.util

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.ClassName
import ru.pixnews.anvil.ksp.codegen.common.InternalPixnewsAnvilCodegenApi

@InternalPixnewsAnvilCodegenApi
public fun SymbolProcessorEnvironment.readClassNameOrDefault(key: String, default: ClassName) =
    options[key]?.let(ClassName::bestGuess) ?: default
