/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.experiment.inject

import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Annotate a Experiment variant class with this to automatically contribute it to the ExperimentScope multibinding.
 * Equivalent to the following declaration in an application module:
 *```
 *  @Module
 *  @ContributesTo(ExperimentScope::class)
 *  abstract class ExperimentVariantSerializerModule {
 *    @Binds @IntoMap
 *    @ExperimentVariantMapKey("main")
 *    abstract fun bindSerializer(serializer: MainExperimentVariantSerializer): ExperimentVariantSerializer
 *  }
 *```
 * The generated code created via the anvil-codegen module.
 */
@Target(CLASS)
public annotation class ContributesExperimentVariantSerializer(
    val experimentKey: String,
)
