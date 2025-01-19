/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.activity.generator

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
import dagger.Binds
import dagger.MembersInjector
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
class ContributesActivityCodeGeneratorTest {
    private val generatedModuleName = "com.test.TestActivity_ActivityModule"
    private val contributesActivityAnnotation = ClassName("com.example.inject", "TestContributesActivity")
    private val activityMapKeyAnnotation = ClassName("com.example.inject.wiring", "TestActivityKey")
    private val activityScope = ClassName("com.example.inject", "TestActivityScope")
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    fun setup() {
        val wiringActivityDiStubs = """
            package ${activityMapKeyAnnotation.packageName}
            import android.app.Activity
            import dagger.MapKey
            import kotlin.reflect.KClass

            @MapKey annotation class ${activityMapKeyAnnotation.simpleName}(val activityClass: KClass<out Activity>)
        """.trimIndent()

        val activityScopeAnnotationStub = """
            package ${activityScope.packageName}
            abstract class ${activityScope.simpleName} private constructor()
        """.trimIndent()

        val contributeActivityAnnotationStub = """
            package ${contributesActivityAnnotation.packageName}
            annotation class ${contributesActivityAnnotation.simpleName}
        """.trimIndent()

        val androidActivityStub = """
            package android.app
            open class Activity
        """.trimIndent()

        val testActivity = """
            package com.test

            import android.app.Activity
            import ${contributesActivityAnnotation.canonicalName}

            @${contributesActivityAnnotation.simpleName}
            class TestActivity : Activity()
        """.trimIndent()

        compilationResult = compileAnvil(
            componentProcessingMode = KSP,
            sources = arrayOf(
                wiringActivityDiStubs,
                activityScopeAnnotationStub,
                contributeActivityAnnotationStub,
                androidActivityStub,
                testActivity,
            ),
            expectExitCode = OK,
            onCompilation = {
                kotlinCompilation.kspArgs += mapOf(
                    CONTRIBUTES_ACTIVITY_ANNOTATION_KSP_KEY to contributesActivityAnnotation.canonicalName,
                    ACTIVITY_MAP_KEY_KSP_KEY to activityMapKeyAnnotation.canonicalName,
                    ACTIVITY_SCOPE_KSP_KEY to activityScope.canonicalName,
                )
            },
        )
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
        val activityMapKey = compilationResult.classLoader.loadClass(activityMapKeyAnnotation)

        val provideMethod = moduleClass.declaredMethods.firstOrNull {
            it.name == "bindsTestActivityInjector"
        } ?: fail("no bindsTestActivityInjector method")

        assertThat(provideMethod.returnType).isEqualTo(MembersInjector::class.java)
        assertThat(provideMethod.parameterTypes).containsExactly(MembersInjector::class.java)
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
