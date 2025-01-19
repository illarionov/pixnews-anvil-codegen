/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.viewmodel.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.internal.testing.ComponentProcessingMode.KSP
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.squareup.kotlinpoet.ClassName
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.kspArgs
import dagger.Provides
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
class ContributesViewModelCodeGeneratorTest {
    private val generatedModuleName = "com.test.TestViewModel_FactoryModule"
    private val featureManagerClass = ClassName("ru.pixnews.foundation.featuretoggles", "FeatureManager")
    private val contributesViewModelAnnotation = ClassName("com.example", "ContributesViewModelAnnotation")
    private val viewModelScopeAnnotation = ClassName("com.example", "ViewModelScope")
    private val viewModelFactoryAnnotation = ClassName("com.example.wiring", "ViewModelFactory")
    private val viewModelMapKeyAnnotation = ClassName("com.example.wiring", "ViewModelMapKey")
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION", "LongMethod")
    fun setup() {
        val androidxLifecycleStubs = """
            package androidx.lifecycle

            abstract class ViewModel
            class SavedStateHandle
            abstract class CreationExtras

            fun CreationExtras.createSavedStateHandle(): SavedStateHandle = SavedStateHandle()
        """.trimIndent()

        val featureTogglesStubs = """
            package ${featureManagerClass.packageName}
            interface ${featureManagerClass.simpleName}
        """.trimIndent()

        val contributesViewModelStub = """
            package ${contributesViewModelAnnotation.packageName}
            annotation class ${contributesViewModelAnnotation.simpleName}
            abstract class ${viewModelScopeAnnotation.simpleName} private constructor()
        """.trimIndent()
        val baseDiViewModelStubs = """
            package ${viewModelMapKeyAnnotation.packageName}

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.CreationExtras
            import dagger.MapKey
            import kotlin.reflect.KClass

            @MapKey annotation class ${viewModelMapKeyAnnotation.simpleName}(val viewModelClass: KClass<out ViewModel>)

            fun interface ${viewModelFactoryAnnotation.simpleName} {
                fun create(creationExtras: CreationExtras): ViewModel
            }
        """.trimIndent()

        val testViewModel = """
            package com.test

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.SavedStateHandle
            import ru.pixnews.foundation.featuretoggles.FeatureManager
            import ${contributesViewModelAnnotation.canonicalName}

            @Suppress("UNUSED_PARAMETER")
            @${contributesViewModelAnnotation.simpleName}
            class TestViewModel(
                featureManager: FeatureManager,
                savedStateHandle: SavedStateHandle,
            ) : ViewModel()
        """.trimIndent()

        compilationResult = compileAnvil(
            componentProcessingMode = KSP,
            sources = arrayOf(
                contributesViewModelStub,
                baseDiViewModelStubs,
                androidxLifecycleStubs,
                featureTogglesStubs,
                testViewModel,
            ),
            expectExitCode = OK,
            onCompilation = {
                kotlinCompilation.kspArgs += mapOf(
                    KspKey.CONTRIBUTES_VIEW_MODEL_ANNOTATION to contributesViewModelAnnotation.canonicalName,
                    KspKey.VIEW_MODEL_MAP_KEY to viewModelMapKeyAnnotation.canonicalName,
                    KspKey.VIEW_MODEL_SCOPE to viewModelScopeAnnotation.canonicalName,
                    KspKey.VIEW_MODEL_FACTORY to viewModelFactoryAnnotation.canonicalName,
                )
            },
        )
    }

    @Test
    fun `Generated module should have correct annotations`() {
        val clazz = compilationResult.classLoader.loadClass(generatedModuleName)
        val viewModelScopeClass = compilationResult.classLoader.loadClass(viewModelScopeAnnotation)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)
        assertThat(clazz.getAnnotation(ContributesTo::class.java).scope.java).isEqualTo(viewModelScopeClass)
    }

    @Test
    fun `Generated module should have correct provide method`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val viewModelMapKey = compilationResult.classLoader.loadClass(viewModelMapKeyAnnotation)
        val viewModelFactoryClass = compilationResult.classLoader.loadClass(viewModelFactoryAnnotation)
        val featureManagerClass = compilationResult.classLoader.loadClass(featureManagerClass)

        val provideMethod = moduleClass.declaredMethods.firstOrNull {
            it.name == "providesTestViewModelViewModelFactory"
        } ?: fail("no providesTestViewModelViewModelFactory method")

        assertThat(provideMethod.returnType).isEqualTo(viewModelFactoryClass)
        assertThat(provideMethod.parameterTypes).containsExactly(featureManagerClass)
        assertThat(provideMethod.annotations.map(Annotation::annotationClass)).containsExactlyInAnyOrder(
            Provides::class,
            IntoMap::class,
            viewModelMapKey.kotlin,
        )
    }
}
