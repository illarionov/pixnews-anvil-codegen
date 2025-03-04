/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.experiment.generator

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.internal.testing.ComponentProcessingMode.KSP
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experiment
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantMapKey
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantSerializer
import ru.pixnews.anvil.ksp.codegen.testutils.getElementValue
import ru.pixnews.anvil.ksp.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.ksp.codegen.testutils.loadClass

@TestInstance(Lifecycle.PER_CLASS)
class ContributesExperimentCodeGeneratorTest {
    private val generatedModuleName = "com.test.TestExperiment_Experiments_Module"
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    fun setup() {
        compilationResult = compileAnvil(
            """
            package ru.pixnews.foundation.featuretoggles
            interface Experiment
            interface ExperimentVariantSerializer
            """.trimIndent(),
            """
            package ru.pixnews.anvil.ksp.codegen.experiment.inject
            public abstract class ExperimentScope private constructor()
            public annotation class ContributesExperiment
            public annotation class ContributesExperimentVariantSerializer(val experimentKey: String)
            """.trimIndent(),
            """
            package ru.pixnews.anvil.ksp.codegen.experiment.inject.wiring
            import dagger.MapKey

            @MapKey annotation class ExperimentVariantMapKey(val key: String)
            """.trimIndent(),
            """
            package com.test
            import ru.pixnews.foundation.featuretoggles.Experiment
            import ru.pixnews.foundation.featuretoggles.ExperimentVariantSerializer
            import ru.pixnews.anvil.ksp.codegen.experiment.inject.ContributesExperiment
            import ru.pixnews.anvil.ksp.codegen.experiment.inject.ContributesExperimentVariantSerializer

            @ContributesExperiment
            public object TestExperiment : Experiment {
                @ContributesExperimentVariantSerializer(experimentKey = "test.serializer")
                public object TestExperimentSerializer : ExperimentVariantSerializer

                @ContributesExperimentVariantSerializer("test.serializer.no.key")
                public object TestNoKeyExperimentSerializer : ExperimentVariantSerializer
            }
        """.trimIndent(),
            componentProcessingMode = KSP,
            expectExitCode = OK,
        )
    }

    @Test
    fun `Generated module should have correct annotations`() {
        val clazz = compilationResult.classLoader.loadClass(generatedModuleName)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)
    }

    @Test
    fun `Generated module should have correct providing method for experiment`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val experimentClass = compilationResult.classLoader.loadClass(experiment)

        val provideMethod = moduleClass.declaredMethods.firstOrNull {
            it.name == "provideTestExperiment"
        } ?: fail("no provideTestExperiment method")

        assertThat(provideMethod.returnType).isEqualTo(experimentClass)
        assertThat(provideMethod.parameterTypes).isEmpty()
        assertThat(
            provideMethod.annotations.map(Annotation::annotationClass),
        ).containsExactlyInAnyOrder(
            Provides::class,
            IntoSet::class,
        )
    }

    @Test
    fun `Generated module should have providing method for experiment variant serializer`() {
        testExperimentVariantProvideMethod(
            methodName = "provideTestExperimentSerializer",
            experimentKey = "test.serializer",
        )
    }

    @Test
    fun `Generated module should have providing method for experiment variant serializer with no key parameter`() {
        testExperimentVariantProvideMethod(
            methodName = "provideTestNoKeyExperimentSerializer",
            experimentKey = "test.serializer.no.key",
        )
    }

    private fun testExperimentVariantProvideMethod(
        methodName: String,
        experimentKey: String,
    ) {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val experimentVariantSerializerClass = compilationResult.classLoader.loadClass(experimentVariantSerializer)

        @Suppress("UNCHECKED_CAST") val experimentVariantMapKey = compilationResult.classLoader
            .loadClass(experimentVariantMapKey) as Class<Annotation>

        val provideMethod = moduleClass.declaredMethods.firstOrNull { it.name == methodName }
            ?: fail("no $methodName method")

        assertThat(provideMethod.returnType).isEqualTo(experimentVariantSerializerClass)
        assertThat(provideMethod.parameterTypes).isEmpty()
        assertThat(
            provideMethod.annotations.map(Annotation::annotationClass),
        ).containsExactlyInAnyOrder(
            Provides::class,
            IntoMap::class,
            experimentVariantMapKey.kotlin,
        )
        assertThat(
            provideMethod.getAnnotation(experimentVariantMapKey).getElementValue<String>("key"),
        ).isEqualTo(experimentKey)
    }
}
