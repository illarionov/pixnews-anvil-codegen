/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.test.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
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
import ru.pixnews.anvil.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesTestCodeGeneratorTest {
    private val generatedModuleName = "com.test.MainTest_TestModule"
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    fun setup() {
        val testStubs = """
            package ru.pixnews.foundation.instrumented.test.di
            import dagger.MembersInjector

            annotation class ContributesTest

            @Suppress("UNUSED_PARAMETER")
            class SingleInstrumentedTestInjector(injector: MembersInjector<*>)
        """.trimIndent()

        val appScopeStub = """
            package ru.pixnews.foundation.di.base.scope
            public abstract class AppScope private constructor()
        """.trimIndent()

        val testClass = """
            package com.test
            import ru.pixnews.foundation.instrumented.test.di.ContributesTest

            @ContributesTest
            class MainTest
        """.trimIndent()
        compilationResult = compileAnvil(
            sources = arrayOf(
                testStubs,
                appScopeStub,
                testClass,
            ),
        )
    }

    @Test
    fun `Dagger module should be generated`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
    }

    @Test
    fun `Generated module should have correct annotations`() {
        val clazz = compilationResult.classLoader.loadClass(generatedModuleName)
        val appScopeClass = compilationResult.classLoader.loadClass(PixnewsTestClassName.appScope)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)

        assertThat(
            clazz.getAnnotation(ContributesTo::class.java).scope.java,
        ).isEqualTo(appScopeClass)
    }

    @Test
    fun `Generated module should have correct provide method`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val singleInstrumentedTestInjectorClass = compilationResult.classLoader.loadClass(
            PixnewsTestClassName.singleInstrumentedTestInjector,
        )

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
