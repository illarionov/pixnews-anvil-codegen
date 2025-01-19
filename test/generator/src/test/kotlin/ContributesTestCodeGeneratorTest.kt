/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.test.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import com.squareup.anvil.compiler.internal.testing.ComponentProcessingMode.KSP
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.squareup.kotlinpoet.ClassName
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.kspArgs
import dagger.MembersInjector
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.ksp.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.ksp.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesTestCodeGeneratorTest {
    private val generatedModuleName = "com.test.MainTest_TestModule"
    private val appScopeAnnotation = ClassName("com.example.scope", "AppScope")
    private val contributesTestAnnotation = ClassName("com.example.inject", "ContributesTest")
    private val instrumentedTestInjectorClass = ClassName("com.example.inject.wiring", "SingleInstrumentedTestInjector")
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    fun setup() {
        val contributesTest = """
            package ${contributesTestAnnotation.packageName}
            annotation class ${contributesTestAnnotation.simpleName}
        """.trimIndent()
        val singleInstrumentedTestInjector = """
            package ${instrumentedTestInjectorClass.packageName}
            import dagger.MembersInjector

            @Suppress("UNUSED_PARAMETER")
            class ${instrumentedTestInjectorClass.simpleName}(injector: MembersInjector<*>)
        """.trimIndent()

        val appScopeStub = """
            package ${appScopeAnnotation.packageName}
            abstract class ${appScopeAnnotation.simpleName} private constructor()
        """.trimIndent()

        val testClass = """
            package com.test
            import ${contributesTestAnnotation.canonicalName}

            @${contributesTestAnnotation.simpleName}
            class MainTest
        """.trimIndent()
        compilationResult = compileAnvil(
            componentProcessingMode = KSP,
            sources = arrayOf(
                contributesTest,
                singleInstrumentedTestInjector,
                appScopeStub,
                testClass,
            ),
            expectExitCode = OK,
            onCompilation = {
                kotlinCompilation.kspArgs += mapOf(
                    APP_SCOPE_KSP_KEY to appScopeAnnotation.canonicalName,
                    CONTRIBUTES_TEST_ANNOTATION_KSP_KEY to contributesTestAnnotation.canonicalName,
                    INSTRUMENTED_TEST_INJECTOR_KSP_KEY to instrumentedTestInjectorClass.canonicalName,
                )
            },
        )
    }

    @Test
    fun `Generated module should have correct annotations`() {
        val clazz = compilationResult.classLoader.loadClass(generatedModuleName)
        val appScopeClass = compilationResult.classLoader.loadClass(appScopeAnnotation)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)

        assertThat(
            clazz.getAnnotation(ContributesTo::class.java).scope.java,
        ).isEqualTo(appScopeClass)
    }

    @Test
    fun `Generated module should have correct provide method`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val singleInstrumentedTestInjectorClass = compilationResult.classLoader.loadClass(instrumentedTestInjectorClass)

        val provideMethod = moduleClass.declaredMethods.firstOrNull {
            it.name == "provideMainTestInjector"
        } ?: fail("no provideMainTestInjector method")
        assertThat(provideMethod.returnType).isEqualTo(singleInstrumentedTestInjectorClass)
        assertThat(provideMethod.parameterTypes).containsExactly(MembersInjector::class.java)
        assertThat(provideMethod.annotations.map(Annotation::annotationClass)).containsExactlyInAnyOrder(
            Provides::class,
            IntoMap::class,
            SingleIn::class,
            ClassKey::class,
        )
    }
}
