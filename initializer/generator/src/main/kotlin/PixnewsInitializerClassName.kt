/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.initializer.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsInitializerClassName {
    internal val initializer: ClassName = ClassName(
        "ru.pixnews.foundation.initializers",
        "Initializer",
    )
    internal val asyncInitializer: ClassName = ClassName(
        "ru.pixnews.foundation.initializers",
        "AsyncInitializer",
    )
    internal val appInitializersScope: ClassName = ClassName(
        "ru.pixnews.anvil.ksp.codegen.initializer.inject",
        "AppInitializersScope",
    )
    internal val contributesInitializerFqName = FqName(
        "ru.pixnews.anvil.ksp.codegen.initializer.inject.ContributesInitializer",
    )
}
