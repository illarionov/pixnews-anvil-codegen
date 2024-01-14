/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.test.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsTestClassName {
    internal val appScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.base.scope",
        "AppScope",
    )
    internal val contributesTestFqName = FqName(
        "ru.pixnews.foundation.instrumented.test.di.ContributesTest",
    )
    internal val singleInstrumentedTestInjector: ClassName = ClassName(
        "ru.pixnews.foundation.instrumented.test.di",
        "SingleInstrumentedTestInjector",
    )
}