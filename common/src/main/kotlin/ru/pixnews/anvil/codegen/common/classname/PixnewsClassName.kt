/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.common.classname

import com.squareup.kotlinpoet.ClassName

public object PixnewsClassName {
    @JvmStatic
    public val applicationContext: ClassName = ClassName(
        "ru.pixnews.foundation.di.base.qualifiers",
        "ApplicationContext",
    )

    @JvmStatic
    public val singleIn: ClassName = ClassName(
        "com.squareup.anvil.annotations.optional",
        "SingleIn",
    )

    @JvmStatic
    public val activityMapKey: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.activity",
        "ActivityMapKey",
    )

    @JvmStatic
    public val coroutineWorkerMapKey: ClassName = ClassName(
        "ru.pixnews.foundation.di.workmanager",
        "CoroutineWorkerMapKey",
    )

    @JvmStatic
    public val activityScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.activity",
        "ActivityScope",
    )

    @JvmStatic
    public val appInitializersScope: ClassName = ClassName(
        "ru.pixnews.foundation.initializers.inject",
        "AppInitializersScope",
    )

    @JvmStatic
    public val appScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.base.scope",
        "AppScope",
    )

    @JvmStatic
    public val coroutineWorkerFactory: ClassName = ClassName(
        "ru.pixnews.foundation.di.workmanager",
        "CoroutineWorkerFactory",
    )

    @JvmStatic
    public val workManagerScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.workmanager",
        "WorkManagerScope",
    )

    @JvmStatic
    public val experiment: ClassName = ClassName(
        "ru.pixnews.foundation.featuretoggles",
        "Experiment",
    )

    @JvmStatic
    public val experimentScope: ClassName = ClassName(
        "ru.pixnews.foundation.featuretoggles.inject",
        "ExperimentScope",
    )

    @JvmStatic
    public val experimentVariantMapKey: ClassName = ClassName(
        "ru.pixnews.foundation.featuretoggles.inject",
        "ExperimentVariantMapKey",
    )

    @JvmStatic
    public val experimentVariantSerializer: ClassName = ClassName(
        "ru.pixnews.foundation.featuretoggles",
        "ExperimentVariantSerializer",
    )

    @JvmStatic
    public val singleInstrumentedTestInjector: ClassName = ClassName(
        "ru.pixnews.foundation.instrumented.test.di",
        "SingleInstrumentedTestInjector",
    )

    @JvmStatic
    public val viewModelFactory: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelFactory",
    )

    @JvmStatic
    public val viewModelMapKey: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelMapKey",
    )

    @JvmStatic
    public val viewModelScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelScope",
    )

    @JvmStatic
    public val asyncInitializer: ClassName = ClassName(
        "ru.pixnews.foundation.initializers",
        "AsyncInitializer",
    )

    @JvmStatic
    public val initializer: ClassName = ClassName(
        "ru.pixnews.foundation.initializers",
        "Initializer",
    )
}
