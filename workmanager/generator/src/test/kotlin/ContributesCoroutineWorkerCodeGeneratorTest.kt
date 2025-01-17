/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.workmanager.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.anvil.compiler.internal.testing.ComponentProcessingMode.KSP
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.squareup.kotlinpoet.ClassName
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.kspArgs
import dagger.assisted.AssistedFactory
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.ksp.codegen.testutils.getElementValue
import ru.pixnews.anvil.ksp.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.ksp.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesCoroutineWorkerCodeGeneratorTest {
    private val generatedFactoryName = "com.test.TestWorker_AssistedFactory"
    private val contributeCoroutineWorkerAnnotation = ClassName("com.example", "ContributeCoroutineWorker")
    private val workManagerScopeClass = ClassName("com.example", "WorkManagerScope")
    private val applicationContextAnnotation = ClassName("com.example.di.base.qualifiers", "ApplicationContext")
    private val coroutineWorkerFactoryClass = ClassName("com.example.di", "CoroutineWorkerFactory")
    private val coroutineWorkerFactoryMapKeyAnnotation = ClassName("com.example.di", "CoroutineWorkerMapKey")
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION", "LongMethod")
    fun setup() {
        val androidContextStub = """
            package android.content
            open class Context
        """.trimIndent()

        val workManagerStubs = """
            package androidx.work
            import android.content.Context

            open class WorkerParameters

            @Suppress("UNUSED_PARAMETER")
            abstract class CoroutineWorker(
                appContext: Context,
                params: WorkerParameters
            ) {
              public abstract suspend fun doWork(): Result<Unit>
            }
        """.trimIndent()

        val daggerStubs = """
            package dagger.assisted
            public annotation class Assisted(val value: String = "")
            public annotation class AssistedInject
        """.trimIndent()

        val appContextQualifier = """
            package ${applicationContextAnnotation.packageName}
            public annotation class ${applicationContextAnnotation.simpleName}
        """.trimIndent()

        val workManagerInjectStubs = """
            package ${contributeCoroutineWorkerAnnotation.packageName}
            public abstract class ${workManagerScopeClass.simpleName} private constructor()
            public annotation class ${contributeCoroutineWorkerAnnotation.simpleName}
        """.trimIndent()
        val workmanagerWiringStubs = """
            package ${coroutineWorkerFactoryClass.packageName}
            import android.content.Context
            import androidx.work.CoroutineWorker
            import androidx.work.WorkerParameters
            import ${applicationContextAnnotation.canonicalName}
            import kotlin.reflect.KClass

            public annotation class ${coroutineWorkerFactoryMapKeyAnnotation.simpleName}(val workerClass: KClass<out CoroutineWorker>)

            public interface ${coroutineWorkerFactoryClass.simpleName} {
                public fun create(
                    @${applicationContextAnnotation.simpleName} context: Context,
                    workerParameters: WorkerParameters,
                ): CoroutineWorker
            }
        """.trimIndent()

        val testWorker = """
            package com.test

            import android.content.Context
            import androidx.work.CoroutineWorker
            import androidx.work.WorkerParameters
            import dagger.assisted.Assisted
            import dagger.assisted.AssistedInject
            import ${applicationContextAnnotation.canonicalName}
            import ${contributeCoroutineWorkerAnnotation.canonicalName}

            @Suppress("UNUSED_PARAMETER")
            @${contributeCoroutineWorkerAnnotation.simpleName}
            public class TestWorker @AssistedInject constructor(
                @Assisted @${applicationContextAnnotation.simpleName} appContext: Context,
                @Assisted params: WorkerParameters,
            ) : CoroutineWorker(appContext, params) {
                override suspend fun doWork(): Result<Unit> {
                    return Result.success(Unit)
                }
            }
        """.trimIndent()

        compilationResult = compileAnvil(
            componentProcessingMode = KSP,
            sources = arrayOf(
                androidContextStub,
                workManagerInjectStubs,
                workManagerStubs,
                daggerStubs,
                appContextQualifier,
                workmanagerWiringStubs,
                testWorker,
            ),
            expectExitCode = OK,
            onCompilation = {
                kotlinCompilation.kspArgs += mapOf(
                    KspKey.CONTRIBUTE_COROUTINE_WORKER to contributeCoroutineWorkerAnnotation.canonicalName,
                    KspKey.APPLICATION_CONTEXT_QUALIFIER to applicationContextAnnotation.canonicalName,
                    KspKey.COROUTINE_WORKER_FACTORY to coroutineWorkerFactoryClass.canonicalName,
                    KspKey.COROUTINE_WORKER_MAP_KEY to coroutineWorkerFactoryMapKeyAnnotation.canonicalName,
                    KspKey.WORK_MANAGER_SCOPE to workManagerScopeClass.canonicalName,
                )
            },
        )
    }

    @Test
    fun `Generated factory should have correct annotations and superclass`() {
        val clazz = compilationResult.classLoader.loadClass(generatedFactoryName)
        val testWorkerClass = compilationResult.classLoader.loadClass("com.test.TestWorker")
        val workManagerScopeClass = compilationResult.classLoader.loadClass(workManagerScopeClass)
        val coroutineWorkerFactoryClass =
            compilationResult.classLoader.loadClass(coroutineWorkerFactoryClass)

        @Suppress("UNCHECKED_CAST")
        val coroutineWorkerMapKeyClass: Class<Annotation> = compilationResult.classLoader.loadClass(
            coroutineWorkerFactoryMapKeyAnnotation,
        ) as Class<Annotation>

        assertThat(clazz).haveAnnotation(AssistedFactory::class.java)
        assertThat(clazz).haveAnnotation(ContributesMultibinding::class.java)
        assertThat(
            clazz.getAnnotation(ContributesMultibinding::class.java).scope.java,
        ).isEqualTo(workManagerScopeClass)

        assertThat(
            clazz.getAnnotation(coroutineWorkerMapKeyClass).getElementValue<Class<*>>("workerClass"),
        ).isEqualTo(testWorkerClass)

        assertTrue { coroutineWorkerFactoryClass.isAssignableFrom(clazz) }
    }

    @Test
    fun `Generated factory should have correct create method`() {
        val factoryClass = compilationResult.classLoader.loadClass(generatedFactoryName)
        val testWorkerClass = compilationResult.classLoader.loadClass("com.test.TestWorker")
        val androidContextClass = compilationResult.classLoader.loadClass(ANDROID_CONTEXT_CLASS_NAME)
        val workerParamsClass = compilationResult.classLoader.loadClass(WORKER_PARAMETERS_CLASS_NAME)

        val createMethod = factoryClass
            .declaredMethods.firstOrNull { it.name == "create" } ?: fail("no create() method")

        assertThat(createMethod.parameterTypes).containsExactly(androidContextClass, workerParamsClass)
        assertThat(createMethod.returnType).isEqualTo(testWorkerClass)
    }
}
