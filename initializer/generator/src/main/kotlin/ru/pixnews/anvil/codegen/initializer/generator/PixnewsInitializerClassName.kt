/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.initializer.generator

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
        "ru.pixnews.foundation.initializers.inject",
        "AppInitializersScope",
    )
    internal val contributesInitializerFqName = FqName(
        "ru.pixnews.foundation.initializers.inject.ContributesInitializer",
    )
}
