/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.viewmodel.generator

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.AnvilKspExtension
import com.squareup.anvil.compiler.internal.joinSimpleNames
import com.squareup.anvil.compiler.internal.ksp.checkClassExtendsBoundType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.ConstructorParameter
import ru.pixnews.anvil.ksp.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.ksp.codegen.common.util.parseConstructorParameters
import ru.pixnews.anvil.ksp.codegen.common.util.readClassNameOrDefault

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesViewModelAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension {
        val contributesViewModelAnnotation = environment.readClassNameOrDefault(
            KspKey.CONTRIBUTES_VIEW_MODEL_ANNOTATION,
            ClassName("ru.pixnews.anvil.ksp.codegen.viewmodel.inject", "ContributesViewModel"),
        )
        val viewModelFactoryAnnotation = environment.readClassNameOrDefault(
            KspKey.VIEW_MODEL_FACTORY,
            ClassName("ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring", "ViewModelFactory"),
        )
        val viewModelMapKeyAnnotation = environment.readClassNameOrDefault(
            KspKey.VIEW_MODEL_MAP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring", "ViewModelMapKey"),
        )
        val viewModelScopeClass = environment.readClassNameOrDefault(
            KspKey.VIEW_MODEL_SCOPE,
            ClassName("ru.pixnews.anvil.ksp.codegen.viewmodel.inject", "ViewModelScope"),
        )
        return ContributesViewModelKspExtension(
            environment,
            contributesViewModelAnnotation,
            viewModelFactoryAnnotation,
            viewModelMapKeyAnnotation,
            viewModelScopeClass,
        )
    }

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesViewModelKspExtension(
    environment: SymbolProcessorEnvironment,
    val contributesViewModelAnnotation: ClassName,
    val viewModelFactoryAnnotation: ClassName,
    val viewModelMapKeyAnnotation: ClassName,
    val viewModelScopeAnnotation: ClassName,
) : AnvilKspExtension {
    private val codeGenerator: com.google.devtools.ksp.processing.CodeGenerator = environment.codeGenerator
    override val supportedAnnotationTypes = setOf(contributesViewModelAnnotation.canonicalName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(contributesViewModelAnnotation.canonicalName)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }
            .onEach { it.checkClassExtendsBoundType(VIEW_MODEL_FQ_NAME, resolver) }
            .map { classDeclaration: KSClassDeclaration ->
                classDeclaration.containingFile!! to generateViewModelModule(classDeclaration)
            }
            .forEach { (originatingKSFile, spec) ->
                spec.writeTo(
                    codeGenerator,
                    aggregating = false,
                    originatingKSFiles = listOf(originatingKSFile),
                )
            }
        return emptyList()
    }

    private fun generateViewModelModule(
        annotatedClassDeclaration: KSClassDeclaration,
    ): FileSpec {
        val moduleClass: ClassName = annotatedClassDeclaration.toClassName().joinSimpleNames(suffix = "_FactoryModule")
        val moduleInterfaceSpec = TypeSpec.objectBuilder(moduleClass)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(viewModelScopeAnnotation))
            .addFunction(generateProvidesFactoryMethod(annotatedClassDeclaration))
            .build()
        return FileSpec.builder(moduleClass).addType(moduleInterfaceSpec).build()
    }

    private fun generateProvidesFactoryMethod(
        viewModelClass: KSClassDeclaration,
    ): FunSpec {
        val vmClassName = viewModelClass.toClassName()
        val primaryConstructor: KSFunctionDeclaration = viewModelClass.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor on $viewModelClass")
        val primaryConstructorParams = primaryConstructor.parameters.parseConstructorParameters()

        val builder = FunSpec.builder("provides${viewModelClass.simpleName.asString()}ViewModelFactory")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoMap)
            .addAnnotation(
                AnnotationSpec
                    .builder(viewModelMapKeyAnnotation)
                    .addMember("%T::class", vmClassName)
                    .build(),
            )
            .returns(viewModelFactoryAnnotation)

        primaryConstructorParams
            .filter { !it.isSavedStateHandle() }
            .forEach { builder.addParameter(it.name, it.resolvedType) }

        val viewModelConstructorParameters = primaryConstructorParams.joinToString(separator = "\n") {
            if (!it.isSavedStateHandle()) {
                "${it.name} = ${it.name},"
            } else {
                "${it.name} = it.%M()"
            }
        }
        val createViewModeStatementArgs: Array<Any> = primaryConstructorParams.mapNotNull {
            if (it.isSavedStateHandle()) CREATE_SAVED_STATE_HANDLE_MEMBER else null
        }.toTypedArray()

        builder.beginControlFlow("return %T", viewModelFactoryAnnotation)
        @Suppress("SpreadOperator")
        builder.addStatement("%T(\n$viewModelConstructorParameters\n)", vmClassName, *createViewModeStatementArgs)
        builder.endControlFlow()
        return builder.build()
    }

    private companion object {
        private val VIEW_MODEL_FQ_NAME = FqName("androidx.lifecycle.ViewModel")
        private val SAVED_STATE_HANDLE_CLASS_NAME: ClassName = ClassName("androidx.lifecycle", "SavedStateHandle")
        private val CREATE_SAVED_STATE_HANDLE_MEMBER = MemberName(
            packageName = "androidx.lifecycle",
            simpleName = "createSavedStateHandle",
            isExtension = true,
        )

        private fun ConstructorParameter.isSavedStateHandle(): Boolean = resolvedType == SAVED_STATE_HANDLE_CLASS_NAME
    }
}
