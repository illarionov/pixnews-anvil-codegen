/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.activity.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import dagger.Binds
import dagger.MembersInjector
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.codegen.activity.generator.PixnewsActivityClassName.activityMapKey
import ru.pixnews.anvil.codegen.activity.generator.PixnewsActivityClassName.activityScope
import ru.pixnews.anvil.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesActivityCodeGeneratorTest {
    private val generatedModuleName = "com.test.TestActivity_ActivityModule"
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    fun setup() {
        val activityDiStubs = """
            package ru.pixnews.foundation.di.ui.base.activity
            import android.app.Activity
            import dagger.MapKey
            import kotlin.reflect.KClass

            public abstract class ActivityScope private constructor()
            public annotation class ActivityMapKey(val activityClass: KClass<out Activity>)
            public annotation class ContributesActivity
        """.trimIndent()

        val androidActivityStub = """
            package android.app
            open class Activity
        """.trimIndent()

        val testActivity = """
            package com.test

            import android.app.Activity
            import ru.pixnews.foundation.di.ui.base.activity.ContributesActivity

            @ContributesActivity
            class TestActivity : Activity()
        """.trimIndent()

        compilationResult = compileAnvil(
            sources = arrayOf(
                activityDiStubs,
                androidActivityStub,
                testActivity,
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
        val activityScopeClass = compilationResult.classLoader.loadClass(activityScope)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)

        assertThat(clazz.getAnnotation(ContributesTo::class.java).scope.java)
            .isEqualTo(activityScopeClass)
    }

    @Test
    fun `Generated module should have correct binding method`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val activityMapKey = compilationResult.classLoader.loadClass(activityMapKey)

        val provideMethod = moduleClass.declaredMethods.firstOrNull {
            it.name == "bindsTestActivityInjector"
        } ?: fail("no bindsTestActivityInjector method")

        assertThat(provideMethod.returnType).isEqualTo(MembersInjector::class.java)
        assertThat(provideMethod.parameterTypes)
            .containsExactly(MembersInjector::class.java)
        assertThat(
            provideMethod.annotations.map(Annotation::annotationClass),
        ).containsExactlyInAnyOrder(
            Binds::class,
            IntoMap::class,
            SingleIn::class,
            activityMapKey.kotlin,
        )
    }
}
