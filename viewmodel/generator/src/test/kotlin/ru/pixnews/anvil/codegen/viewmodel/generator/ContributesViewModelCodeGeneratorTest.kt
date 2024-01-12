/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.viewmodel.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.squareup.kotlinpoet.ClassName
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import dagger.Provides
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.fail
import ru.pixnews.anvil.codegen.common.classname.PixnewsClassName
import ru.pixnews.anvil.codegen.testutils.haveAnnotation
import ru.pixnews.anvil.codegen.testutils.loadClass

@OptIn(ExperimentalCompilerApi::class)
@TestInstance(Lifecycle.PER_CLASS)
class ContributesViewModelCodeGeneratorTest {
    private val generatedModuleName = "com.test.TestViewModel_FactoryModule"
    private val featureManagerClass = ClassName("ru.pixnews.foundation.featuretoggles", "FeatureManager")
    private lateinit var compilationResult: JvmCompilationResult

    @BeforeAll
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
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

        val baseDiViewModelStubs = """
            package ru.pixnews.foundation.di.ui.base.viewmodel

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.CreationExtras
            import kotlin.reflect.KClass

            public annotation class ContributesViewModel
            public annotation class ViewModelMapKey(val viewModelClass: KClass<out ViewModel>)
            public abstract class ViewModelScope private constructor()

            public fun interface ViewModelFactory {
                public fun create(creationExtras: CreationExtras): ViewModel
            }
        """.trimIndent()

        val testViewModel = """
            package com.test

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.SavedStateHandle
            import ru.pixnews.foundation.featuretoggles.FeatureManager
            import ru.pixnews.foundation.di.ui.base.viewmodel.ContributesViewModel

            @Suppress("UNUSED_PARAMETER")
            @ContributesViewModel
            class TestViewModel(
                featureManager: FeatureManager,
                savedStateHandle: SavedStateHandle,
            ) : ViewModel()
        """.trimIndent()

        compilationResult = compileAnvil(
            sources = arrayOf(
                baseDiViewModelStubs,
                androidxLifecycleStubs,
                featureTogglesStubs,
                testViewModel,
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
        val viewModelScopeClass = compilationResult.classLoader.loadClass(PixnewsClassName.viewModelScope)
        assertThat(clazz).haveAnnotation(ContributesTo::class.java)

        assertThat(
            clazz.getAnnotation(ContributesTo::class.java).scope.java,
        ).isEqualTo(viewModelScopeClass)
    }

    @Test
    fun `Generated module should have correct provide method`() {
        val moduleClass = compilationResult.classLoader.loadClass(generatedModuleName)
        val viewModelMapKey = compilationResult.classLoader.loadClass(PixnewsClassName.viewModelMapKey)
        val viewModelFactoryClass = compilationResult.classLoader.loadClass(PixnewsClassName.viewModelFactory)
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
