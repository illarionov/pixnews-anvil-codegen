/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.experiment.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsExperimentClassName {
    private const val FEATURETOGGLES_PACKAGE = "ru.pixnews.foundation.featuretoggles"
    private const val FEATURETOGGLES_INJECT_PACKAGE = "ru.pixnews.foundation.featuretoggles.inject"
    internal val experiment = ClassName(FEATURETOGGLES_PACKAGE, "Experiment")
    internal val experimentFqName = FqName(experiment.canonicalName)
    internal val experimentVariantSerializer = ClassName(FEATURETOGGLES_PACKAGE, "ExperimentVariantSerializer")
    internal val experimentVariantSerializerFqName = FqName(experimentVariantSerializer.canonicalName)
    internal val contributesExperimentFqName = FqName("$FEATURETOGGLES_INJECT_PACKAGE.ContributesExperiment")
    internal val contributesExperimentVariantSerializerFqName = FqName(
        "$FEATURETOGGLES_INJECT_PACKAGE.ContributesExperimentVariantSerializer",
    )
    internal val experimentScope = ClassName(FEATURETOGGLES_INJECT_PACKAGE, "ExperimentScope")
    internal val experimentVariantMapKey = ClassName(FEATURETOGGLES_INJECT_PACKAGE, "ExperimentVariantMapKey")
}
