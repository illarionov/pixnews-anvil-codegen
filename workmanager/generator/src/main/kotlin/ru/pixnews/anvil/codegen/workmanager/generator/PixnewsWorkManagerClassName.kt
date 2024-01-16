/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.workmanager.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsWorkManagerClassName {
    internal val contributesCoroutineWorkerFqName = FqName(
        "ru.pixnews.anvil.codegen.workmanager.inject.ContributesCoroutineWorker",
    )
    internal val applicationContext = ClassName(
        "ru.pixnews.foundation.di.base.qualifiers",
        "ApplicationContext",
    )
    internal val coroutineWorkerFactory: ClassName = ClassName(
        "ru.pixnews.anvil.codegen.workmanager.inject.wiring",
        "CoroutineWorkerFactory",
    )
    internal val coroutineWorkerMapKey: ClassName = ClassName(
        "ru.pixnews.anvil.codegen.workmanager.inject.wiring",
        "CoroutineWorkerMapKey",
    )
    internal val workManagerScope: ClassName = ClassName(
        "ru.pixnews.anvil.codegen.workmanager.inject",
        "WorkManagerScope",
    )
}
