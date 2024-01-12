/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.workmanager.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import dagger.assisted.AssistedFactory
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.codegen.common.classname.AndroidClassName
import ru.pixnews.anvil.codegen.common.classname.PixnewsClassName
import ru.pixnews.anvil.codegen.testutils.getElementValue
import ru.pixnews.anvil.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesCoroutineWorkerCodeGeneratorTest {
    private val generatedFactoryName = "com.test.TestWorker_AssistedFactory"
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
            public annotation class Assisted
            public annotation class AssistedInject
        """.trimIndent()

        val appContextQualifier = """
            package ru.pixnews.foundation.di.base.qualifiers
            public annotation class ApplicationContext
        """.trimIndent()

        val workmanagerDiStubs = """
            package ru.pixnews.foundation.di.workmanager
            import android.content.Context
            import androidx.work.CoroutineWorker
            import androidx.work.WorkerParameters
            import ru.pixnews.foundation.di.base.qualifiers.ApplicationContext
            import kotlin.reflect.KClass

            public abstract class WorkManagerScope private constructor()
            public annotation class ContributesCoroutineWorker
            public annotation class CoroutineWorkerMapKey(val workerClass: KClass<out CoroutineWorker>)

            public interface CoroutineWorkerFactory {
                public fun create(
                    @ApplicationContext context: Context,
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
            import ru.pixnews.foundation.di.base.qualifiers.ApplicationContext
            import ru.pixnews.foundation.di.workmanager.ContributesCoroutineWorker

            @Suppress("UNUSED_PARAMETER")
            @ContributesCoroutineWorker
            public class TestWorker @AssistedInject constructor(
                @Assisted @ApplicationContext appContext: Context,
                @Assisted params: WorkerParameters,
            ) : CoroutineWorker(appContext, params) {
                override suspend fun doWork(): Result<Unit> {
                    return Result.success(Unit)
                }
            }
        """.trimIndent()

        compilationResult = compileAnvil(
            sources = arrayOf(
                androidContextStub,
                workManagerStubs,
                daggerStubs,
                appContextQualifier,
                workmanagerDiStubs,
                testWorker,
            ),
        )
    }

    @Test
    fun `Dagger factory should be generated`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
    }

    @Test
    fun `Generated factory should have correct annotations and superclass`() {
        val clazz = compilationResult.classLoader.loadClass(generatedFactoryName)
        val testWorkerClass = compilationResult.classLoader.loadClass("com.test.TestWorker")
        val workManagerScopeClass = compilationResult.classLoader.loadClass(PixnewsClassName.workManagerScope)
        val coroutineWorkerFactoryClass =
            compilationResult.classLoader.loadClass(PixnewsClassName.coroutineWorkerFactory)

        @Suppress("UNCHECKED_CAST")
        val coroutineWorkerMapKeyClass: Class<Annotation> = compilationResult.classLoader.loadClass(
            PixnewsClassName.coroutineWorkerMapKey,
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
        val androidContextClass = compilationResult.classLoader.loadClass(AndroidClassName.context)
        val workerParamsClass = compilationResult.classLoader.loadClass(AndroidClassName.workerParameters)

        val createMethod = factoryClass.declaredMethods.firstOrNull {
            it.name == "create"
        } ?: fail("no create() method")

        assertThat(createMethod.parameterTypes).containsExactly(androidContextClass, workerParamsClass)
        assertThat(createMethod.returnType).isEqualTo(testWorkerClass)
    }
}