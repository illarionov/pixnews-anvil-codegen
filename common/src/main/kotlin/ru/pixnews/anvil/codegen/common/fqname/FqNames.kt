/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.common.fqname

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.codegen.common.classname.PixnewsClassName

public object FqNames {
    @JvmField
    public val contributesActivity: FqName =
        FqName("ru.pixnews.foundation.di.ui.base.activity.ContributesActivity")

    @JvmField
    public val contributesCoroutineWorker: FqName =
        FqName("ru.pixnews.foundation.di.workmanager.ContributesCoroutineWorker")

    @JvmField
    public val contributesExperiment: FqName =
        FqName("ru.pixnews.foundation.featuretoggles.inject.ContributesExperiment")

    @JvmField
    public val contributesInitializer: FqName =
        FqName("ru.pixnews.foundation.initializers.inject.ContributesInitializer")

    @JvmField
    public val contributesTest: FqName = FqName("ru.pixnews.foundation.instrumented.test.di.ContributesTest")

    @JvmField
    public val contributesVariantSerializer: FqName = FqName(
        "ru.pixnews.foundation.featuretoggles.inject.ContributesExperimentVariantSerializer",
    )

    @JvmField
    public val contributesViewModel: FqName =
        FqName("ru.pixnews.foundation.di.ui.base.viewmodel.ContributesViewModel")

    @JvmField
    public val experiment: FqName = PixnewsClassName.experiment.asFqName()

    @JvmField
    public val experimentVariantSerializer: FqName = PixnewsClassName.experimentVariantSerializer.asFqName()

    private fun ClassName.asFqName(): FqName = FqName(this.canonicalName)
}
